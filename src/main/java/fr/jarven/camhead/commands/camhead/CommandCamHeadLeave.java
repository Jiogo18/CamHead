package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.spectate.CameraSpectator;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadLeave extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("leave")
			.executesNative((proxy, args) -> {
				CommandSender puppet = proxy.getCallee();
				assert puppet != null;
				if (puppet instanceof Player) {
					Player player = (Player) puppet;
					CameraSpectator spectator = CamHead.spectatorManager.getSpectator(player);
					Camera camera = null;
					if (spectator != null) camera = spectator.getCamera();
					if (CamHead.spectatorManager.leave(player)) {
						Messages.Resources.SPECTATE_LEAVE.params(camera, player).send(proxy);
						return 1;
					} else {
						Messages.Resources.SPECTATE_NOT_SPECTATING.params(player).sendFailure(proxy);
						return 0;
					}
				} else {
					Messages.Resources.SPECTATE_NOT_A_PLAYER.replace("%name%", puppet.getName()).sendFailure(proxy);
					return 0;
				}
			});
	}
}
