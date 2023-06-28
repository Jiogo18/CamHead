package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Optional;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadAdd extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("add")
			.then(literal("camera")
					.then(roomArgument()
							.then(executeWithRequiredLocation(new StringArgument("camera_name").replaceSuggestions(cameraSuggestions),
								(sender, args, location) -> addCamera(sender, getRoom(args), (String) args.get("camera_name"), location)))))
			.then(literal("screen")
					.then(roomArgument()
							.then(executeWithRequiredLocation(new StringArgument("screen_name").replaceSuggestions(screenSuggestions),
								(sender, args, location) -> addScreen(sender, getRoom(args), (String) args.get("screen_name"), location)))))
			.then(literal("room")
					.then(executeWithRequiredLocation(new StringArgument("room_name"),
						(sender, args, location) -> addRoom(sender, (String) args.get("room_name"), location))));
	}

	private int addCamera(CommandSender sender, Room room, String cameraName, Location location) {
		assert room != null && cameraName != null && location != null;
		Optional<Camera> existingCamera = room.getCamera(cameraName);
		if (existingCamera.isPresent()) {
			Messages.Resources.CAMERA_ALREADY_EXISTS.params(existingCamera.get(), room).sendFailure(sender);
			return 0;
		}
		Camera camera = room.addCamera(cameraName, location);
		if (camera == null) {
			Messages.Resources.CAMERA_ADD_FAILED.replace("%camera%", cameraName).params(room).sendFailure(sender);
			return 0;
		}
		Messages.Resources.CAMERA_ADD_SUCCESS.params(camera, room).send(sender);
		CamHead.LOGGER.info("Camera " + camera.getName() + " added to room " + room.getName() + " " + getLocationString(camera.getLocation()));
		return 1;
	}

	private int addScreen(CommandSender sender, Room room, String screenName, Location location) {
		assert room != null && screenName != null && location != null;
		Optional<Screen> existingScreen = room.getScreen(screenName);
		if (existingScreen.isPresent()) {
			Messages.Resources.SCREEN_ALREADY_EXISTS.params(existingScreen.get(), room).sendFailure(sender);
			return 0;
		}
		Screen screen = room.addScreen(screenName, location);
		if (screen == null) {
			Messages.Resources.SCREEN_ADD_FAILED.replace("%screen%", screenName).params(room).sendFailure(sender);
			return 0;
		}
		Messages.Resources.SCREEN_ADD_SUCCESS.params(screen, room).send(sender);
		CamHead.LOGGER.info("Screen " + screen.getName() + " added to room " + room.getName() + " " + getLocationString(screen.getLocation()));
		return 1;
	}

	private int addRoom(CommandSender sender, String roomName, Location location) {
		assert roomName != null && location != null;
		Optional<Room> existingRoom = CamHead.manager.getRoom(roomName);
		if (existingRoom.isPresent()) {
			Messages.Resources.ROOM_ALREADY_EXISTS.params(existingRoom.get()).sendFailure(sender);
			return 0;
		}
		Room room = new Room(roomName, location);
		if (!CamHead.manager.addRoom(room)) {
			Messages.Resources.ROOM_ADD_FAILED.replace("%room%", roomName).params(room).sendFailure(sender);
			return 0;
		}
		Messages.Resources.ROOM_ADD_SUCCESS.params(room).send(sender);
		CamHead.LOGGER.info("Room " + room.getName() + " created " + getLocationString(room.getLocation()));
		return 1;
	}

	private ArgumentSuggestions<CommandSender> cameraSuggestions = (info, builder) -> {
		Room room = (Room) info.previousArgs().get("room_name");
		if (room != null) builder.suggest(getNextCameraName(room));
		return builder.buildFuture();
	};

	private ArgumentSuggestions<CommandSender> screenSuggestions = (info, builder) -> {
		Room room = (Room) info.previousArgs().get("room_name");
		if (room != null) builder.suggest(getNextScreenName(room));
		return builder.buildFuture();
	};

	private static String getNextCameraName(Room room) {
		int i = 1;
		while (room.getCamera("camera" + i).isPresent()) {
			i++;
		}
		return "camera" + i;
	}

	private static String getNextScreenName(Room room) {
		int i = 1;
		while (room.getScreen("screen" + i).isPresent()) {
			i++;
		}
		return "screen" + i;
	}
}
