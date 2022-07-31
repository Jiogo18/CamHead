package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;

import java.util.Set;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadList extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("list")
			.then(literal("camera").then(roomArgument().executesNative(this::listCameras)))
			.then(literal("screen").then(roomArgument().executesNative(this::listScreens)))
			.then(literal("room").executesNative(this::listRooms));
	}

	private int listCameras(NativeProxyCommandSender proxy, Object[] args) throws WrapperCommandSyntaxException {
		Room room = getRoom(args, 0);
		if (room == null) {
			sendFailureMessage(proxy, "Room not found");
			return 0;
		} else {
			StringBuilder sb = new StringBuilder();
			Set<Camera> cameras = room.getCameras();
			sb.append("Cameras in room ").append(room.getName()).append(": ").append(cameras.size());
			for (Camera camera : cameras) {
				sb.append("\n- ").append(camera.getName()).append(getDistanceWith(proxy.getLocation(), camera.getLocation()));
			}
			sendMessageToBoth(proxy, sb.toString());
			return cameras.size();
		}
	}

	private int listScreens(NativeProxyCommandSender proxy, Object[] args) throws WrapperCommandSyntaxException {
		Room room = getRoom(args, 0);
		if (room == null) {
			sendFailureMessage(proxy, "Room not found");
			return 0;
		} else {
			StringBuilder sb = new StringBuilder();
			Set<Screen> screens = room.getScreens();
			sb.append("Screens in room ").append(room.getName()).append(": ").append(screens.size());
			for (Screen screen : screens) {
				sb.append("\n- ").append(screen.getName()).append(getDistanceWith(proxy.getLocation(), screen.getLocation()));
			}
			sendMessageToBoth(proxy, sb.toString());
			return screens.size();
		}
	}

	private int listRooms(NativeProxyCommandSender proxy, Object[] args) throws WrapperCommandSyntaxException {
		Set<Room> rooms = CamHead.manager.getRooms();
		if (rooms.isEmpty()) {
			sendMessage(proxy, "No room found");
			return 0;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Rooms: ").append(rooms.size());
			for (Room room : rooms) {
				sb.append("\n- ").append(room.getName()).append(getDistanceWith(proxy.getLocation(), room.getLocation()));
			}
			sendMessageToBoth(proxy, sb.toString());
			return rooms.size();
		}
	}

	private String getDistanceWith(Location source, Location target) {
		if (source.getWorld().equals(target.getWorld())) {
			return " (" + roundIfAboveTen(source.distance(target)) + " m)";
		} else {
			return " (in " + target.getWorld().getName() + ")";
		}
	}

	private void sendMessageToBoth(NativeProxyCommandSender proxy, String message) {
		if (!proxy.getCallee().equals(proxy.getCaller())) {
			sendMessage(proxy.getCallee(), message);
		}
		sendMessage(proxy.getCaller(), message);
	}
}
