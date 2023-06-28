package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadReplace extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("replace")
			.then(generateBasicCameraSelector((proxy, cam) -> replaceCamera(proxy, cam)))
			.then(generateBasicScreenSelector((proxy, scr) -> replaceScreen(proxy, scr)))
			.then(generateBasicRoomSelector((proxy, room) -> replaceRoom(proxy, room)));
	}

	private int replaceCamera(CommandSender sender, Camera camera) {
		camera.replace();
		Messages.Resources.REPLACE_CAMERA_SUCCESS.params(camera, camera.getRoom()).send(sender);
		return 1;
	}

	private int replaceScreen(CommandSender sender, Screen screen) {
		screen.replace();
		Messages.Resources.REPLACE_SCREEN_SUCCESS.params(screen, screen.getRoom()).send(sender);
		return 1;
	}

	private int replaceRoom(CommandSender sender, Room room) {
		int count = 0;
		for (Camera camera : room.getCameras()) {
			camera.replace();
			count++;
		}
		for (Screen screen : room.getScreens()) {
			screen.replace();
			count++;
		}
		Messages.Resources.REPLACE_ROOM_SUCCESS.params(room).replace("%count%", String.valueOf(count)).send(sender);
		return 1;
	}
}
