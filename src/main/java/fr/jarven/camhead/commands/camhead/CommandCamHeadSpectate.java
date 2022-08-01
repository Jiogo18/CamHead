package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.spectate.CameraSpectator;
import fr.jarven.camhead.utils.Messages;

public class CommandCamHeadSpectate extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("spectate")
			.then(cameraArgument(cameraArg -> cameraArg.executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Camera camera = getCamera(args, 1);
				return spectateCamera(proxy, puppet, camera);
			})))
			.then(roomArgument().executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Room room = getRoom(args, 0);
				return spectateRoom(proxy, puppet, room);
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

	private int spectateCamera(CommandSender sender, Player player, Camera camera) {
		boolean wasSpectating = CamHead.spectatorManager.getSpectator(player) != null;
		if (CamHead.spectatorManager.enter(player, camera)) {
			if (wasSpectating) {
				Messages.Resources.SPECTATE_ENTER.params(camera).send(sender);
			} else {
				Messages.Resources.SPECTATE_CHANGE.params(camera).send(sender);
			}
			return 1;
		} else {
			if (wasSpectating) {
				Messages.Resources.SPECTATE_SAME_CAMERA.params(camera).sendFailure(sender);
			} else {
				Messages.Resources.SPECTATE_FAILED.params(camera).sendFailure(sender);
			}
			return 0;
		}
	}

	private int spectateRoom(CommandSender sender, Player player, Room room) {
		boolean wasSpectating = CamHead.spectatorManager.getSpectator(player) != null;
		if (CamHead.spectatorManager.enter(player, room)) {
			CameraSpectator spectator = CamHead.spectatorManager.getSpectator(player);
			if (wasSpectating) {
				Messages.Resources.SPECTATE_ENTER.params(spectator.getCamera()).send(sender);
			} else {
				Messages.Resources.SPECTATE_CHANGE.params(spectator.getCamera()).send(sender);
			}
			return 1;
		} else {
			if (wasSpectating) {
				if (room.getCameras().isEmpty()) {
					Messages.Resources.SPECTATE_NO_CAMERAS.params(room).sendFailure(sender);
				} else {
					Messages.Resources.SPECTATE_SAME_ROOM.params(room).sendFailure(sender);
				}
			} else {
				Messages.Resources.SPECTATE_FAILED.params(room).sendFailure(sender);
			}
			return 0;
		}
	}

	private Player getPlayer(NativeProxyCommandSender proxy) {
		CommandSender puppet = proxy.getCallee();
		if (puppet instanceof Player) {
			return (Player) puppet;
		} else {
			Messages.Resources.SPECTATE_NOT_A_PLAYER.replace("%name%", puppet.getName()).sendFailure(proxy);
			return null;
		}
	}
}
