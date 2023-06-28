package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadMove extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("move")
			.then(generateCameraSelector(cameraArgument -> executeWithRequiredLocation(cameraArgument, (sender, args, location) -> moveCamera(sender, getCamera(args), location))))
			.then(generateScreenSelector(screenArgument -> executeWithRequiredLocation(screenArgument, (sender, args, location) -> moveScreen(sender, getScreen(args), location))))
			.then(generateRoomSelector(roomArgument -> executeWithRequiredLocation(roomArgument, (sender, args, location) -> moveRoom(sender, getRoom(args), location))));
	}

	private int moveCamera(CommandSender sender, Camera camera, Location destination) {
		if (camera.getLocation().equals(destination)) {
			Messages.Resources.MOVE_CAMERA_SAME_LOCATION.params(camera, destination).sendFailure(sender);
			return 0;
		}
		if (!camera.teleport(destination)) {
			Messages.Resources.MOVE_CAMERA_FAILED.params(camera, destination).sendFailure(sender);
			return 0;
		}
		Messages.Resources.MOVE_CAMERA_SUCCESS.params(camera, destination).send(sender);
		return 1;
	}

	private int moveScreen(CommandSender sender, Screen screen, Location destination) {
		if (screen.getLocation().equals(destination)) {
			Messages.Resources.MOVE_SCREEN_SAME_LOCATION.params(screen, destination).sendFailure(sender);
			return 0;
		}
		if (!screen.teleport(destination)) {
			Messages.Resources.MOVE_SCREEN_FAILED.params(screen, destination).sendFailure(sender);
			return 0;
		}
		Messages.Resources.MOVE_SCREEN_SUCCESS.params(screen, destination).send(sender);
		return 1;
	}

	private int moveRoom(CommandSender sender, Room room, Location destination) {
		if (room.getLocation().equals(destination)) {
			Messages.Resources.MOVE_ROOM_SAME_LOCATION.params(room, destination).sendFailure(sender);
			return 0;
		}
		if (!room.teleport(destination)) {
			Messages.Resources.MOVE_ROOM_FAILED.params(room, destination).sendFailure(sender);
			return 0;
		}
		Messages.Resources.MOVE_ROOM_SUCCESS.params(room, destination).send(sender);
		return 1;
	}
}
