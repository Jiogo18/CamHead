package fr.jarven.camhead.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.commands.arguments.CameraArgument;
import fr.jarven.camhead.commands.arguments.RoomArgument;
import fr.jarven.camhead.commands.arguments.ScreenArgument;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public abstract class CommandTools {
	protected static LiteralArgument literal(String commandName) {
		return new LiteralArgument(commandName);
	}

	protected static Argument<Room> cameraArgument(Function<CameraArgument, Argument<Camera>> generateAfter) {
		return new RoomArgument("room_name").then(generateAfter.apply(new CameraArgument("camera_name")));
	}

	protected static Argument<Room> screenArgument(Function<ScreenArgument, Argument<Screen>> generateAfter) {
		return new RoomArgument("room_name").then(generateAfter.apply(new ScreenArgument("screen_name")));
	}

	protected static RoomArgument roomArgument() {
		return new RoomArgument("room_name");
	}

	protected static @Nonnull Camera getCamera(CommandArguments args) {
		Camera camera = (Camera) args.get("camera_name");
		if (camera == null) {
			throw new IllegalArgumentException("Camera argument is missing");
		}
		return camera;
	}

	protected static @Nonnull Screen getScreen(CommandArguments args) {
		Screen screen = (Screen) args.get("screen_name");
		if (screen == null) {
			throw new IllegalArgumentException("Screen argument is missing");
		}
		return screen;
	}

	protected static @Nonnull Room getRoom(CommandArguments args) {
		Room room = (Room) args.get("room_name");
		if (room == null) {
			throw new IllegalArgumentException("Room argument is missing");
		}
		return room;
	}

	protected Argument<String> generateCameraSelector(Function<CameraArgument, Argument<Camera>> generateAfter) {
		return literal("camera").then(cameraArgument(generateAfter));
	}

	protected Argument<String> generateBasicCameraSelector(BiFunction<NativeProxyCommandSender, Camera, Integer> callback) {
		return generateCameraSelector(cameraArgument -> cameraArgument.executesNative((proxy, args) -> (callback.apply(proxy, getCamera(args)))));
	}

	protected Argument<String> generateScreenSelector(Function<ScreenArgument, Argument<Screen>> generateAfter) {
		return literal("screen").then(screenArgument(generateAfter));
	}

	protected Argument<String> generateBasicScreenSelector(BiFunction<NativeProxyCommandSender, Screen, Integer> callback) {
		return generateScreenSelector(screenArgument -> screenArgument.executesNative((proxy, args) -> (callback.apply(proxy, getScreen(args)))));
	}

	protected Argument<String> generateRoomSelector(Function<RoomArgument, Argument<Room>> generateAfter) {
		return literal("room").then(generateAfter.apply(roomArgument()));
	}

	protected Argument<String> generateBasicRoomSelector(BiFunction<NativeProxyCommandSender, Room, Integer> callback) {
		return generateRoomSelector(roomArgument -> roomArgument.executesNative((proxy, args) -> (callback.apply(proxy, getRoom(args)))));
	}

	@FunctionalInterface
	protected interface TriFunction<T, U, V, R> {
		R apply(T t, U u, V v);
	}

	protected <T> Argument<T> executeWithRequiredLocation(Argument<T> lastArgument, TriFunction<CommandSender, CommandArguments, Location, Integer> function) {
		return lastArgument
			.then(new LocationArgument("location", LocationType.BLOCK_POSITION)
					.executes((sender, args) -> (function.apply(sender, args, (Location) args.get("location")))))
			.executesNative((proxy, args) -> (function.apply(proxy.getCaller(), args, proxy.getLocation())));
	}

	public static String getVectorString(Vector vector) {
		return "(" + vector.getBlockX() + ";" + vector.getBlockY() + ";" + vector.getBlockZ() + ")";
	}

	public static String getLocationString(Location location) {
		return "in " + location.getWorld().getName() + " at " + getVectorString(location.toVector());
	}

	public static String translateLocation(String message, Location location) {
		return message.replace("%world%", location.getWorld().getName())
			.replace("%x%", String.valueOf(location.getX()))
			.replace("%y%", String.valueOf(location.getY()))
			.replace("%z%", String.valueOf(location.getZ()))
			.replace("%yaw%", String.valueOf(location.getYaw()))
			.replace("%pitch%", String.valueOf(location.getPitch()));
	}

	protected String roundIfAboveTen(double n) {
		return n >= 10 ? Integer.toString((int) Math.round(n)) : Double.toString(Math.round(n * 10) / 10.0);
	}

	public static void onLoad(JavaPlugin plugin) {
		if (!CommandAPI.isLoaded())
			CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin));
	}

	public static void onEnable() {
		new CommandCamHead().getCommandTree().register();
	}

	public static void onDisable() {
		CommandAPI.unregister("camhead");
	}
}
