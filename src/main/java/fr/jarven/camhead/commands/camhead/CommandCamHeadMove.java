package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadMove extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("move")
			.then(generateCameraSelector(cameraArgument -> executeWithRequiredLocation(cameraArgument, 2, (sender, args, location) -> moveCamera(sender, getCamera(args, 1), location))))
			.then(generateScreenSelector(screenArgument -> executeWithRequiredLocation(screenArgument, 2, (sender, args, location) -> moveScreen(sender, getScreen(args, 1), location))))
			.then(generateRoomSelector(roomArgument -> executeWithRequiredLocation(roomArgument, 1, (sender, args, location) -> moveRoom(sender, getRoom(args, 0), location))));
	}

	private int moveCamera(CommandSender sender, Camera camera, Location destination) {
		if (camera.getLocation().equals(destination)) {
			sendFailureMessage(sender, "Camera is already at this location");
			return 0;
		}
		if (!camera.teleport(destination)) {
			sendFailureMessage(sender, "Could not move camera");
			return 0;
		}
		sendMessage(sender, "Camera moved");
		return 1;
	}

	private int moveScreen(CommandSender sender, Screen screen, Location destination) {
		if (screen.getLocation().equals(destination)) {
			sendFailureMessage(sender, "Screen is already at this location");
			return 0;
		}
		if (!screen.teleport(destination)) {
			sendFailureMessage(sender, "Could not move screen");
			return 0;
		}
		sendMessage(sender, "Screen moved");
		return 1;
	}

	private int moveRoom(CommandSender sender, Room room, Location destination) {
		if (room.getLocation().equals(destination)) {
			sendFailureMessage(sender, "Room is already at this location");
			return 0;
		}
		if (!room.teleport(destination)) {
			sendFailureMessage(sender, "Could not move room");
			return 0;
		}
		sendMessage(sender, "Room moved");
		return 1;
	}
}
