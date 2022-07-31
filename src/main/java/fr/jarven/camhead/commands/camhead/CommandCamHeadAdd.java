package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadAdd extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("add")
			.then(literal("camera")
					.then(roomArgument()
							.then(executeWithRequiredLocation(stringArgument("camera"), 2, (sender, args, location) -> addCamera(sender, getRoom(args, 0), args[1].toString(), location)))))
			.then(literal("screen")
					.then(roomArgument()
							.then(executeWithRequiredLocation(stringArgument("screen"), 2, (sender, args, location) -> addScreen(sender, getRoom(args, 0), args[1].toString(), location)))))
			.then(literal("room")
					.then(executeWithRequiredLocation(stringArgument("room"), 1, (sender, args, location) -> addRoom(sender, args[0].toString(), location))));
	}

	private int addCamera(CommandSender sender, Room room, String cameraName, Location location) {
		assert room != null && cameraName != null && location != null;
		if (room.getCamera(cameraName).isPresent()) {
			sendFailureMessage(sender, "A camera with this name already exists in this room");
			return 0;
		}
		Camera camera = room.addCamera(cameraName, location);
		if (camera == null) {
			sendFailureMessage(sender, "Failed to add camera " + cameraName + " to room " + room.getName());
			return 0;
		}
		sendMessage(sender, "Camera " + camera.getName() + " added to room " + room.getName());
		CamHead.LOGGER.info("Camera " + camera.getName() + " added to room " + room.getName() + " " + getLocationString(camera.getLocation()));
		return 1;
	}

	private int addScreen(CommandSender sender, Room room, String screenName, Location location) {
		assert room != null && screenName != null && location != null;
		if (room.getScreen(screenName).isPresent()) {
			sendFailureMessage(sender, "Screen " + screenName + " already exists in room " + room.getName());
			return 0;
		}
		Screen screen = room.addScreen(screenName, location);
		if (screen == null) {
			sendFailureMessage(sender, "Failed to add screen " + screenName + " to room " + room.getName());
			return 0;
		}
		sendMessage(sender, "Screen " + screen.getName() + " added to room " + room.getName());
		CamHead.LOGGER.info("Screen " + screen.getName() + " added to room " + room.getName() + " " + getLocationString(screen.getLocation()));
		return 1;
	}

	private int addRoom(CommandSender sender, String roomName, Location location) {
		assert roomName != null && location != null;
		if (CamHead.manager.getRoom(roomName).isPresent()) {
			sendFailureMessage(sender, "Room " + roomName + " already exists");
			return 0;
		}
		Room room = new Room(roomName, location);
		if (!CamHead.manager.addRoom(room)) {
			sendFailureMessage(sender, "Failed to add room " + roomName);
			return 0;
		}
		sendMessage(sender, "Room " + room.getName() + " created");
		CamHead.LOGGER.info("Room " + room.getName() + " created " + getLocationString(room.getLocation()));
		return 1;
	}
}
