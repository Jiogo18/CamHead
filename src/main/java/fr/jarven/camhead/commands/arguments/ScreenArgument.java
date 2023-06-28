package fr.jarven.camhead.commands.arguments;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.utils.Messages;

public class ScreenArgument extends CustomArgument<Screen, String> {
	public ScreenArgument(String nodeName) {
		super(new StringArgument(nodeName), ScreenArgument::parseScreen);
		replaceSuggestions(screenSuggestions);
	}

	private static ArgumentSuggestions<CommandSender> screenSuggestions = (info, builder) -> {
		String current = info.currentArg().toLowerCase();
		// List of screen names
		Room room = (Room) info.previousArgs().get("room_name");
		if (room != null) {
			for (Screen screen : room.getScreens()) {
				if (screen.getName().toLowerCase().startsWith(current)) {
					builder.suggest(screen.getName());
				}
			}
		}
		return builder.buildFuture();
	};

	private static Screen parseScreen(CustomArgumentInfo<String> info) throws CustomArgumentException {
		String screenName = info.input();
		Room room = (Room) info.previousArgs().get("room_name");
		if (room == null) {
			throw Messages.createCustomArgumentException(info, Messages.Resources.ROOM_UNKNOWN);
		}
		return room.getScreen(screenName).orElseThrow(() -> Messages.createCustomArgumentException(info, Messages.Resources.SCREEN_UNKNOWN.replace("%screen%", screenName)));
	}

	public static Screen getScreen(CommandArguments args, String nodeName) {
		return (Screen) args.get(nodeName);
	}
}
