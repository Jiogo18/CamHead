package fr.jarven.camhead.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.components.Room;

public class RoomArgument extends CustomArgument<Room, String> {
	public RoomArgument(String nodeName) {
		super(new StringArgument(nodeName), RoomArgument::parseRoom);
		replaceSuggestions(roomSuggestions);
	}

	private static ArgumentSuggestions roomSuggestions = (info, builder) -> {
		// List of room names
		for (Room room : CamHead.manager.getRooms()) {
			builder.suggest(room.getName());
		}
		return builder.buildFuture();
	};

	private static Room parseRoom(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String roomName = info.input();
		return CamHead.manager.getRoom(roomName).orElseThrow(() -> new CustomArgumentException(new MessageBuilder("camhead.argument.room.unknown").appendArgInput()));
	}

	public static Room getRoom(Object[] args, int argIndex) {
		assert args[argIndex] instanceof Room;
		return (Room) args[argIndex];
	}
}
