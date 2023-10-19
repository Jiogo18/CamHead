package fr.jarven.camhead.utils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfo;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class Messages {
	private static String defaultLanguage = "en_US";
	private static YamlConfiguration defaultTranslations = null;
	private static Map<String, YamlConfiguration> languages = new HashMap<>();
	private static final String LANGUAGE_VERSION = "2023-10-19";

	public enum Resources {
		ROOM_UNKNOWN("camhead.room.unknown"),
		ROOM_ALREADY_EXISTS("camhead.room.alreadyexists"),
		ROOM_ADD_FAILED("camhead.room.addfailed"),
		ROOM_ADD_SUCCESS("camhead.room.addsuccess"),

		CAMERA_UNKNOWN("camhead.camera.unknown"),
		CAMERA_ALREADY_EXISTS("camhead.camera.alreadyexists"),
		CAMERA_ADD_FAILED("camhead.camera.addfailed"),
		CAMERA_ADD_SUCCESS("camhead.camera.addsuccess"),

		SCREEN_UNKNOWN("camhead.screen.unknown"),
		SCREEN_ALREADY_EXISTS("camhead.screen.alreadyexists"),
		SCREEN_ADD_FAILED("camhead.screen.addfailed"),
		SCREEN_ADD_SUCCESS("camhead.screen.addsuccess"),

		DIRECTION_UNKNOWN("camhead.direction.unknown"),
		DIRECTION_INVALID("camhead.direction.invalid"),

		DATE_TIME("camhead.date_time"),

		INFO_ROOM("camhead.info.room.main"),
		INFO_ROOM_SAVE_TIME("camhead.info.room.savetime.datetime"),
		INFO_ROOM_SAVE_NEVER("camhead.info.room.savetime.never"),
		INFO_ROOM_SAVING("camhead.info.room.saving"),
		INFO_ROOM_NOT_SAVING("camhead.info.room.notsaving"),
		INFO_CAMERA("camhead.info.camera"),
		INFO_SCREEN("camhead.info.screen"),
		INFO_PLAYER("camhead.info.player"),

		LIST_ROOMS_HEADER("camhead.list.rooms.header"),
		LIST_ROOMS_LINE("camhead.list.rooms.room"),
		LIST_CAMERAS_HEADER("camhead.list.cameras.header"),
		LIST_CAMERAS_LINE("camhead.list.cameras.camera"),
		LIST_SCREENS_HEADER("camhead.list.screens.header"),
		LIST_SCREENS_LINE("camhead.list.screens.screen"),
		LIST_DISTANCE_BLOCK("camhead.list.distance.block"),
		LIST_DISTANCE_OTHER_WORLD("camhead.list.distance.otherworld"),

		MOVE_CAMERA_SAME_LOCATION("camhead.move.camera.samelocation"),
		MOVE_CAMERA_FAILED("camhead.move.camera.failed"),
		MOVE_CAMERA_SUCCESS("camhead.move.camera.success"),
		MOVE_SCREEN_SAME_LOCATION("camhead.move.screen.samelocation"),
		MOVE_SCREEN_FAILED("camhead.move.screen.failed"),
		MOVE_SCREEN_SUCCESS("camhead.move.screen.success"),
		MOVE_ROOM_SAME_LOCATION("camhead.move.room.samelocation"),
		MOVE_ROOM_FAILED("camhead.move.room.failed"),
		MOVE_ROOM_SUCCESS("camhead.move.room.success"),

		RELOAD_SUCCESS("camhead.reload.success"),
		RELOAD_ROOM_SUCCESS("camhead.reload.room.success"),
		RELOAD_RELOADING("camhead.reload.reloading"),

		REMOVE_ROOM_FAILED("camhead.remove.room.failed"),
		REMOVE_ROOM_SUCCESS("camhead.remove.room.success"),
		REMOVE_CAMERA_FAILED("camhead.remove.camera.failed"),
		REMOVE_CAMERA_SUCCESS("camhead.remove.camera.success"),
		REMOVE_SCREEN_FAILED("camhead.remove.screen.failed"),
		REMOVE_SCREEN_SUCCESS("camhead.remove.screen.success"),
		REMOVE_CAMERA_BREAK_BUTTON("camhead.remove.camera.break.button"),
		REMOVE_CAMERA_BREAK_PERMISSION("camhead.remove.camera.break.nopermission"),
		REMOVE_CAMERA_BREAK_CONFIRM("camhead.remove.camera.break.confirm"),
		REMOVE_CAMERA_BREAK_YES("camhead.remove.camera.break.yes"),
		REMOVE_CAMERA_BREAK_HOVER("camhead.remove.camera.break.hover"),
		REMOVE_SCREEN_BREAK_BUTTON("camhead.remove.screen.break.button"),
		REMOVE_SCREEN_BREAK_PERMISSION("camhead.remove.screen.break.nopermission"),
		REMOVE_SCREEN_BREAK_CONFIRM("camhead.remove.screen.break.confirm"),
		REMOVE_SCREEN_BREAK_YES("camhead.remove.screen.break.yes"),
		REMOVE_SCREEN_BREAK_HOVER("camhead.remove.screen.break.hover"),

		REPLACE_ROOM_SUCCESS("camhead.replace.room.success"),
		REPLACE_CAMERA_SUCCESS("camhead.replace.camera.success"),
		REPLACE_SCREEN_SUCCESS("camhead.replace.screen.success"),

		ROTATE_CAMERA_SUCCESS("camhead.rotate.camera.success"),
		ROTATE_CAMERA_DETAILED("camhead.rotate.camera.detailed"),
		ROTATE_SCREEN_SUCCESS("camhead.rotate.screen.success"),
		ROTATE_SCREEN_DETAILED("camhead.rotate.screen.detailed"),

		SPECTATE_NOT_A_PLAYER("camhead.spectate.notaplayer"),
		SPECTATE_ENTER_SUCCESS("camhead.spectate.enter.success"),
		SPECTATE_ENTER_CHANGED("camhead.spectate.enter.changed"),
		SPECTATE_ENTER_SAME_CAMERA("camhead.spectate.enter.same_camera"),
		SPECTATE_ENTER_FAILED_UNKNOWN("camhead.spectate.enter.failed.unknown"),
		SPECTATE_ENTER_FAILED_NO_CAMERAS("camhead.spectate.enter.failed.no_cameras"),
		SPECTATE_ENTER_FAILED_NO_PERMISSION("camhead.spectate.enter.failed.no_permission"),
		SPECTATE_ENTER_FAILED_NO_SEAT("camhead.spectate.enter.failed.no_seat"),
		SPECTATE_ENTER_FAILED_ROOM_FULL("camhead.spectate.enter.failed.room_full"),
		SPECTATE_ENTER_FAILED_LEAVING("camhead.spectate.enter.failed.leaving"),
		SPECTATE_LEAVE_SUCCESS("camhead.spectate.leave.success"),
		SPECTATE_LEAVE_FAILED_ALREADY_LEAVING("camhead.spectate.leave.failed.already_leaving"),
		SPECTATE_LEAVE_FAILED_UNKNOWN("camhead.spectate.leave.failed.unknown"),
		SPECTATE_LEAVE_FAILED_NOT_SPECTATING("camhead.spectate.leave.failed.not_spectating"),
		SPECTATE_LEAVE_FAILED_NO_PERMISSION("camhead.spectate.leave.failed.no_permission"),

		TELEPORT_NOT_AN_ENTITY("camhead.teleport.notanentity"),

		CONFIG_GLOBAL_ENTER_ENABLED("camhead.config.global.enter.enabled"),
		CONFIG_GLOBAL_LEAVE_ENABLED("camhead.config.global.leave.enabled"),
		CONFIG_GLOBAL_ENTER_DISABLED("camhead.config.global.enter.disabled"),
		CONFIG_GLOBAL_LEAVE_DISABLED("camhead.config.global.leave.disabled"),
		CONFIG_ROOM_ENTER_ENABLED("camhead.config.room.enter.enabled"),
		CONFIG_ROOM_LEAVE_ENABLED("camhead.config.room.leave.enabled"),
		CONFIG_ROOM_ENTER_DISABLED("camhead.config.room.enter.disabled"),
		CONFIG_ROOM_LEAVE_DISABLED("camhead.config.room.leave.disabled"),
		CONFIG_CAMERA_VISIBLE_ENABLED("camhead.config.camera.visible.enabled"),
		CONFIG_CAMERA_VISIBLE_DISABLED("camhead.config.camera.visible.disabled"),
		;

		private String key;
		Resources(String key) {
			this.key = key;
		}
		private String getKey() {
			return key;
		}
		public MessageBuilder getBuilder() {
			return new MessageBuilder(this);
		}
		public MessageBuilder replace(String key, String value) {
			return getBuilder().replace(key, value);
		}
		public MessageBuilder params(Object... objects) {
			return getBuilder().params(objects);
		}
		private String build(CommandSender sender) {
			if (sender instanceof NativeProxyCommandSender) return build(((NativeProxyCommandSender) sender).getCaller());
			return Messages.tr(sender, this);
		}
		public Resources send(CommandSender sender) {
			sender.sendMessage(build(sender));
			return this;
		}
		public Resources send(NativeProxyCommandSender proxy) {
			proxy.sendMessage(build(proxy));
			return this;
		}
		public void send(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, TextComponent.fromLegacyText(build(player)));
		}
		public Resources sendFailure(CommandSender sender) {
			sender.spigot().sendMessage(new ComponentBuilder(build(sender)).color(ChatColor.RED).create());
			return this;
		}
		public Resources sendFailure(NativeProxyCommandSender proxy) {
			proxy.spigot().sendMessage(new ComponentBuilder(build(proxy)).color(ChatColor.RED).create());
			return this;
		}
		public Resources sendFailure(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, new ComponentBuilder(build(player)).color(ChatColor.RED).create());
			return this;
		}
	}

	public static class MessageBuilder {
		private final Resources messageResource;
		private final Map<String, String> replacements = new HashMap<>();
		private final Map<String, MessageBuilder> messageReplacements = new HashMap<>();
		private MessageBuilder(Resources key) {
			this.messageResource = key;
		}
		public MessageBuilder replace(String key, String value) {
			replacements.put(key, value);
			return this;
		}
		public MessageBuilder replace(String key, MessageBuilder value) {
			messageReplacements.put(key, value);
			return this;
		}
		public MessageBuilder params(Object... objects) {
			for (int i = 0; i < objects.length; i++) {
				Object o = objects[i];
				if (o == null) {
				} else if (o instanceof Camera) {
					replace("%camera%", ((Camera) o).getName());
				} else if (o instanceof Screen) {
					replace("%screen%", ((Screen) o).getName());
				} else if (o instanceof Room) {
					replace("%room%", ((Room) o).getName());
				} else if (o instanceof Player) {
					replace("%player%", ((Player) o).getName());
				} else if (o instanceof CommandSender) {
					replace("%sender%", ((CommandSender) o).getName());
				} else if (o instanceof Location) {
					Location loc = (Location) o;
					replace("%world%", loc.getWorld().getName());
					replace("%x%", String.valueOf(loc.getX()));
					replace("%y%", String.valueOf(loc.getY()));
					replace("%z%", String.valueOf(loc.getZ()));
					replace("%pitch%", String.valueOf(loc.getPitch()));
					replace("%yaw%", String.valueOf(loc.getYaw()));
					replace("%X%", String.valueOf(loc.getBlockX()));
					replace("%Y%", String.valueOf(loc.getBlockY()));
					replace("%Z%", String.valueOf(loc.getBlockZ()));
				} else {
					replace("%" + o.getClass().getName().toLowerCase() + "%", objects[i + 1].toString());
				}
			}
			return this;
		}
		public String build(CommandSender sender) {
			if (sender instanceof NativeProxyCommandSender) {
				return build(((NativeProxyCommandSender) sender).getCaller());
			}
			String message = Messages.tr(sender, messageResource);
			for (Map.Entry<String, MessageBuilder> entry : messageReplacements.entrySet()) {
				if (message.contains(entry.getKey())) {
					message = message.replace(entry.getKey(), entry.getValue().build(sender));
				}
			}
			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				if (message.contains(entry.getKey())) {
					message = message.replace(entry.getKey(), entry.getValue());
				}
			}
			return message;
		}
		public MessageBuilder send(CommandSender sender) {
			sender.sendMessage(build(sender));
			return this;
		}
		public MessageBuilder send(NativeProxyCommandSender proxy) {
			proxy.getCaller().sendMessage(build(proxy.getCaller()));
			return this;
		}
		public MessageBuilder send(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, TextComponent.fromLegacyText(build(player)));
			return this;
		}
		public MessageBuilder sendToBoth(NativeProxyCommandSender proxy) {
			if (!proxy.getCallee().equals(proxy.getCaller())) {
				send(proxy.getCaller());
			}
			send(proxy.getCallee());
			return this;
		}
		public MessageBuilder sendFailure(CommandSender sender) {
			sender.spigot().sendMessage(new ComponentBuilder(build(sender)).color(ChatColor.RED).create());
			return this;
		}
		public MessageBuilder sendFailure(NativeProxyCommandSender proxy) {
			proxy.getCaller().spigot().sendMessage(new ComponentBuilder(build(proxy.getCaller())).color(ChatColor.RED).create());
			return this;
		}
		public MessageBuilder sendFailure(Player player, ChatMessageType position) {
			player.spigot().sendMessage(position, new ComponentBuilder(build(player)).color(ChatColor.RED).create());
			return this;
		}
	}

	private Messages() {}

	public static void loadConfig(YamlConfiguration config) {
		defaultLanguage = config.getString("lang", "en_US");
		// The folder /lang to store the language files (and custom ones)
		File folder = new File(CamHead.getInstance().getDataFolder(), "lang");
		if (!folder.exists()) {
			folder.mkdir();
		}
		// Save if not present
		saveLanguageFile("en_US", false);
		saveLanguageFile("fr_FR", false);
		// Get every files written in the /lang folder (even the files wich are not in the resources)
		for (File file : folder.listFiles(f -> f.getName().endsWith(".yml"))) {
			addLanguage(file);
		}
		// Determine the default language
		defaultTranslations = languages.get(defaultLanguage.toLowerCase());
		if (defaultTranslations == null) {
			defaultTranslations = languages.get("en_US");
			if (defaultTranslations == null) {
				CamHead.LOGGER.warning("No default language found, using en_US or " + defaultLanguage);
			}
		}
	}

	private static void saveLanguageFile(String lang, boolean replace) {
		File file = new File(CamHead.getInstance().getDataFolder(), "lang/" + lang + ".yml");
		if (!file.exists() || replace) {
			CamHead.getInstance().saveResource("lang/" + lang + ".yml", replace);
		}
	}

	private static void addLanguage(File file) {
		String name = file.getName().replace(".yml", "");
		YamlConfiguration lang = YamlConfiguration.loadConfiguration(file);
		String version = lang.getString("version.revision", "");
		boolean versionWarning = lang.getBoolean("version.warnings");
		boolean autoupdate = lang.getBoolean("version.autoupdate");
		// If outdated
		if (!version.equals(LANGUAGE_VERSION)) {
			// Update it
			if (autoupdate) {
				if (versionWarning) {
					CamHead.LOGGER.warning("Language file " + name + " is outdated, updating to version " + LANGUAGE_VERSION);
				}
				saveLanguageFile(name, true);
				lang = YamlConfiguration.loadConfiguration(file);

				version = lang.getString("version.revision", "");
				// If the resource is outdated
				if (!version.equals(LANGUAGE_VERSION)) {
					CamHead.LOGGER.severe("Language file " + name + " of CamHead's resources is outdated. Please, contact the plugin author to update it."
						+ " (expected " + LANGUAGE_VERSION + ", found " + version + ")");
				}
			} else {
				if (versionWarning) {
					CamHead.LOGGER.warning("Language file " + name + " is outdated, please update or delete it");
				}
			}
		}
		// Register the language's config
		languages.put(name.toLowerCase(), lang);
	}

	private static String tr(String local, String messageKey) {
		YamlConfiguration lang = languages.getOrDefault(local.toLowerCase(), defaultTranslations);
		if (lang == null) {
			CamHead.LOGGER.warning("No language found for " + local + " and " + defaultLanguage);
			return messageKey;
		}
		String translated = lang.getString(messageKey, null);
		if (translated != null) return translated; // local translation

		if (defaultTranslations == null) return messageKey; // no default language (shouldn't happen)

		return defaultTranslations.getString(messageKey, messageKey); // default translation
	}

	public static String tr(CommandSender sender, Resources message) {
		String local = defaultLanguage;
		if (sender instanceof Player) local = ((Player) sender).getLocale();
		return tr(local, message.getKey());
	}

	public static CustomArgumentException createCustomArgumentException(CustomArgumentInfo<?> info, Resources message) {
		return CustomArgumentException.fromString(tr(info.sender(), message));
	}

	public static CustomArgumentException createCustomArgumentException(CustomArgumentInfo<?> info, MessageBuilder builder) {
		return CustomArgumentException.fromString(builder.build(info.sender()));
	}
}
