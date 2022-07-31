package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadRemove extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("remove")
			.then(generateBasicCameraSelector(1, (proxy, cam) -> removeCamera(proxy, cam)))
			.then(generateBasicScreenSelector(1, (proxy, scr) -> removeScreen(proxy, scr)))
			.then(generateBasicRoomSelector(0, (proxy, room) -> removeRoom(proxy, room)));
	}

	private int removeCamera(CommandSender sender, Camera camera) {
		boolean removed = camera.getRoom().removeCamera(camera);
		if (!removed) {
			sendFailureMessage(sender, "Failed to remove camera " + camera.getName() + " of room " + camera.getRoom().getName());
			return 0;
		}
		sendMessage(sender, "Camera " + camera.getName() + " removed");
		CamHead.LOGGER.info("Camera " + camera.getName() + " of room " + camera.getRoom().getName() + " removed");
		return 1;
	}

	private int removeScreen(CommandSender sender, Screen screen) {
		boolean removed = screen.getRoom().removeScreen(screen);
		if (!removed) {
			sendFailureMessage(sender, "Failed to remove screen " + screen.getName() + " of room " + screen.getRoom().getName());
			return 0;
		}
		sendMessage(sender, "Screen " + screen.getName() + " removed");
		CamHead.LOGGER.info("Screen " + screen.getName() + " of room " + screen.getRoom().getName() + " removed");
		return 1;
	}

	private int removeRoom(CommandSender sender, Room room) {
		boolean removed = CamHead.manager.removeRoom(room);
		if (!removed) {
			sendFailureMessage(sender, "Failed to remove room " + room.getName());
			return 0;
		}
		sendMessage(sender, "Room " + room.getName() + " removed");
		CamHead.LOGGER.info("Room " + room.getName() + ", " + room.getCameras().size() + " cameras and " + room.getScreens().size() + " screens removed");
		return 1;
	}
}
