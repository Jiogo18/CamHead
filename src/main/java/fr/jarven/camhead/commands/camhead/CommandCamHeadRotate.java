package fr.jarven.camhead.commands.camhead;

import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.commands.arguments.DirectionArgument;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Screen;
import fr.jarven.camhead.task.CameraAnimator;
import fr.jarven.camhead.utils.Messages;
import fr.jarven.camhead.utils.YawBlockFace;

public class CommandCamHeadRotate extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("rotate")
			.then(generateCameraSelector(cameraArgument -> {
				return cameraArgument
					.then(new DirectionArgument("support", Camera.SUPPORT_DIRECTIONS)
							.then(new DirectionArgument("facing", Camera.ANIMATION_DIRECTIONS)
									.executes(this::rotateCameraParams)))
					.executesNative((proxy, args) -> { return rotateCameraYaw(proxy, (Camera) args[1]); });
			}))
			.then(generateScreenSelector(screenArgument -> {
				return screenArgument
					.then(new DirectionArgument("support", Screen.SUPPORT_DIRECTIONS)
							.then(new DirectionArgument("facing", Screen.FACING)
									.executes(this::rotateScreenParams)))
					.executesNative((proxy, args) -> { return rotateScreenYaw(proxy, (Screen) args[1]); });
			}));
	}

	private int rotateCameraParams(CommandSender sender, Object[] args) {
		Camera camera = (Camera) args[1];
		BlockFace support = (BlockFace) args[2];
		BlockFace facing = (BlockFace) args[3];
		camera.setSupportDirection(support);
		camera.setAnimationFace(facing);
		CameraAnimator.addCamera(camera); // update
		Messages.Resources.ROTATE_CAMERA_SUCCESS.params(camera).send(sender);
		return 1;
	}

	private int rotateCameraYaw(NativeProxyCommandSender proxy, Camera camera) {
		float yaw = proxy.getLocation().getYaw();
		float pitch = proxy.getLocation().getPitch();
		BlockFace support;
		if (pitch > 80)
			support = BlockFace.DOWN;
		else if (pitch < -80)
			support = BlockFace.UP;
		else
			support = YawBlockFace.yawToHorizontal90BlockFace(yaw);
		BlockFace animation = YawBlockFace.yawToHorizontal45BlockFace(yaw).getOppositeFace();

		camera.setSupportDirection(support);
		camera.setAnimationFace(animation);
		CameraAnimator.addCamera(camera); // update
		Messages.Resources.ROTATE_CAMERA_DETAILED
			.params(camera)
			.replace("%support%", camera.getSupportDirection().name())
			.replace("%facing%", camera.getAnimationDirection().name())
			.send(proxy);
		return 1;
	}

	private int rotateScreenParams(CommandSender sender, Object[] args) {
		Screen screen = (Screen) args[1];
		BlockFace support = (BlockFace) args[2];
		BlockFace facing = (BlockFace) args[3];
		screen.setSupportDirection(support);
		screen.setFacing(facing);
		Messages.Resources.ROTATE_SCREEN_SUCCESS.params(screen).send(sender);
		return 1;
	}

	private int rotateScreenYaw(NativeProxyCommandSender proxy, Screen screen) {
		float yaw = proxy.getLocation().getYaw();
		float pitch = proxy.getLocation().getPitch();
		BlockFace support;
		if (pitch > 80)
			support = BlockFace.DOWN;
		else if (pitch < -80)
			support = BlockFace.UP;
		else
			support = YawBlockFace.yawToHorizontal90BlockFace(yaw);
		BlockFace facing = YawBlockFace.yawToHorizontal90BlockFace(yaw);

		switch (screen.getSupportDirection()) {
			case DOWN:
			case UP:
				screen.setFacing(facing);
				if (!screen.hasBlockForSupport() && screen.isSolidBlock(support)) {
					screen.setSupportDirection(support);
				}
				break;
			default:
				screen.setFacing(facing);
				if (screen.isSolidBlock(support)) {
					screen.setSupportDirection(support);
				} else {
					if (screen.isSolidBlock(BlockFace.DOWN)) {
						screen.setSupportDirection(BlockFace.DOWN);
					} else if (screen.isSolidBlock(BlockFace.UP)) {
						screen.setSupportDirection(BlockFace.UP);
					} else {
						screen.setSupportDirection(support);
					}
				}
				break;
		}

		Messages.Resources.ROTATE_SCREEN_DETAILED
			.params(screen)
			.replace("%support%", screen.getSupportDirection().name())
			.replace("%facing%", screen.getFacingDirection().name())
			.send(proxy);
		return 1;
	}
}
