package fr.jarven.camhead.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;
import java.util.UUID;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.ComponentBase;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.spectate.CameraSpectator;
import fr.jarven.camhead.utils.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class PlayerInteractBlocks implements Listener {
	private boolean canBePluginBlock(Block block) {
		return block != null
			&& (block.getType().equals(Camera.MATERIAL_BLOCK)
				|| block.getType().equals(Screen.MATERIAL_DOWN_SUPPORT)
				|| block.getType().equals(Screen.MATERIAL_UP_SUPPORT)
				|| block.getType().equals(Screen.MATERIAL_WALL_SUPPORT));
	}

	private ComponentBase getPluginComponent(Block block) {
		if (!canBePluginBlock(block)) return null;
		for (Room room : CamHead.manager.getRooms()) {
			Optional<Camera> camera = room.getCamera(block.getLocation());
			if (camera.isPresent()) return camera.get();
			Optional<Screen> screen = room.getScreen(block.getLocation());
			if (screen.isPresent()) return screen.get();
		}
		return null;
	}

	private ComponentBase getPluginComponent(ArmorStand armorStand) {
		if (armorStand == null) return null;
		UUID uuid = armorStand.getUniqueId();
		for (Room room : CamHead.manager.getRooms()) {
			Optional<Camera> camera = room.getCameras().stream().filter(c -> c.getCameraSeat().getUniqueId().equals(uuid) || c.getCameraman().getUniqueId().equals(uuid)).findAny();
			if (camera.isPresent()) return camera.get();
		}
		return null;
	}

	/**
	 * Break a block
	 */
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		ComponentBase component = getPluginComponent(event.getBlock());
		if (component != null) {
			component.remove();
		}
	}

	/**
	 * Interact with a block or when in a camera
	 */
	@EventHandler
	public void onPlayerInteractBlock(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		boolean isMainHand = event.getHand() == EquipmentSlot.HAND;
		if (CamHead.spectatorManager.isSpectator(player)) {
			event.setCancelled(true);
		} else if (isMainHand && canBePluginBlock(event.getClickedBlock())) {
			ComponentBase component = getPluginComponent(event.getClickedBlock());
			if (component == null) return;
			switch (event.getAction()) {
				case LEFT_CLICK_BLOCK:
					event.setCancelled(true);
					onPlayerLeftClickComponent(player, component);
					break;
				case RIGHT_CLICK_BLOCK:
					event.setCancelled(true);
					onPlayerRightClickComponent(player, component);
					break;
				default:
					break;
			}
		}
	}

	private void onPlayerLeftClickComponent(Player player, ComponentBase component) {
		if (player.hasPermission("camhead.maker")) {
			removePluginBlock(player, component);
		} else if (player.hasPermission("camhead.admin") && player.isSneaking()) {
			if (component instanceof Camera) {
				Messages.Resources.REMOVE_CAMERA_BREAK_PERMISSION.sendFailure(player);
			} else if (component instanceof Screen) {
				Messages.Resources.REMOVE_SCREEN_BREAK_PERMISSION.sendFailure(player);
			}
		} else {
			throw new IllegalStateException("Unknow component " + component);
		}
	}

	private void onPlayerRightClickComponent(Player player, ComponentBase component) {
		if (player.isSneaking() && (player.hasPermission("camhead.maker") || player.hasPermission("camhead.admin"))) {
			infoPluginBlock(player, component);
		} else {
			usePluginBlock(player, component);
		}
	}

	@EventHandler
	private void onSlotChanged(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		if (CamHead.spectatorManager.isSpectator(player) && CamHead.spectatorManager.isAllowSlotToChange()) {
			CameraSpectator spectator = CamHead.spectatorManager.getSpectator(player);
			int difference = event.getNewSlot() - event.getPreviousSlot();
			if (difference < -4) {
				difference += 9;
			} else if (difference > 4) {
				difference -= 9;
			}
			if (difference < 0) { // go left
				spectator.nextCamera();
			} else if (difference > 0) { // go right
				spectator.previousCamera();
			}
		}
	}

	@EventHandler
	private void onPickingArmorStandItem(PlayerArmorStandManipulateEvent event) {
		Player player = event.getPlayer();
		if (CamHead.spectatorManager.isSpectator(player)) {
			event.setCancelled(true);
		} else {
			ComponentBase component = getPluginComponent(event.getRightClicked());
			if (component == null) return;
			event.setCancelled(true);
			onPlayerRightClickComponent(player, component);
		}
	}

	@EventHandler
	private void onPlayerInteractArmorStand(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		if (CamHead.spectatorManager.isSpectator(player)) {
			event.setCancelled(true);
		} else if (event.getRightClicked().getType() == EntityType.ARMOR_STAND) {
			ComponentBase component = getPluginComponent((ArmorStand) event.getRightClicked());
			if (component == null) return;
			event.setCancelled(true);
			onPlayerRightClickComponent(player, component);
		}
	}

	private void removePluginBlock(Player player, ComponentBase component) {
		ComponentBuilder builder;
		Messages.Resources yes;
		Messages.Resources hover;
		String removeCommand;
		if (component instanceof Camera) {
			builder = new ComponentBuilder(Messages.Resources.REMOVE_CAMERA_BREAK_CONFIRM.getBuilder().build(player));
			removeCommand = "/camhead remove camera " + ((Camera) component).getRoom().getName() + " " + component.getName();
			yes = Messages.Resources.REMOVE_CAMERA_BREAK_YES;
			hover = Messages.Resources.REMOVE_CAMERA_BREAK_HOVER;
		} else if (component instanceof Screen) {
			builder = new ComponentBuilder(Messages.Resources.REMOVE_SCREEN_BREAK_CONFIRM.getBuilder().build(player));
			removeCommand = "/camhead remove screen " + ((Screen) component).getRoom().getName() + " " + component.getName();
			yes = Messages.Resources.REMOVE_SCREEN_BREAK_YES;
			hover = Messages.Resources.REMOVE_SCREEN_BREAK_HOVER;
		} else {
			throw new IllegalArgumentException("Unknown component " + component);
		}
		builder.append(
			new ComponentBuilder("")
				.color(ChatColor.YELLOW)
				.append(yes.getBuilder().build(player))
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, removeCommand))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover.getBuilder().build(player))))
				.create());
		player.spigot().sendMessage(builder.create());
	}

	private void usePluginBlock(Player player, ComponentBase component) {
		if (component instanceof Camera) {
			if (CamHead.spectatorManager.isAllowEnterByCamera()) {
				CamHead.spectatorManager.enter(player, ((Camera) component));
			}
		} else if (component instanceof Screen) {
			if (CamHead.spectatorManager.isAllowEnterByScreen()) {
				CamHead.spectatorManager.enter(player, ((Screen) component));
			}
		}
	}

	private void infoPluginBlock(Player player, ComponentBase component) {
		ComponentBuilder builder;
		Messages.Resources removeButton;
		Messages.Resources removeButtonHover;
		String removeCommand;
		if (component instanceof Camera) {
			Camera camera = (Camera) component;
			builder = new ComponentBuilder(Messages.Resources.INFO_CAMERA
							       .params(camera, camera.getLocation(), camera.getRoom())
							       .replace("%supportDirection%", camera.getSupportDirection().name())
							       .replace("%animationDirection%", camera.getAnimationDirection().name())
							       .build(player));
			removeCommand = "/camhead remove camera " + ((Camera) component).getRoom().getName() + " " + component.getName();
			removeButton = Messages.Resources.REMOVE_CAMERA_BREAK_BUTTON;
			removeButtonHover = Messages.Resources.REMOVE_CAMERA_BREAK_HOVER;
		} else if (component instanceof Screen) {
			Screen screen = (Screen) component;
			builder = new ComponentBuilder(Messages.Resources.INFO_SCREEN
							       .params(screen, screen.getLocation(), screen.getRoom())
							       .replace("%supportDirection%", screen.getSupportDirection().name())
							       .replace("%facingDirection%", screen.getFacingDirection().name())
							       .build(player));
			removeCommand = "/camhead remove screen " + ((Screen) component).getRoom().getName() + " " + component.getName();
			removeButton = Messages.Resources.REMOVE_SCREEN_BREAK_BUTTON;
			removeButtonHover = Messages.Resources.REMOVE_SCREEN_BREAK_HOVER;
		} else {
			throw new IllegalStateException("Unknown component " + component);
		}

		builder
			.append("\n")
			.append(
				new ComponentBuilder("  ")
					.color(ChatColor.YELLOW)
					.append(removeButton.getBuilder().build(player))
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, removeCommand))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(removeButtonHover.getBuilder().build(player))))
					.create());
		player.spigot().sendMessage(builder.create());
	}
}
