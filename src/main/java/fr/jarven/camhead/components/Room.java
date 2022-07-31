package fr.jarven.camhead.components;

import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

	public Room(String name, Location location) {
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.cameras = new TreeSet<Camera>();
		this.screens = new TreeSet<Screen>();
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
		assert location != null;
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

	public Camera getPreviousCamera(Camera camera) {
		Camera previousCamera = cameras.last();
		for (Camera camera2 : cameras) {
			if (camera2 == camera) {
				return previousCamera;
			}
			previousCamera = camera2;
		}
		throw new IllegalStateException("Camera not found");
	}

	public Camera getNextCamera(Camera camera) {
		Camera camera2 = cameras.last();
		for (Camera nextCamera : cameras) {
			if (camera2 == camera) {
				return nextCamera;
			}
			camera2 = nextCamera;
		}
		throw new IllegalStateException("Camera not found");
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

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.location = config.getLocation("location");
		this.cameraId = config.getInt("cameraId");
		this.screenId = config.getInt("screenId");
		this.saveTimestamp = config.getLong("saveTimestamp");

		SortedSet<Camera> previousCameras = new TreeSet<Camera>(this.cameras);
		SortedSet<Screen> previousScreens = new TreeSet<Screen>(this.screens);
		Set<String> camerasName = ((MemorySection) config.get("cameras")).getKeys(false);
		Set<String> screensName = ((MemorySection) config.get("screens")).getKeys(false);
		// Remove cameras and screens that were removed from the config
		for (Camera camera : previousCameras) {
			if (!camerasName.contains(camera.getName())) removeCamera(camera);
		}
		for (Screen screen : previousScreens) {
			if (!screensName.contains(screen.getName())) removeScreen(screen);
		}
		// Add cameras and screens that were added to the config
		// Update cameras and screens that were changed in the config
		camerasName.forEach(cameraName -> {
			try {
				Camera camera = (Camera) config.get("cameras." + cameraName, Camera.class);
				Optional<Camera> currentCamera = getCamera(camera.getName());
				if (currentCamera.isEmpty()) {
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
				if (currentScreen.isEmpty()) {
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

	protected static Room fromConfig(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		String name = config.getString("name");
		Location location = config.getLocation("location");
		assert name != null && location != null;
		Room room = new Room(name, location);
		room.config = config;
		room.cameraId = config.getInt("cameraId");
		room.screenId = config.getInt("screenId");
		room.saveTimestamp = config.getLong("saveTimestamp");

		Set<String> camerasName = ((MemorySection) config.get("cameras")).getKeys(false);
		Set<String> screensName = ((MemorySection) config.get("screens")).getKeys(false);
		camerasName.forEach(cameraName -> {
			try {
				Camera camera = (Camera) config.get("cameras." + cameraName, Camera.class);
				camera.setRoom(room);
				room.cameras.add(camera);
			} catch (Exception e) {
				e.printStackTrace();
				CamHead.LOGGER.warning("Could not load camera " + cameraName);
			}
		});
		screensName.forEach(screenName -> {
			try {
				Screen screen = (Screen) config.get("screens." + screenName, Screen.class);
				screen.setRoom(room);
				room.screens.add(screen);
			} catch (Exception e) {
				e.printStackTrace();
				CamHead.LOGGER.warning("Could not load screen " + screenName);
			}
		});

		return room;
	}

	public long getSaveTimestamp() {
		return saveTimestamp;
	}
}
