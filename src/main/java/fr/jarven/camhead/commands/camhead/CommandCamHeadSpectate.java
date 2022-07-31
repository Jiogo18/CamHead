package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;

public class CommandCamHeadSpectate extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("spectate")
			.then(cameraArgument(cameraArg -> cameraArg.executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Camera camera = getCamera(args, 1);
				if (CamHead.spectatorManager.enter(puppet, camera)) {
					proxy.sendMessage("You are now spectating camera " + camera.getName());
					return 1;
				} else {
					proxy.sendMessage("You are not allowed to spectate camera " + camera.getName());
					return 0;
				}
			})))
			.then(roomArgument().executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Room room = getRoom(args, 0);
				if (CamHead.spectatorManager.enter(puppet, room)) {
					proxy.sendMessage("You are now spectating room " + room.getName());
					return 1;
				} else {
					proxy.sendMessage("You are not allowed to spectate room " + room.getName());
					return 0;
				}
			}))
			.executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				if (CamHead.spectatorManager.leave(puppet)) {
					proxy.sendMessage("You are no longer spectating");
					return 1;
				} else {
					proxy.sendMessage("You are not spectating");
					return 0;
				}
			});
	}

	private Player getPlayer(NativeProxyCommandSender proxy) {
		CommandSender puppet = proxy.getCallee();
		if (puppet instanceof Player) {
			return (Player) puppet;
		} else {
			sendFailureMessage(proxy, "Not a player " + puppet.getName());
			return null;
		}
	}
}
