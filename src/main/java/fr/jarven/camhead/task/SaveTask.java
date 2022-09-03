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

	public static void saveAllNowIfNeeded() {
		new HashSet<Room>(rooms.keySet()).forEach(SaveTask::saveNowIfNeeded);
	}

	public static void onDisable() {
		saveAllNowIfNeeded();
	}

	public static void saveLater(Room room) {
		if (room == null) return;
		if (rooms.containsKey(room))
			return;

		rooms.put(
			room,
			Bukkit.getScheduler().runTaskLater(CamHead.getInstance(), () -> {
				room.save();
				rooms.remove(room);
			}, saveDelay * 20L));
	}

	public static void cancelSaveLater(Room room) {
		if (room == null) return;
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
