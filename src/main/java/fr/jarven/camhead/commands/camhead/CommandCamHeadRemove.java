package fr.jarven.camhead.commands.camhead;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadRemove extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("remove")
			.then(generateBasicCameraSelector((proxy, cam) -> removeCamera(proxy, cam)))
			.then(generateBasicScreenSelector((proxy, scr) -> removeScreen(proxy, scr)))
			.then(generateBasicRoomSelector((proxy, room) -> removeRoom(proxy, room)));
	}

	private int removeCamera(NativeProxyCommandSender proxy, Camera camera) {
		boolean removed = camera.getRoom().removeCamera(camera);
		if (!removed) {
			Messages.Resources.REMOVE_CAMERA_FAILED.params(camera, camera.getRoom()).sendFailure(proxy);
			return 0;
		}
		Messages.Resources.REMOVE_CAMERA_SUCCESS.params(camera, camera.getRoom()).send(proxy);
		CamHead.LOGGER.info("Camera " + camera.getName() + " of room " + camera.getRoom().getName() + " removed");
		return 1;
	}

	private int removeScreen(NativeProxyCommandSender proxy, Screen screen) {
		boolean removed = screen.getRoom().removeScreen(screen);
		if (!removed) {
			Messages.Resources.REMOVE_SCREEN_FAILED.params(screen, screen.getRoom()).sendFailure(proxy);
			return 0;
		}
		Messages.Resources.REMOVE_SCREEN_SUCCESS.params(screen, screen.getRoom()).send(proxy);
		CamHead.LOGGER.info("Screen " + screen.getName() + " of room " + screen.getRoom().getName() + " removed");
		return 1;
	}

	private int removeRoom(NativeProxyCommandSender proxy, Room room) {
		boolean removed = CamHead.manager.removeRoom(room);
		if (!removed) {
			Messages.Resources.REMOVE_ROOM_FAILED.params(room).sendFailure(proxy);
			return 0;
		}
		Messages.Resources.REMOVE_ROOM_SUCCESS.params(room).send(proxy);
		CamHead.LOGGER.info("Room " + room.getName() + ", " + room.getCameras().size() + " cameras and " + room.getScreens().size() + " screens removed");
		return 1;
	}
}
