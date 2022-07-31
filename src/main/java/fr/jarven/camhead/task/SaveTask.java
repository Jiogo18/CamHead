package fr.jarven.camhead.task;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Room;

public class SaveTask {
	private static final Map<Room, BukkitTask> rooms = new HashMap<>();
	public static int saveDelay = 10;

	private SaveTask() {
	}

	public static void onDisable() {
		new HashSet<Room>(rooms.keySet()).forEach(SaveTask::saveNowIfNeeded);
	}

	public static void saveLater(Room room) {
		assert room != null;
		if (rooms.containsKey(room))
			return;

		rooms.put(
			room,
			Bukkit.getScheduler().runTaskLater(CamHead.getInstance(), () -> {
				room.save();
				rooms.remove(room);
			}, saveDelay * 20));
	}

	public static void cancelSaveLater(Room room) {
		assert room != null;
		if (!rooms.containsKey(room))
			return;
		BukkitTask task = rooms.get(room);
		if (!task.isCancelled())
			task.cancel();
		rooms.remove(room);
	}

	public static void saveNowIfNeeded(Room room) {
		if (rooms.containsKey(room)) {
			cancelSaveLater(room);
			room.save();
		}
	}

	public static boolean willSaveSoon(Room room) {
		return rooms.containsKey(room);
	}

	public static void loadConfig(YamlConfiguration config) {
		SaveTask.saveDelay = config.getInt("saveDelay", 10);
	}
}
