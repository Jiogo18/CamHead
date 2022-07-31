package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadReplace extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("replace")
			.then(generateBasicCameraSelector(1, (proxy, cam) -> replaceCamera(proxy, cam)))
			.then(generateBasicScreenSelector(1, (proxy, scr) -> replaceScreen(proxy, scr)))
			.then(generateBasicRoomSelector(0, (proxy, room) -> replaceRoom(proxy, room)));
	}

	private int replaceCamera(CommandSender sender, Camera camera) {
		camera.replace();
		sendMessage(sender, "Camera " + camera.getName() + " replaced");
		return 1;
	}

	private int replaceScreen(CommandSender sender, Screen screen) {
		screen.replace();
		sendMessage(sender, "Screen " + screen.getName() + " replaced");
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
		sendMessage(sender, count + " components of " + room.getName() + " replaced");
		return 1;
	}
}
