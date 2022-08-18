package fr.jarven.camhead.spectate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;

public class CameraSpectator implements Comparable<CameraSpectator> {
	public static GameMode GAMEMODE = GameMode.SPECTATOR;
	private Player player;
	private Camera camera;
	private PlayerState stateBeforeEnter;
	private boolean leaving = false;

	public CameraSpectator(Player player, Camera camera) {
		if (player == null || camera == null) {
			throw new IllegalArgumentException("player and camera must not be null");
		}
		this.player = player;
		this.camera = camera;
		this.stateBeforeEnter = new PlayerState(player);
	}

	@Override
	public int compareTo(CameraSpectator o) {
		return player.getUniqueId().compareTo(o.player.getUniqueId());
	}

	public Player getPlayer() {
		return player;
	}

	public Camera getCamera() {
		return camera;
	}

	public Room getRoom() {
		return camera.getRoom();
	}

	public void previousCamera() {
		camera = getRoom().getPreviousCamera(camera);
		enter();
	}

	public void nextCamera() {
		camera = getRoom().getNextCamera(camera);
		enter();
	}

	public Location getLocation(Location playerYawPitch) {
		Location location = camera.getTpLocation().clone();
		location.setYaw(playerYawPitch.getYaw());
		location.setPitch(playerYawPitch.getPitch());
		return location;
	}

	public boolean enter() {
		player.setGameMode(GAMEMODE);
		player.setSneaking(false);
		player.setInvisible(true);
		player.setCollidable(false);
		player.setCanPickupItems(false);
		if (GAMEMODE == GameMode.SPECTATOR) {
			player.setSpectatorTarget(null);
		}
		camera.getCameraSeat().addPassenger(player);
		leaving = false;
		return true;
	}

	public boolean enter(Camera camera) {
		if (camera == null) {
			throw new IllegalArgumentException("camera must not be null");
		}
		if (this.camera != camera) {
			if (this.camera != null) {
				this.camera.getCameraSeat().removePassenger(player);
			}
			this.camera = camera;
			return enter();
		}
		return true;
	}

	protected boolean leave() {
		if (leaving) return false;
		leaving = true;
		camera.getCameraSeat().removePassenger(player);
		stateBeforeEnter.restore();
		player.setSneaking(false);
		return true;
	}

	public boolean isLeaving() {
		return this.leaving;
	}

	private class PlayerState {
		private final Player player;
		private final GameMode gameMode;
		private final Location location;
		private final boolean hadAllowFlight;
		private final boolean wasFlying;
		private final float flySpeed;
		private final boolean wasInvisible;
		private final boolean hadGravity;
		private final boolean wasCollidable;
		private final boolean wasCanPickupItems;

		private PlayerState(Player player) {
			this.player = player;
			this.gameMode = player.getGameMode();
			this.location = player.getLocation();
			this.hadAllowFlight = player.getAllowFlight();
			this.wasFlying = player.isFlying();
			this.flySpeed = player.getFlySpeed();
			this.wasInvisible = player.isInvisible();
			this.hadGravity = player.hasGravity();
			this.wasCollidable = player.isCollidable();
			this.wasCanPickupItems = player.getCanPickupItems();
		}

		private void restore() {
			player.setGameMode(gameMode);
			player.teleport(location, TeleportCause.PLUGIN);
			player.setAllowFlight(hadAllowFlight);
			player.setFlying(wasFlying);
			player.setFlySpeed(flySpeed);
			player.setInvisible(wasInvisible);
			player.setGravity(hadGravity);
			player.setCollidable(wasCollidable);
			player.setCanPickupItems(wasCanPickupItems);
		}
	}
}
