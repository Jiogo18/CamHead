package fr.jarven.camhead.components;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.task.SaveTask;

public class RoomManager {
	public static final String NAME_REGEX = "^[a-zA-Z\\d_\\-]+$";
	private final SortedSet<Room> rooms;
	private final File roomFolder;

	public RoomManager() {
		this.rooms = new TreeSet<>();
		this.roomFolder = new File(CamHead.getInstance().getDataFolder(), "rooms");
	}

	public static void assertNameStandard(String name) {
		if (!name.matches(NAME_REGEX)) {
			throw new IllegalArgumentException("Name must match " + NAME_REGEX);
		}
	}

	public void loadConfig(YamlConfiguration config) {
		final SortedSet<Room> previousRooms = new TreeSet<>(rooms);
		onDisable();
		Camera.loadConfig(config);
		Screen.loadConfig(config);
		SaveTask.loadConfig(config);
		if (!roomFolder.exists()) {
			makeRoomFolderIfNeeded();
		} else {
			List<Room> newRooms =
				Stream.of(roomFolder.listFiles(file -> file.getName().endsWith(".yml")))
					.map(file -> {
						String name = file.getName().substring(0, file.getName().length() - 4); // Remove .yml
						try {
							Optional<Room> previousRoom = previousRooms.stream().filter(r -> r.getName().equals(name)).findAny();
							return Room.fromConfig(file, previousRoom);
						} catch (Exception e) {
							CamHead.LOGGER.warning("Room not loaded : '" + name + "'");
							e.printStackTrace();
							return null;
						}
					})
					.filter(room -> room != null)
					.toList();

			List<Room> roomsToRemove = rooms.stream().filter(room -> !newRooms.contains(room)).toList();
			roomsToRemove.forEach(this::removeRoomDontDeleteFile);
			newRooms.forEach(rooms::add); // Add without dirty
		}
		CamHead.LOGGER.info("Loaded " + rooms.size() + " rooms");
	}

	public boolean addRoom(Room room) {
		if (room == null || this.rooms.contains(room) || getRoom(room.getName()).isPresent()) {
			return false;
		} else {
			this.rooms.add(room);
			room.makeDirty();
			return true;
		}
	}

	public boolean removeRoom(Room room) {
		if (removeRoomDontDeleteFile(room)) {
			File file = room.getFile();
			if (file.exists()) {
				try {
					Files.delete(file.toPath());
				} catch (IOException e) {
					CamHead.LOGGER.warning("Could not delete file " + file.getName());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean removeRoomDontDeleteFile(Room room) {
		if (this.rooms.remove(room)) {
			room.removeInternal();
			return true;
		} else {
			return false;
		}
	}

	public Optional<Room> getRoom(String name) {
		return rooms.stream().filter(r -> r.getName().equals(name)).findAny();
	}

	public SortedSet<Room> getRooms() {
		return this.rooms;
	}

	public File getRoomFolder() {
		return roomFolder;
	}

	public void makeRoomFolderIfNeeded() {
		if (!roomFolder.exists()) {
			if (!roomFolder.getParentFile().exists()) {
				roomFolder.getParentFile().mkdirs();
			}
			roomFolder.mkdirs();
		}
	}

	public void reload(Room room) {
		if (room == null || !rooms.contains(room)) {
			throw new IllegalArgumentException("Room must be non null and in the room manager");
		}
		SaveTask.saveNowIfNeeded(room);
		if (!room.getFile().exists()) {
			room.save();
		}
		room.loadConfig();
	}

	public void onDisable() {
		SaveTask.onDisable();
		rooms.clear();
	}
}
