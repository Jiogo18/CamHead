package fr.jarven.camhead.components;

import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.task.SaveTask;

public class Room implements ComponentBase, Comparable<Room> {
	final String name;
	private Location location;
	private int cameraId = 0;
	private int screenId = 0;
	private long saveTimestamp = 0;
	private final SortedSet<Camera> cameras;
	private final SortedSet<Screen> screens;
	private YamlConfiguration config;
	private int playerLimit = 0;

	public Room(String name, Location location) {
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.cameras = new TreeSet<>();
		this.screens = new TreeSet<>();
		playerLimit = CamHead.manager.getDefaultPlayerLimit();
	}

	public Camera addCamera(String name, Location location) {
		Camera camera = new Camera(this, name, location);
		this.cameras.add(camera);
		this.cameraId++;
		makeDirty();
		return camera;
	}

	public boolean removeCamera(Camera camera) {
		if (this.cameras.remove(camera)) {
			camera.removeInternal();
			if (config != null) config.set("cameras." + camera.getName(), null);
			makeDirty();
			return true;
		} else {
			return false;
		}
	}

	public Optional<Camera> getCamera(String name) {
		return cameras.stream().filter(c -> c.getName().equals(name)).findAny();
	}

	public Optional<Camera> getCamera(Location location) {
		return cameras.stream().filter(c -> c.isAtLocation(location)).findAny();
	}

	public SortedSet<Camera> getCameras() {
		return this.cameras;
	}

	public Screen addScreen(String name, Location location) {
		Screen screen = new Screen(this, name, location);
		this.screens.add(screen);
		this.screenId++;
		makeDirty();
		return screen;
	}

	public boolean removeScreen(Screen screen) {
		if (this.screens.remove(screen)) {
			screen.removeInternal();
			if (config != null) config.set("screens." + screen.getName(), null);
			makeDirty();
			return true;
		} else {
			return false;
		}
	}

	public Optional<Screen> getScreen(String name) {
		return this.screens.stream().filter(screen -> screen.getName().equals(name)).findFirst();
	}

	public Optional<Screen> getScreen(Location location) {
		return screens.stream().filter(c -> c.isAtLocation(location)).findAny();
	}

