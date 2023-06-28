package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.spectate.EnterResult;
import fr.jarven.camhead.spectate.LeaveResult;
import fr.jarven.camhead.utils.Messages;
import fr.jarven.camhead.utils.Messages.MessageBuilder;
import net.md_5.bungee.api.ChatMessageType;

public class CommandCamHeadSpectate extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("spectate")
			.then(cameraArgument(cameraArg -> cameraArg.executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Camera camera = getCamera(args);
				return spectateCamera(proxy, puppet, camera);
			})))
			.then(roomArgument().executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				Room room = getRoom(args);
				return spectateRoom(proxy, puppet, room);
			}))
			.executesNative((proxy, args) -> {
				Player puppet = getPlayer(proxy);
				if (puppet == null) return 0;
				return leaveSpectate(proxy, puppet);
			});
	}

	public static int sendMessageForEnterResult(CommandSender sender, EnterResult result, ChatMessageType position) {
		int status = 0;
		MessageBuilder builder = null;
		switch (result.getType()) {
			case SUCCESS_ENTER:
				builder = Messages.Resources.SPECTATE_ENTER_SUCCESS.params(result.getCamera());
				status = 1;
				break;
			case SUCCESS_CHANGE:
				builder = Messages.Resources.SPECTATE_ENTER_CHANGED.params(result.getCamera());
				status = 1;
				break;
			case SAME_CAMERA:
				builder = Messages.Resources.SPECTATE_ENTER_SAME_CAMERA.params(result.getCamera());
				break;
			case FAILED_UNKNOWN:
				builder = Messages.Resources.SPECTATE_ENTER_FAILED_UNKNOWN.params(result.getCamera());
				break;
			case NO_CAMERAS:
				builder = Messages.Resources.SPECTATE_ENTER_FAILED_NO_CAMERAS.params(result.getRoom());
				break;
			case NO_PERMISSION:
				builder = Messages.Resources.SPECTATE_ENTER_FAILED_NO_PERMISSION.params(result.getCamera());
				break;
			case NO_SEAT:
				builder = Messages.Resources.SPECTATE_ENTER_FAILED_NO_SEAT.params(result.getCamera());
				break;
			case ROOM_FULL:
				builder = Messages.Resources.SPECTATE_ENTER_FAILED_ROOM_FULL.params(result.getRoom());
				break;
		}

		if (builder == null) {
			throw new IllegalStateException("Builder is null");
		} else if (sender instanceof Player && position != null) {
			builder.send((Player) sender, position);
		} else {
			builder.send(sender);
		}
		return status;
	}

	public static int spectateCamera(CommandSender sender, @Nonnull Player player, @Nonnull Camera camera) {
		EnterResult result = CamHead.spectatorManager.enter(player, camera);
		return sendMessageForEnterResult(sender, result, null);
	}

	public static int spectateRoom(CommandSender sender, @Nonnull Player player, @Nonnull Room room) {
		EnterResult result = CamHead.spectatorManager.enter(player, room);
		return sendMessageForEnterResult(sender, result, null);
	}

	public static int leaveSpectate(CommandSender sender, Player player) {
		LeaveResult result = CamHead.spectatorManager.leave(player);

		switch (result.getType()) {
			case SUCCESS:
				Messages.Resources.SPECTATE_LEAVE_SUCCESS.params(result.getCamera()).send(sender);
				return 1;
			case ALREADY_LEAVING:
				Messages.Resources.SPECTATE_LEAVE_FAILED_ALREADY_LEAVING.sendFailure(sender);
				return 0;
			case FAILED_UNKNOWN:
				Messages.Resources.SPECTATE_LEAVE_FAILED_UNKNOWN.params(result.getCamera()).sendFailure(sender);
				return 0;
			case NOT_SPECTATING:
				Messages.Resources.SPECTATE_LEAVE_FAILED_NOT_SPECTATING.sendFailure(sender);
				return 0;
			case NO_PERMISSION:
				Messages.Resources.SPECTATE_LEAVE_FAILED_NO_PERMISSION.params(result.getCamera()).sendFailure(sender);
				return 0;
		}
		throw new IllegalStateException("Unknown LeaveResult type: " + result.getType());
	}

	public static Player getPlayer(NativeProxyCommandSender proxy) {
		CommandSender puppet = proxy.getCallee();
		if (puppet instanceof Player) {
			return (Player) puppet;
		} else {
			Messages.Resources.SPECTATE_NOT_A_PLAYER.replace("%name%", puppet.getName()).sendFailure(proxy);
			return null;
		}
	}
}
