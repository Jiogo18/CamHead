package fr.jarven.camhead.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;

public class CameraArgument extends CustomArgument<Camera, String> {
	public CameraArgument(String nodeName) {
		super(new StringArgument(nodeName), CameraArgument::parseCamera);
		replaceSuggestions(cameraSuggestions);
	}

	private static ArgumentSuggestions cameraSuggestions = (info, builder) -> {
		// List of camera names
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room != null) {
			for (Camera camera : room.getCameras()) {
				builder.suggest(camera.getName());
			}
		}
		return builder.buildFuture();
	};

	private static Camera parseCamera(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String cameraName = info.input();
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room == null) {
			throw new CustomArgumentException(new MessageBuilder("camhead.argument.room.unknown").appendArgInput());
		}
		return room.getCamera(cameraName).orElseThrow(() -> new CustomArgumentException(new MessageBuilder("camhead.argument.camera.unknown").appendArgInput()));
	}

	public static Camera getCamera(Object[] args, int argIndex) {
		assert args[argIndex] instanceof Camera;
		return (Camera) args[argIndex];
	}
}
