package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;

public class CommandCamHeadLeave extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("leave")
			.executesNative((proxy, args) -> {
				CommandSender puppet = proxy.getCallee();
				assert puppet != null;
				if (puppet instanceof Player) {
					if (CamHead.spectatorManager.leave((Player) puppet)) {
						proxy.sendMessage("You are no longer spectating");
						return 1;
					} else {
						proxy.sendMessage("You are not spectating");
						return 0;
					}
				} else {
					sendFailureMessage(proxy, "Not a player " + puppet.getName());
					return 0;
				}
			});
	}
}
