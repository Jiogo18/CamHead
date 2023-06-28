package fr.jarven.camhead.commands.arguments;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.utils.Messages;

public class RoomArgument extends CustomArgument<Room, String> {
	public RoomArgument(String nodeName) {
		super(new StringArgument(nodeName), RoomArgument::parseRoom);
		replaceSuggestions(roomSuggestions);
	}

	private static ArgumentSuggestions<CommandSender> roomSuggestions = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// List of room names
		for (Room room : CamHead.manager.getRooms()) {
			if (room.getName().toLowerCase().startsWith(current)) {
				builder.suggest(room.getName());
			}
		}
		return builder.buildFuture();
	};

	private static Room parseRoom(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String roomName = info.input();
		return CamHead.manager.getRoom(roomName).orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.ROOM_UNKNOWN.replace("%room%", roomName)));
	}

	public static Room getRoom(CommandArguments args, String nodeName) {
		return (Room) args.get(nodeName);
	}
}
