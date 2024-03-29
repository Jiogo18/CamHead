package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadTeleportTo extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("tpto")
			.then(generateBasicCameraSelector((proxy, cam) -> tpto(proxy, cam.getLocation())))
			.then(generateBasicScreenSelector((proxy, scr) -> tpto(proxy, scr.getLocation())))
			.then(generateBasicRoomSelector((proxy, room) -> tpto(proxy, room.getLocation())));
	}

	private int tpto(NativeProxyCommandSender proxy, Location destination) {
		CommandSender puppet = proxy.getCallee();
		assert puppet != null && destination != null;
		if (puppet instanceof Entity) {
			((Entity) puppet).teleport(destination.clone().add(0.5, 0.5, 0.5));
			return 1;
		} else {
			Messages.Resources.TELEPORT_NOT_AN_ENTITY.replace("%name%", puppet.getName()).sendFailure(proxy);
			return 0;
		}
	}
}
