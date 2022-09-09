package fr.jarven.camhead.commands.camhead;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;

public class CommandCamHeadLeave extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("leave")
			.executesNative((proxy, args) -> {
				Player puppet = CommandCamHeadSpectate.getPlayer(proxy);
				if (puppet == null) return 0;
				return CommandCamHeadSpectate.leaveSpectate(proxy, puppet);
			});
	}
}
