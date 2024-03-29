package fr.jarven.camhead.spectate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.spectate.EnterResult.EnterResultType;
import fr.jarven.camhead.spectate.LeaveResult.LeaveResultType;
import fr.jarven.camhead.utils.FakePlayerLib;

public class CameraSpectator implements Comparable<CameraSpectator> {
	public static GameMode GAMEMODE = GameMode.SPECTATOR;
	private @Nonnull Player player;
	private @Nonnull Camera camera;
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

	@Override
	public boolean equals(Object o) {
		return o instanceof CameraSpectator && player.getUniqueId().equals(((CameraSpectator) o).player.getUniqueId());
	}

	public int hashCode() {
		return player.getUniqueId().hashCode();
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
		Camera previousCamera = getRoom().getPreviousCamera(camera);
		if (previousCamera != null) {
			enter(previousCamera);
		} else {
			leave();
		}
	}

	public void nextCamera() {
		Camera nextCamera = getRoom().getNextCamera(camera);
		if (nextCamera != null) {
			enter(nextCamera);
		} else {
			leave();
		}
	}

	public Location getLocation(Location playerYawPitch) {
		Location location = camera.getTpLocation().clone();
		location.setYaw(playerYawPitch.getYaw());
		location.setPitch(playerYawPitch.getPitch());
		return location;
	}

	public EnterResult enter() {
		if (leaving) return new EnterResult(camera, EnterResultType.LEAVING);
		if (!camera.hasSeat()) return new EnterResult(camera, EnterResultType.NO_SEAT);
		if (!camera.getRoom().canEnter()) return new EnterResult(camera, EnterResultType.NO_PERMISSION);

		player.setGameMode(GAMEMODE);
		player.setSneaking(false);
		player.setInvisible(true);
		player.setCollidable(false);
		player.setCanPickupItems(false);
		player.setInvulnerable(true);
		player.setAllowFlight(true);
		player.setFlying(true);
		if (GAMEMODE == GameMode.SPECTATOR) {
			player.setSpectatorTarget(null);
		}
		player.getInventory().clear();
		player.addScoreboardTag("CamHeadSpectator");
		leaving = false;
		if (camera.addPlayer(player)) {
			return new EnterResult(camera, EnterResultType.SUCCESS_ENTER);
		} else {
			CamHead.spectatorManager.leave(player);
			return new EnterResult(camera, EnterResultType.FAILED_UNKNOWN);
		}
	}

	public EnterResult enter(@Nonnull Camera camera) {
		if (!camera.getRoom().canEnter()) return new EnterResult(camera, EnterResultType.NO_PERMISSION);
		if (!camera.canHaveSpectators()) return new EnterResult(camera, EnterResultType.NO_SEAT);

		if (this.camera != camera) {
			boolean wasInCamera = this.camera != null;
			if (this.camera != null) {
				this.camera.removePlayer(player);
			}
			this.camera = camera;
			EnterResult result = enter();
			if (result.isSuccess() && wasInCamera) {
				return new EnterResult(camera, EnterResultType.SUCCESS_CHANGE);
			} else {
				return result;
			}
		} else {
			return new EnterResult(camera, EnterResultType.SAME_CAMERA);
		}
	}

	protected LeaveResult leave(boolean force) {
		if (leaving) return new LeaveResult(camera, LeaveResultType.ALREADY_LEAVING);
		if (!force && !camera.getRoom().canLeave()) return new LeaveResult(camera, LeaveResultType.NO_PERMISSION);
		leaving = true;
		camera.removePlayer(player);
		stateBeforeEnter.restore();
		player.setSneaking(false);
		return new LeaveResult(camera, LeaveResultType.SUCCESS);
	}

	protected LeaveResult leave() {
		return leave(false);
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
		private final boolean wasCollidable;
		private final boolean wasCanPickupItems;
		private final boolean wasInvulnerable;
		private final ItemStack[] inventory;
		private final int air;
		private FakePlayerLib fakePlayer;

		private PlayerState(Player player) {
			this.player = player;
			this.gameMode = player.getGameMode();
			this.location = player.getLocation();
			this.hadAllowFlight = player.getAllowFlight();
			this.wasFlying = player.isFlying();
			this.flySpeed = player.getFlySpeed();
			this.wasInvisible = this.gameMode != GameMode.SPECTATOR && player.isInvisible();
			this.wasCollidable = player.isCollidable();
			this.wasCanPickupItems = player.getCanPickupItems();
			this.wasInvulnerable = player.isInvulnerable();
			this.inventory = player.getInventory().getContents().clone();
			this.air = player.getRemainingAir();
			this.fakePlayer = new FakePlayerLib(this.location, player.getUniqueId(), player.getName());
		}

		private void restore() {
			player.setGameMode(gameMode);
			player.teleport(location, TeleportCause.PLUGIN);
			player.setAllowFlight(hadAllowFlight);
			player.setFlying(wasFlying);
			player.setFlySpeed(flySpeed);
			player.setInvisible(wasInvisible);
			player.setCollidable(wasCollidable);
			player.setCanPickupItems(wasCanPickupItems);
			player.setInvulnerable(wasInvulnerable);
			player.getInventory().setContents(inventory);
			player.removeScoreboardTag("CamHeadSpectator");
			player.setRemainingAir(air);
			fakePlayer.destroy();
		}
	}
}
