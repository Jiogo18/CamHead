package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.commands.SubCommandBuider;

public class CommandCamHeadTeleportTo extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("tpto")
			.then(generateBasicCameraSelector(1, (proxy, cam) -> tpto(proxy, cam.getLocation())))
			.then(generateBasicScreenSelector(1, (proxy, scr) -> tpto(proxy, scr.getLocation())))
			.then(generateBasicRoomSelector(0, (proxy, room) -> tpto(proxy, room.getLocation())));
	}

	private int tpto(NativeProxyCommandSender proxy, Location destination) {
		CommandSender puppet = proxy.getCallee();
		assert puppet != null && destination != null;
		if (puppet instanceof Entity) {
			((Entity) puppet).teleport(destination.clone().add(0.5, 0.5, 0.5));
			return 1;
		} else {
			sendFailureMessage(proxy, "Not an entity " + puppet + " " + puppet.getName());
			return 0;
		}
	}
}
