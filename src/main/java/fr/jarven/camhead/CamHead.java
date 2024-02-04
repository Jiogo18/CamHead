package fr.jarven.camhead;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import fr.jarven.camhead.commands.CommandTools;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.RoomManager;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.listeners.PlayerInteractBlocks;
import fr.jarven.camhead.listeners.SpectatorInteractCamera;
import fr.jarven.camhead.spectate.SpectatorManager;
import fr.jarven.camhead.task.CameraAnimator;
import fr.jarven.camhead.task.SaveTask;
import fr.jarven.camhead.utils.FakePlayerLib;
import fr.jarven.camhead.utils.Messages;

/*
 * camhead java plugin
 */
public class CamHead extends JavaPlugin {
	public static final Logger LOGGER = Logger.getLogger("CamHead");
	private static CamHead instance;
	public static RoomManager manager;
	public static SpectatorManager spectatorManager;

	@Override
	public void onLoad() {
		instance = this;
		Path dataFolder = getDataFolder().toPath();
		if (!dataFolder.toFile().exists()) {
			dataFolder.toFile().mkdir();
		}
		ConfigurationSerialization.registerClass(Camera.class);
		ConfigurationSerialization.registerClass(Screen.class);
		CommandTools.onLoad(this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		LOGGER.info("Loading plugin & configurations...");
		saveDefaultConfig();
		try {
			manager = new RoomManager();
			spectatorManager = new SpectatorManager();
			loadConfig();
			CommandTools.onEnable();
			CameraAnimator.onEnable(this);
			registerListeners();
			LOGGER.info("CamHead enabled");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.severe("Failed to load plugin");
		}
	}

	public void loadConfig() {
		SaveTask.saveAllNowIfNeeded();
		saveDefaultConfig();
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			throw new IllegalStateException("Config file not found");
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		manager.loadConfig(config);
		spectatorManager.loadConfig(config);
		CameraAnimator.loadConfig(config);
		Messages.loadConfig(config);
		FakePlayerLib.load();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		CommandTools.onDisable();
		CameraAnimator.onDisable();
		if (spectatorManager != null) spectatorManager.onDisable();
		if (manager != null) manager.onDisable();
		spectatorManager = null;
		manager = null;
		LOGGER.info("CamHead disabled");
	}

	private void registerListeners() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			pluginManager.registerEvents(new SpectatorInteractCamera(), this);
			pluginManager.registerEvents(new PlayerInteractBlocks(), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CamHead getInstance() {
		return instance;
	}
}
