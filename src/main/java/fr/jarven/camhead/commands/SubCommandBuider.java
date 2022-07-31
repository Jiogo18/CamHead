package fr.jarven.camhead.commands;

import dev.jorel.commandapi.arguments.LiteralArgument;

public abstract class SubCommandBuider extends CommandTools {
	protected abstract LiteralArgument getArgumentTree();
}
