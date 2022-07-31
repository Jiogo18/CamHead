package fr.jarven.camhead.commands.arguments;

import org.bukkit.block.BlockFace;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class DirectionArgument extends CustomArgument<BlockFace, String> {
	public DirectionArgument(String nodeName, BlockFace[] directions) {
		super(new StringArgument(nodeName), (info) -> {
			BlockFace face = BlockFace.valueOf(info.input().toUpperCase());
			if (face == null) {
				throw new CustomArgumentException(new MessageBuilder("camhead.argument.direction.unknown").appendArgInput());
			}
			for (BlockFace direction : directions) {
				if (direction == face) {
					return face;
				}
			}
			throw new CustomArgumentException(new MessageBuilder("camhead.argument.direction.invalid").appendArgInput());
		});
		replaceSuggestions((info, builder) -> {
			for (BlockFace direction : directions) {
				builder.suggest(direction.name());
			}
			return builder.buildFuture();
		});
	}
}
