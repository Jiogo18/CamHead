package fr.jarven.camhead.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nonnull;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.StringArgument;
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

	protected static StringArgument stringArgument(String name) {
		return new StringArgument(name);
	}

	protected static ArgumentTree cameraArgument(Function<CameraArgument, ArgumentTree> generateAfter) {
		return new RoomArgument("room_name").then(generateAfter.apply(new CameraArgument("camera_name")));
	}

	protected static ArgumentTree screenArgument(Function<ScreenArgument, ArgumentTree> generateAfter) {
		return new RoomArgument("room_name").then(generateAfter.apply(new ScreenArgument("screen_name")));
	}

	protected static RoomArgument roomArgument() {
		return new RoomArgument("room_name");
	}

	protected ArgumentTree generateCameraSelector(Function<CameraArgument, ArgumentTree> generateAfter) {
		return literal("camera").then(cameraArgument(generateAfter));
	}

	protected ArgumentTree generateBasicCameraSelector(int argIndex, BiFunction<NativeProxyCommandSender, Camera, Integer> callback) {
		return generateCameraSelector(cameraArgument -> cameraArgument.executesNative((proxy, args) -> { return callback.apply(proxy, getCamera(args, argIndex)); }));
	}

	protected ArgumentTree generateScreenSelector(Function<ScreenArgument, ArgumentTree> generateAfter) {
		return literal("screen").then(screenArgument(generateAfter));
	}

	protected ArgumentTree generateBasicScreenSelector(int argIndex, BiFunction<NativeProxyCommandSender, Screen, Integer> callback) {
		return generateScreenSelector(screenArgument -> screenArgument.executesNative((proxy, args) -> { return callback.apply(proxy, getScreen(args, argIndex)); }));
	}

	protected ArgumentTree generateRoomSelector(Function<RoomArgument, ArgumentTree> generateAfter) {
		return literal("room").then(generateAfter.apply(roomArgument()));
	}

	protected ArgumentTree generateBasicRoomSelector(int argIndex, BiFunction<NativeProxyCommandSender, Room, Integer> callback) {
		return generateRoomSelector(roomArgument -> roomArgument.executesNative((proxy, args) -> { return callback.apply(proxy, getRoom(args, argIndex)); }));
	}

	protected @Nonnull Camera getCamera(Object[] args, int argIndex) {
		Camera camera = CameraArgument.getCamera(args, argIndex);
		if (camera == null) {
			throw new IllegalArgumentException("Camera not found");
		}
		return camera;
	}

	protected @Nonnull Screen getScreen(Object[] args, int argIndex) {
		Screen screen = ScreenArgument.getScreen(args, argIndex);
		if (screen == null) {
			throw new IllegalArgumentException("Screen not found");
		}
		return screen;
	}

	protected @Nonnull Room getRoom(Object[] args, int argIndex) {
		Room room = RoomArgument.getRoom(args, argIndex);
		if (room == null) {
			throw new IllegalArgumentException("Room not found");
		}
		return room;
	}

	@FunctionalInterface
	protected interface TriFunction<T, U, V, R> {
		R apply(T t, U u, V v);
	}

	protected ArgumentTree executeWithRequiredLocation(ArgumentTree lastArgument, int argCount, TriFunction<CommandSender, Object[], Location, Integer> function) {
		return lastArgument
			.then(new LocationArgument("location", LocationType.BLOCK_POSITION)
					.executes((sender, args) -> { return function.apply(sender, args, (Location) args[argCount]); }))
			.executesNative((proxy, args) -> { return function.apply(proxy.getCaller(), args, proxy.getLocation()); });
	}

	public static <S> Optional<Location> getLocation(S sender) {
		if (sender instanceof Entity) {
			return Optional.of(((Entity) sender).getLocation());
		} else {
			return Optional.empty();
		}
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

	public static void onEnable() {
		new CommandCamHead().getCommandTree().register();
	}

	public static void onDisable() {
		CommandAPI.unregister("camhead");
	}
}
