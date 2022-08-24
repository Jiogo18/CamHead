package fr.jarven.camhead.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.utils.Messages;

public class CameraArgument extends CustomArgument<Camera, String> {
	public CameraArgument(String nodeName) {
		super(new StringArgument(nodeName), CameraArgument::parseCamera);
		replaceSuggestions(cameraSuggestions);
	}

	private static ArgumentSuggestions cameraSuggestions = (info, builder) -> {
		String current = info.currentArg().toUpperCase();
		// List of camera names
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room != null) {
			for (Camera camera : room.getCameras()) {
				if (camera.getName().toLowerCase().startsWith(current)) {
					builder.suggest(camera.getName());
				}
			}
		}
		return builder.buildFuture();
	};

	private static Camera parseCamera(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String cameraName = info.input();
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room == null) {
			throw Messages.createCustomArgumentException(info, Messages.Resources.ROOM_UNKNOWN);
		}
		return room.getCamera(cameraName).orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.CAMERA_UNKNOWN.replace("%camera%", cameraName)));
	}

	public static Camera getCamera(Object[] args, int argIndex) {
		return (Camera) args[argIndex];
	}
}
