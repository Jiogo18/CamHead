package fr.jarven.camhead.spectate;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class SpectatorManager {
	private final Map<UUID, CameraSpectator> spectators = new HashMap<>();
	private boolean allowSneakToLeave = true;
	private boolean allowClickToChange = true;
	private boolean allowSlotToChange = true;
	private boolean allowEnterByScreen = true;
	private boolean allowEnterByCamera = false;
	private boolean adminClickToRemove = true;
	private final Random random = new Random();

	public SpectatorManager() {
		random.setSeed(System.currentTimeMillis());
	}

	public boolean isSpectator(OfflinePlayer player) {
		return spectators.containsKey(player.getUniqueId());
	}

	public CameraSpectator getSpectator(OfflinePlayer player) {
		return spectators.get(player.getUniqueId());
	}

	public boolean canEnter(Player player, Camera camera) {
		Room room = camera.getRoom();
		CameraSpectator spectator = getSpectator(player);
		if (spectator != null) {
			if (spectator.getRoom().equals(room)) return true; // Can change camera
		}
		long playersInRoom = spectators.values().stream().filter(s -> s.getCamera().getRoom().equals(room)).count();
		return playersInRoom < room.getPlayerLimit();
	}

	public boolean enter(Player player, Camera camera) {
		if (!canEnter(player, camera)) return false;

		CameraSpectator spectator = getSpectator(player);
		if (spectator != null) {
			if (spectator.getCamera() == camera) {
				return false;
			} else {
				spectator.enter(camera);
			}
		} else {
			spectator = new CameraSpectator(player, camera);
			spectators.put(player.getUniqueId(), spectator);
			spectator.enter();
		}
		return true;
	}

	public boolean enter(Player player, Room room) {
		if (room.getCameras().isEmpty()) {
			return false;
		} else {
			int index = random.nextInt(room.getCameras().size());
			Camera camera = room.getCameras().toArray(new Camera[0])[index];
			return enter(player, camera);
		}
	}

	public boolean enter(Player player, Screen screen) {
		return enter(player, screen.getRoom());
	}

	public boolean leave(Player player) {
		CameraSpectator spectator = getSpectator(player);
		if (spectator == null) {
			return false;
		} else if (!spectator.leave()) {
			return false;
		} else {
			spectators.remove(player.getUniqueId());
			return true;
		}
	}

	public void onDisable() {
		for (CameraSpectator spectator : spectators.values()) {
			spectator.leave();
		}
		spectators.clear();
	}

	public void loadConfig(YamlConfiguration config) {
		allowSneakToLeave = config.getBoolean("allowSneakToLeave", true);
		allowClickToChange = config.getBoolean("allowClickToChange", true);
		allowSlotToChange = config.getBoolean("allowSlotToChange", true);
		allowEnterByScreen = config.getBoolean("allowEnterByScreen", true);
		allowEnterByCamera = config.getBoolean("allowEnterByCamera", false);
		adminClickToRemove = config.getBoolean("adminClickToRemove", true);
		CameraSpectator.GAMEMODE = GameMode.valueOf(config.getString("camera.gamemode", "SPECTATOR"));
	}

	public void onPlayerConnect(Player player) {
		// Hide spectators from the player
		for (CameraSpectator spectator : this.spectators.values()) {
			if (player.getUniqueId().equals(spectator.getPlayer().getUniqueId())) continue;
			player.hidePlayer(CamHead.getInstance(), spectator.getPlayer());
		}
	}

	public boolean isAllowSneakToLeave() {
		return allowSneakToLeave;
	}

	public boolean isAllowClickToChange() {
		return allowClickToChange;
	}

	public boolean isAllowSlotToChange() {
		return allowSlotToChange;
	}

	public boolean isAllowEnterByScreen() {
		return allowEnterByScreen;
	}

	public boolean isAllowEnterByCamera() {
		return allowEnterByCamera;
	}

	public boolean isAdminClickToRemove() {
		return adminClickToRemove;
	}
}
