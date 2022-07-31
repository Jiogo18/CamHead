package fr.jarven.camhead.commands.camhead;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.spectate.CameraSpectator;
import fr.jarven.camhead.task.SaveTask;

public class CommandCamHeadInfo extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("info")
			.then(generateBasicCameraSelector(1, (proxy, cam) -> infoCamera(proxy, cam)))
			.then(generateBasicScreenSelector(1, (proxy, scr) -> infoScreen(proxy, scr)))
			.then(generateBasicRoomSelector(0, (proxy, room) -> infoRoom(proxy, room)))
			.then(literal("player").then((new PlayerArgument("player1")).executes((sender, args) -> { return infoPlayer(sender, (Player) args[0]); })));
	}

	private int infoCamera(CommandSender sender, Camera camera) {
		StringBuilder sb = new StringBuilder();
		sb.append("Camera ").append(camera.getName());
		sb.append("\n- Location: ").append(getLocationString(camera.getLocation()));
		sb.append("\n- Room: ").append(camera.getRoom().getName());
		sb.append("\n- Support direction: ").append(camera.getSupportDirection().name());
		sb.append("\n- Animation direction: ").append(camera.getAnimationDirection().name());
		sendMessage(sender, sb.toString());
		return 1;
	}

	private int infoScreen(CommandSender sender, Screen screen) {
		StringBuilder sb = new StringBuilder();
		sb.append("Screen ").append(screen.getName());
		sb.append("\n- Location: ").append(getLocationString(screen.getLocation()));
		sb.append("\n- Room: ").append(screen.getRoom().getName());
		sb.append("\n- Support direction: ").append(screen.getSupportDirection().name());
		sb.append("\n- Facing direction: ").append(screen.getFacingDirection().name());
		sendMessage(sender, sb.toString());
		return 1;
	}

	private int infoRoom(CommandSender sender, Room room) {
		StringBuilder sb = new StringBuilder();
		sb.append("Room ").append(room.getName()).append(":");
		sb.append("\n- Location: ").append(getLocationString(room.getLocation()));
		sb.append("\n- Cameras: ").append(room.getCameras().size());
		sb.append("\n- Screens: ").append(room.getScreens().size());
		if (room.getSaveTimestamp() > 0) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ");
			ZoneOffset zone = OffsetDateTime.now().getOffset();
			LocalDateTime date = LocalDateTime.ofEpochSecond(room.getSaveTimestamp() / 1000, (int) ((room.getSaveTimestamp() % 1000) * 1e6), zone);
			sb.append("\n- Save: ").append(date.format(formatter)).append("(UTC").append(zone.getId()).append(")");
		} else {
			sb.append("\n- Save: Never");
		}
		if (SaveTask.willSaveSoon(room)) {
			sb.append("\n- Save in less than ").append(SaveTask.saveDelay).append(" s");
		}
		sendMessage(sender, sb.toString());
		return 1;
	}

	private int infoPlayer(CommandSender sender, Player player) {
		StringBuilder sb = new StringBuilder();
		sb.append("Player ").append(player.getName()).append(":");
		sb.append("\n- Location: ").append(getLocationString(player.getLocation()));
		CameraSpectator spectator = CamHead.spectatorManager.getSpectator(player);
		if (spectator != null) {
			sb.append("\n- In Room: ").append(spectator.getRoom().getName());
			sb.append("\n- In Camera: ").append(spectator.getCamera().getName());
			sb.append("\n- Leaving: ").append(spectator.isLeaving());
			sendMessage(sender, sb.toString());
			return 2;
		} else {
			sb.append("\n- In Camera: no");
			sendMessage(sender, sb.toString());
			return 1;
		}
	}
}
