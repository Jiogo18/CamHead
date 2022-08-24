package fr.jarven.camhead.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.spectate.CameraSpectator;

public class SpectatorInteractCamera implements Listener {
	private boolean isInCamera(Player player) {
		return CamHead.spectatorManager.isSpectator(player);
	}

	private boolean leaveIfInCamera(Player player) {
		if (isInCamera(player)) {
			CamHead.spectatorManager.leave(player);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Press "Shift" => Leave
	 */
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) return;
		if (!event.isSneaking()) return; // unsneak
		if (CamHead.spectatorManager.isAllowSneakToLeave()) {
			leaveIfInCamera(event.getPlayer());
		}
	}

	/**
	 * Dismount => Leave
	 */
	@EventHandler
	public void onDismount(VehicleExitEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getExited() instanceof Player)) return;
		Player player = (Player) event.getExited();
		if (!isInCamera(player)) return;
		if (CamHead.spectatorManager.isAllowSneakToLeave()) {
			leaveIfInCamera(player);
		} else {
			event.setCancelled(true);
		}
	}

	/**
	 * Change gamemode => Leave
	 */
	@EventHandler
	public void onGamemode(PlayerGameModeChangeEvent event) {
		if (event.isCancelled()) return;
		if (event.getNewGameMode().equals(CameraSpectator.GAMEMODE)) return;
		leaveIfInCamera(event.getPlayer());
	}

	/**
	 * On Quit => Leave
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		leaveIfInCamera(event.getPlayer());
	}

	/**
	 * On death => Leave
	 */
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		leaveIfInCamera(event.getEntity());
	}

	/**
	 * Teleport => Leave or Deny
	 */
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.isCancelled()) return;
		CameraSpectator spectator = CamHead.spectatorManager.getSpectator(event.getPlayer());
		if (spectator == null || spectator.isLeaving()) return;
		if (spectator.getCamera().getCameraSeat().getPassengers().contains(spectator.getPlayer())) return;
		switch (event.getCause()) {
			case UNKNOWN: // spectator
				break;
			case COMMAND:
			case END_PORTAL:
			case NETHER_PORTAL:
			case PLUGIN:
				leaveIfInCamera(event.getPlayer());
				break;
			default:
				Bukkit.getScheduler().runTaskLater(CamHead.getInstance(), () -> spectator.enter(), 1L); // cancel and tp back
				break;
		}
	}

	/**
	 * On move => Deny
	 */
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		CameraSpectator spectator = CamHead.spectatorManager.getSpectator(event.getPlayer());
		if (spectator == null || spectator.isLeaving()) return;
		if (spectator.getCamera().isAtTpLocation(spectator.getPlayer().getLocation())) return;
		Bukkit.getScheduler().runTaskLater(CamHead.getInstance(), () -> spectator.enter(), 1L); // cancel and tp back
	}

	/**
	 * Swing/Punch => Change camera
	 */
	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING
			&& CamHead.spectatorManager.isSpectator(event.getPlayer())
			&& CamHead.spectatorManager.isAllowClickToChange()) {
			CameraSpectator spectator = CamHead.spectatorManager.getSpectator(event.getPlayer());
			spectator.nextCamera();
		}
	}

	/**
	 * Drop item => Deny
	 */
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;
		CameraSpectator spectator = CamHead.spectatorManager.getSpectator(event.getPlayer());
		if (spectator == null || spectator.isLeaving()) return;
		event.setCancelled(true);
	}

	/**
	 * Reconnect => hide spectators from player
	 */
	@EventHandler
	public void onConnect(PlayerJoinEvent event) {
		CamHead.spectatorManager.onPlayerConnect(event.getPlayer());
	}
}
