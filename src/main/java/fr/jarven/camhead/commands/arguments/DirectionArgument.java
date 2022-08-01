package fr.jarven.camhead.commands.arguments;

import org.bukkit.block.BlockFace;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import fr.jarven.camhead.utils.Messages;

public class DirectionArgument extends CustomArgument<BlockFace, String> {
	public DirectionArgument(String nodeName, BlockFace[] directions) {
		super(new StringArgument(nodeName), info -> {
			BlockFace face = BlockFace.valueOf(info.input().toUpperCase());
			if (face == null) {
				throw Messages.createCustomArgumentException(info, Messages.Resources.DIRECTION_UNKNOWN.replace("%input%", info.input()));
			}
			for (BlockFace direction : directions) {
				if (direction == face) {
					return face;
				}
			}
			throw Messages.createCustomArgumentException(info, Messages.Resources.DIRECTION_INVALID.replace("%input%", info.input()));
		});
		replaceSuggestions((info, builder) -> {
			for (BlockFace direction : directions) {
				builder.suggest(direction.name());
			}
			return builder.buildFuture();
		});
	}
}
