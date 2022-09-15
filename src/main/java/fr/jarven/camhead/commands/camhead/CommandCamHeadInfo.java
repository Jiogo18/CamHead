package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.spectate.CameraSpectator;
import fr.jarven.camhead.task.SaveTask;
import fr.jarven.camhead.utils.Messages;
import fr.jarven.camhead.utils.Messages.MessageBuilder;

public class CommandCamHeadInfo extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("info")
			.then(generateBasicCameraSelector(1, (proxy, cam) -> infoCamera(proxy, cam)))
			.then(generateBasicScreenSelector(1, (proxy, scr) -> infoScreen(proxy, scr)))
			.then(generateBasicRoomSelector(0, (proxy, room) -> infoRoom(proxy, room)))
			.then(literal("player").then((new PlayerArgument("player1")).executes((sender, args) -> { return infoPlayer(sender, (Player) args[0]); })));
	}

	public static MessageBuilder infoCamera(Camera camera) {
		return Messages.Resources.INFO_CAMERA
			.params(camera, camera.getRoom(), camera.getLocation())
			.replace("%supportDirection%", camera.getSupportDirection().name())
			.replace("%animationDirection%", camera.getAnimationDirection().name())
			.replace("%cameraman%", camera.getCameramanUUID().toString() + " (" + (camera.getCameraman(true).isPresent() ? "ok" : "unknown") + ")")
			.replace("%seat%", camera.getSeatUUID().toString() + " (" + (camera.getCameraSeat(true).isPresent() ? "ok" : "unknown") + ")");
	}

	public static MessageBuilder infoScreen(Screen screen) {
		return Messages.Resources.INFO_SCREEN
			.params(screen, screen.getRoom(), screen.getLocation())
			.replace("%supportDirection%", screen.getSupportDirection().name())
			.replace("%facingDirection%", screen.getFacingDirection().name());
	}

	private int infoCamera(NativeProxyCommandSender proxy, Camera camera) {
		infoCamera(camera).send(proxy);
		return 1;
	}

	private int infoScreen(NativeProxyCommandSender proxy, Screen screen) {
		infoScreen(screen).send(proxy);
		return 1;
	}

	private int infoRoom(NativeProxyCommandSender proxy, Room room) {
		MessageBuilder saveTime;
		if (room.getSaveTimestamp() > 0) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Messages.Resources.DATE_TIME.getBuilder().build(proxy));
			ZoneOffset zone = OffsetDateTime.now().getOffset();
			LocalDateTime date = LocalDateTime.ofEpochSecond(room.getSaveTimestamp() / 1000, (int) ((room.getSaveTimestamp() % 1000) * 1e6), zone);
			saveTime = Messages.Resources.INFO_ROOM_SAVE_TIME
					   .replace("%timestamp%", String.valueOf(room.getSaveTimestamp()))
					   .replace("%saveTime%", date.format(formatter))
					   .replace("%timezone%", "UTC" + zone.getId());
		} else {
			saveTime = Messages.Resources.INFO_ROOM_SAVE_NEVER.getBuilder();
		}

		MessageBuilder saving;
		if (SaveTask.willSaveSoon(room)) {
			saving = Messages.Resources.INFO_ROOM_SAVING.replace("%saveTime%", String.valueOf(SaveTask.saveDelay));
		} else {
			saving = Messages.Resources.INFO_ROOM_NOT_SAVING.getBuilder();
		}

		Messages.Resources.INFO_ROOM
			.params(room, room.getLocation())
			.replace("%cameraCount%", String.valueOf(room.getCameras().size()))
			.replace("%screenCount%", String.valueOf(room.getScreens().size()))
			.replace("%playerLimit%", String.valueOf(room.getPlayerLimit()))
			.replace("%saveTime%", saveTime)
			.replace("%saving%", saving)
			.send(proxy);

		return 1;
	}

	private int infoPlayer(CommandSender sender, Player player) {
		CameraSpectator spectator = CamHead.spectatorManager.getSpectator(player);
		String roomName = "None";
		String cameraName = "None";
		String leaving = "false";
		if (spectator != null) {
			roomName = spectator.getRoom().getName();
			cameraName = spectator.getCamera().getName();
			leaving = String.valueOf(spectator.isLeaving());
		}

		Messages.Resources.INFO_PLAYER
			.params(player, player.getLocation())
			.replace("%room%", roomName)
			.replace("%camera%", cameraName)
			.replace("%leaving%", leaving)
			.send(sender);

		return spectator != null ? 2 : 1;
	}
}
