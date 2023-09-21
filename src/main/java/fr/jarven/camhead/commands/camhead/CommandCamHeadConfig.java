package fr.jarven.camhead.commands.camhead;

import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadConfig extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("config")
			.then(literal("global")
					.then(literal("allow-enter")
							.then(new BooleanArgument("allow")
									.executesNative(CommandCamHeadConfig::allowEnterGlobal)))
					.then(literal("allow-leave")
							.then(new BooleanArgument("allow")
									.executesNative(CommandCamHeadConfig::allowLeaveGlobal))))
			.then(literal("room")
					.then(roomArgument()
							.then(literal("allow-enter")
									.then(new BooleanArgument("allow")
											.executesNative(CommandCamHeadConfig::allowEnterRoom))
									.executesNative((proxy, args) -> { Room room = getRoom(args); return room.canEnter() ? 1 : 0; }))
							.then(literal("allow-leave")
									.then(new BooleanArgument("allow")
											.executesNative(CommandCamHeadConfig::allowLeaveRoom))
									.executesNative((proxy, args) -> { Room room = getRoom(args); return room.canLeave() ? 1 : 0; }))))
			.then(generateCameraSelector(cameraArgument -> cameraArgument
				.then(literal("visible")
						.executesNative((proxy, args) -> { Camera camera = getCamera(args); proxy.sendMessage(camera.isVisible() ? "true" : "false"); return camera.isVisible() ? 1 : 0; })
						.then(new BooleanArgument("visible")
								.executesNative((proxy, args) -> (setCameraVisible(proxy, args, (Boolean) args.get("visible"))))))));
	}

	public static int allowEnterGlobal(NativeProxyCommandSender proxy, CommandArguments args) {
		// Allow enter for every rooms
		boolean allow = (boolean) args.getOptional("allow").orElse(true);
		for (Room room : CamHead.manager.getRooms()) {
			room.setAllowEnter(allow);
		}

		if (allow) {
			Messages.Resources.CONFIG_GLOBAL_ENTER_ENABLED.send(proxy);
		} else {
			Messages.Resources.CONFIG_GLOBAL_ENTER_DISABLED.send(proxy);
		}

		return 1;
	}

	public static int allowLeaveGlobal(NativeProxyCommandSender proxy, CommandArguments args) {
		// Allow leave for every rooms
		boolean allow = (boolean) args.getOptional("allow").orElse(true);
		for (Room room : CamHead.manager.getRooms()) {
			room.setAllowLeave(allow);
		}

		if (allow) {
			Messages.Resources.CONFIG_GLOBAL_LEAVE_ENABLED.send(proxy);
		} else {
			Messages.Resources.CONFIG_GLOBAL_LEAVE_DISABLED.send(proxy);
		}

		return 1;
	}

	public static int allowEnterRoom(NativeProxyCommandSender proxy, CommandArguments args) {
		// Allow enter for the room
		boolean allow = (boolean) args.getOptional("allow").orElse(true);
		Room room = getRoom(args);
		room.setAllowEnter(allow);

		if (allow) {
			Messages.Resources.CONFIG_ROOM_ENTER_ENABLED.params(room).send(proxy);
		} else {
			Messages.Resources.CONFIG_ROOM_ENTER_DISABLED.params(room).send(proxy);
		}

		return 1;
	}

	public static int allowLeaveRoom(NativeProxyCommandSender proxy, CommandArguments args) {
		// Allow leave for the room
		boolean allow = (boolean) args.getOptional("allow").orElse(true);
		Room room = getRoom(args);
		room.setAllowLeave(allow);

		if (allow) {
			Messages.Resources.CONFIG_ROOM_LEAVE_ENABLED.params(room).send(proxy);
		} else {
			Messages.Resources.CONFIG_ROOM_LEAVE_DISABLED.params(room).send(proxy);
		}

		return 1;
	}
	
	private int setCameraVisible(NativeProxyCommandSender proxy, CommandArguments args, Boolean b) {
		Camera camera = getCamera(args);
		if (b != null) {
			// Set camera visible
			camera.setVisible(b);
		}

		if (camera.isVisible()) {
			Messages.Resources.CONFIG_CAMERA_VISIBLE_ENABLED.params(camera).send(proxy);
		} else {
			Messages.Resources.CONFIG_CAMERA_VISIBLE_DISABLED.params(camera).send(proxy);
		}

		return 1;
	}
}
