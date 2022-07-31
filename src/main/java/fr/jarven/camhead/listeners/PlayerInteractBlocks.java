package fr.jarven.camhead.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.Optional;
import java.util.UUID;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.CommandTools;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.ComponentBase;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.spectate.CameraSpectator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class PlayerInteractBlocks implements Listener {
	private boolean canBePluginBlock(Block block) {
		return block != null
			&& (block.getType().equals(Camera.MATERIAL_SUPPORT)
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
		if (CamHead.spectatorManager.isSpectator(player)) {
			event.setCancelled(true);
		} else if (canBePluginBlock(event.getClickedBlock())) {
			ComponentBase component = getPluginComponent(event.getClickedBlock());
			if (component == null) return;
			switch (event.getAction()) {
				case LEFT_CLICK_BLOCK:
					event.setCancelled(true);
					if (player.hasPermission("camhead.maker")) {
						removePluginBlock(player, component);
					} else if (player.hasPermission("camhead.admin") && player.isSneaking()) {
						player.sendMessage("§cYou must have the permission camhead.maker to remove this block");
					}
					break;
				case RIGHT_CLICK_BLOCK:
					if (player.isSneaking() && (player.hasPermission("camhead.maker") || player.hasPermission("camhead.admin"))) {
						infoPluginBlock(player, component);
					} else {
						usePluginBlock(player, component);
					}
					break;
				default:
					break;
			}
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
			usePluginBlock(player, component);
			event.setCancelled(true);
		}
	}

	private void removePluginBlock(Player player, ComponentBase component) {
		ComponentBuilder builder = new ComponentBuilder("Are you sure you want to remove this ");
		String removeCommand;
		if (component instanceof Camera) {
			builder.append("camera ? ");
			removeCommand = "/camhead remove camera " + ((Camera) component).getRoom().getName() + " " + component.getName();
		} else if (component instanceof Screen) {
			builder.append("screen ? ");
			removeCommand = "/camhead remove screen " + ((Screen) component).getRoom().getName() + " " + component.getName();
		} else {
			throw new IllegalArgumentException("Unknown component type");
		}
		builder.append(
			new ComponentBuilder("")
				.color(ChatColor.YELLOW)
				.append("[Yes]")
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, removeCommand))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("See the command")))
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
		if (component instanceof Camera) {
			Camera camera = (Camera) component;
			StringBuilder sb = new StringBuilder();
			sb.append("Camera ").append(camera.getName());
			sb.append("\n- Location: ").append(CommandTools.getLocationString(camera.getLocation()));
			sb.append("\n- Room: ").append(camera.getRoom().getName());
			player.sendMessage(sb.toString());
		} else if (component instanceof Screen) {
			Screen screen = (Screen) component;
			StringBuilder sb = new StringBuilder();
			sb.append("Screen ").append(screen.getName());
			sb.append("\n- Location: ").append(CommandTools.getLocationString(screen.getLocation()));
			sb.append("\n- Room: ").append(screen.getRoom().getName());
			player.sendMessage(sb.toString());
		} else {
			throw new IllegalStateException("Unknown component " + component);
		}
	}
}
