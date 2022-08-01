package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;

import java.util.Set;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;
import fr.jarven.camhead.utils.Messages.MessageBuilder;

public class CommandCamHeadList extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("list")
			.then(literal("camera").then(roomArgument().executesNative(this::listCameras)))
			.then(literal("screen").then(roomArgument().executesNative(this::listScreens)))
			.then(literal("room").executesNative(this::listRooms));
	}

	private int listCameras(NativeProxyCommandSender proxy, Object[] args) {
		Room room = getRoom(args, 0);
		Set<Camera> cameras = room.getCameras();
		MessageBuilder builder = Messages.Resources.LIST_CAMERAS_HEADER.params(room).replace("%cameraCount%", String.valueOf(cameras.size()));
		MessageBuilder lastItem = builder;
		for (Camera camera : cameras) {
			MessageBuilder nextItem = Messages.Resources.LIST_CAMERAS_LINE
							  .params(camera)
							  .replace("%distance%", getDistanceWith(proxy, camera.getLocation()));
			lastItem.replace("%nextCamera%", nextItem);
			lastItem = nextItem;
		}
		lastItem.replace("\n%nextCamera%", "");
		builder.sendToBoth(proxy);
		return cameras.size();
	}

	private int listScreens(NativeProxyCommandSender proxy, Object[] args) {
		Room room = getRoom(args, 0);
		Set<Screen> screens = room.getScreens();
		MessageBuilder builder = Messages.Resources.LIST_SCREENS_HEADER.params(room).replace("%screenCount%", String.valueOf(screens.size()));
		MessageBuilder lastItem = builder;
		for (Screen screen : screens) {
			MessageBuilder nextItem = Messages.Resources.LIST_SCREENS_LINE
							  .params(screen)
							  .replace("%distance%", getDistanceWith(proxy, screen.getLocation()));
			lastItem.replace("%nextScreen%", nextItem);
			lastItem = nextItem;
		}
		lastItem.replace("\n%nextScreen%", "");
		builder.sendToBoth(proxy);
		return screens.size();
	}

	private int listRooms(NativeProxyCommandSender proxy, Object[] args) {
		Set<Room> rooms = CamHead.manager.getRooms();
		MessageBuilder builder = Messages.Resources.LIST_ROOMS_HEADER.replace("%roomCount%", String.valueOf(rooms.size()));
		MessageBuilder lastItem = builder;
		for (Room room : rooms) {
			MessageBuilder nextItem = Messages.Resources.LIST_ROOMS_LINE
							  .params(room)
							  .replace("%cameraCount%", String.valueOf(room.getCameras().size()))
							  .replace("%screenCount%", String.valueOf(room.getScreens().size()))
							  .replace("%distance%", getDistanceWith(proxy, room.getLocation()));
			lastItem.replace("%nextRoom%", nextItem);
			lastItem = nextItem;
		}
		lastItem.replace("\n%nextRoom%", "");
		builder.sendToBoth(proxy);
		return rooms.size();
	}

	private MessageBuilder getDistanceWith(NativeProxyCommandSender source, Location target) {
		if (source.getWorld().equals(target.getWorld())) {
			return Messages.Resources.LIST_DISTANCE_BLOCK
				.replace("%distance%", roundIfAboveTen(source.getLocation().distance(target)))
				.replace("%world%", target.getWorld().getName());
		} else {
			return Messages.Resources.LIST_DISTANCE_OTHER_WORLD
				.replace("%distance%", "N/A")
				.replace("%world%", target.getWorld().getName());
		}
	}
}