	public SortedSet<Screen> getScreens() {
		return this.screens;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(Location location) {
		location = location.getBlock().getLocation();
		if (!this.location.equals(location)) {
			this.location = location;
			makeDirty();
		}
	}

	public boolean teleport(Location destination) {
		if (location.equals(destination)) {
			return false;
		}
		location = destination;
		makeDirty();
		return true;
	}

	public void makeDirty() {
		SaveTask.saveLater(this);
	}

	public void makeClean() {
		SaveTask.cancelSaveLater(this);
	}

	@Override
	public int compareTo(Room c) {
		return name.compareTo(c.name);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Room) {
			return name.equals(((Room) o).name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public Optional<Camera> getPreviousCamera(Camera camera) {
		// From camera (expect camera) to start, then from end to camera (except camera)
		Camera[] previousCameras = Stream
						   .concat(this.cameras.tailSet(camera).stream(), this.cameras.headSet(camera).stream())
						   .filter(c -> !c.equals(camera))
						   .toArray(Camera[] ::new);
		for (int i = previousCameras.length - 1; i >= 0; i--) {
			Camera camera2 = previousCameras[i];
			if (camera2.canHaveSpectators()) {
				return Optional.of(camera2);
			}
		}
		return camera.canHaveSpectators() ? Optional.of(camera) : Optional.empty();
	}

	public Optional<Camera> getNextCamera(Camera camera) {
		// From camera (expect camera) to end, then from start to camera (except camera)
		Camera[] nextCameras = Stream
					       .concat(this.cameras.tailSet(camera).stream(), this.cameras.headSet(camera).stream())
					       .filter(c -> !c.equals(camera))
					       .toArray(Camera[] ::new);
		for (int i = 0; i < nextCameras.length; i++) {
			Camera camera2 = nextCameras[i];
			if (camera2.canHaveSpectators()) {
				return Optional.of(camera2);
			}
		}

		return camera.canHaveSpectators() ? Optional.of(camera) : Optional.empty();
	}

	@Override
	public boolean remove() {
		return CamHead.manager.removeRoom(this);
	}

	protected void removeInternal() {
		for (Screen screen : screens) {
			screen.removeInternal();
		}
		screens.clear();
		for (Camera camera : cameras) {
			camera.removeInternal();
		}
		cameras.clear();
	}

	protected File getFile() {
		return new File(CamHead.manager.getRoomFolder(), getName() + ".yml");
	}

	public void save() {
		File file = getFile();
		if (!file.exists()) {
			try {
				CamHead.manager.makeRoomFolderIfNeeded();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if (config == null) {
			config = new YamlConfiguration();
		}
		saveTimestamp = System.currentTimeMillis();
		config.set("name", name);
		config.set("location", location);
		config.set("cameraId", cameraId);
		config.set("screenId", screenId);
		config.set("saveTimestamp", saveTimestamp);
		config.set("playerLimit", playerLimit);
		for (Camera camera : cameras) {
			config.set("cameras." + camera.getName(), camera);
		}
		for (Screen screen : screens) {
			config.set("screens." + screen.getName(), screen);
		}

		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		makeClean();
	}

	protected void loadConfig() {
		SaveTask.saveNowIfNeeded(this);
		File file = getFile();
		if (!file.exists())
			return;

		config = YamlConfiguration.loadConfiguration(file);
		this.location = config.getLocation("location");
		this.cameraId = config.getInt("cameraId");
		this.screenId = config.getInt("screenId");
		this.saveTimestamp = config.getLong("saveTimestamp");
		this.playerLimit = config.getInt("playerLimit", CamHead.manager.getDefaultPlayerLimit());

		SortedSet<Camera> previousCameras = new TreeSet<>(this.cameras);
		SortedSet<Screen> previousScreens = new TreeSet<>(this.screens);
		Set<String> camerasName = config.contains("cameras") ? ((MemorySection) config.get("cameras")).getKeys(false) : Collections.emptySet();
		Set<String> screensName = config.contains("screens") ? ((MemorySection) config.get("screens")).getKeys(false) : Collections.emptySet();

		// Remove cameras and screens that were removed from the config
		List<Camera> camerasToRemove = previousCameras.stream().filter(c -> !camerasName.contains(c.getName())).collect(Collectors.toList());
		List<Screen> screensToRemove = previousScreens.stream().filter(s -> !screensName.contains(s.getName())).collect(Collectors.toList());
		this.cameras.removeAll(camerasToRemove);
		this.screens.removeAll(screensToRemove);
		camerasToRemove.forEach(Camera::removeInternal);
		screensToRemove.forEach(Screen::removeInternal);

		// Add cameras and screens that were added to the config
		// Update cameras and screens that were changed in the config
		camerasName.forEach(cameraName -> {
			try {
				Camera camera = (Camera) config.get("cameras." + cameraName, Camera.class);
				Optional<Camera> currentCamera = getCamera(camera.getName());
				if (!currentCamera.isPresent()) {
					camera.setRoom(this);
					this.cameras.add(camera);
				} else {
					currentCamera.get().updateWith(camera);
				}
			} catch (Exception e) {
				e.printStackTrace();
				CamHead.LOGGER.warning("Could not load camera " + cameraName);
			}
		});
		screensName.forEach(screenName -> {
			try {
				Screen screen = (Screen) config.get("screens." + screenName, Screen.class);
				Optional<Screen> currentScreen = getScreen(screen.getName());
				if (!currentScreen.isPresent()) {
					screen.setRoom(this);
					this.screens.add(screen);
				} else {
					currentScreen.get().updateWith(screen);
				}
			} catch (Exception e) {
				e.printStackTrace();
				CamHead.LOGGER.warning("Could not load screen " + screenName);
			}
		});

		makeClean();
	}

	protected static Room fromConfig(File file, Optional<Room> existingRoom) {
		Room room;

		if (existingRoom.isPresent()) {
			room = existingRoom.get();
		} else {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String name = config.getString("name");
			Location location = config.getLocation("location");
			assert name != null && location != null;
			room = new Room(name, location);
		}
		room.loadConfig();
		return room;
	}

	public long getSaveTimestamp() {
		return saveTimestamp;
	}

	public int getPlayerLimit() {
		return playerLimit;
	}
}
