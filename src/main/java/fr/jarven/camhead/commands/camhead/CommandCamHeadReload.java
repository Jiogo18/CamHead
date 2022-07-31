package fr.jarven.camhead.commands.camhead;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Room;

public class CommandCamHeadReload extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("reload")
			.then(roomArgument().executes((sender, args) -> {
				Room room = getRoom(args, 0);
				CamHead.manager.reload(room);
				room = CamHead.manager.getRoom(room.getName()).orElseThrow();
				sender.sendMessage("Room " + room.getName() + " reloaded.");
			}))
			.executes((sender, args) -> {
				sender.sendMessage("Reloading...");
				CamHead.getInstance().loadConfig();
				sender.sendMessage("Reloaded with " + CamHead.manager.getRooms().size() + " rooms.");
			});
	}
}
