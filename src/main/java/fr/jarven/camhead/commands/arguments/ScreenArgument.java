package fr.jarven.camhead.commands.arguments;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class ScreenArgument extends CustomArgument<Screen, String> {
	public ScreenArgument(String nodeName) {
		super(new StringArgument(nodeName), ScreenArgument::parseScreen);
		replaceSuggestions(screenSuggestions);
	}

	private static ArgumentSuggestions screenSuggestions = (info, builder) -> {
		// List of screen names
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room != null) {
			for (Screen screen : room.getScreens()) {
				builder.suggest(screen.getName());
			}
		}
		return builder.buildFuture();
	};

	private static Screen parseScreen(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String screenName = info.input();
		Room room = info.previousArgs().length > 0 ? (Room) info.previousArgs()[info.previousArgs().length - 1] : null;
		if (room == null) {
			throw Messages.createCustomArgumentException(info, Messages.Resources.ROOM_UNKNOWN);
		}
		return room.getScreen(screenName).orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.SCREEN_UNKNOWN.replace("%screen%", screenName)));
	}

	public static Screen getScreen(Object[] args, int argIndex) {
		return (Screen) args[argIndex];
	}
}
