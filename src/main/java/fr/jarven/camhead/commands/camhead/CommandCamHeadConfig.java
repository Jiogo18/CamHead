package fr.jarven.camhead.commands.camhead;

import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
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
									.executesNative((proxy, args) -> { Room room = (Room) args[0]; return room.canEnter() ? 1 : 0; }))
							.then(literal("allow-leave")
									.then(new BooleanArgument("allow")
											.executesNative(CommandCamHeadConfig::allowLeaveRoom))
									.executesNative((proxy, args) -> { Room room = (Room) args[0]; return room.canLeave() ? 1 : 0; }))));
	}

	public static int allowEnterGlobal(NativeProxyCommandSender proxy, Object[] args) {
		// Allow enter for every rooms
		boolean allow = (boolean) args[0];
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

	public static int allowLeaveGlobal(NativeProxyCommandSender proxy, Object[] args) {
		// Allow leave for every rooms
		boolean allow = (boolean) args[0];
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

	public static int allowEnterRoom(NativeProxyCommandSender proxy, Object[] args) {
		// Allow enter for the room
		boolean allow = (boolean) args[1];
		Room room = (Room) args[0];
		room.setAllowEnter(allow);

		if (allow) {
			Messages.Resources.CONFIG_ROOM_ENTER_ENABLED.params(room).send(proxy);
		} else {
			Messages.Resources.CONFIG_ROOM_ENTER_DISABLED.params(room).send(proxy);
		}

		return 1;
	}

	public static int allowLeaveRoom(NativeProxyCommandSender proxy, Object[] args) {
		// Allow leave for the room
		boolean allow = (boolean) args[1];
		Room room = (Room) args[0];
		room.setAllowLeave(allow);

		if (allow) {
			Messages.Resources.CONFIG_ROOM_LEAVE_ENABLED.params(room).send(proxy);
		} else {
			Messages.Resources.CONFIG_ROOM_LEAVE_DISABLED.params(room).send(proxy);
		}

		return 1;
	}
}
