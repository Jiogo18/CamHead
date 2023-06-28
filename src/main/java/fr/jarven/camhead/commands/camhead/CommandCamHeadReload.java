package fr.jarven.camhead.commands.camhead;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadReload extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("reload")
			.then(roomArgument().executes((sender, args) -> {
				Room room = getRoom(args);
				CamHead.manager.reload(room);
				room = CamHead.manager.getRoom(room.getName()).orElseThrow(() -> new IllegalStateException("Room not found"));
				Messages.Resources.RELOAD_ROOM_SUCCESS.params(room).send(sender);
			}))
			.executes((sender, args) -> {
				Messages.Resources.RELOAD_RELOADING.send(sender);
				CamHead.getInstance().loadConfig();
				Messages.Resources.RELOAD_SUCCESS.replace("%roomCount%", String.valueOf(CamHead.manager.getRooms().size())).send(sender);
			});
	}
}
