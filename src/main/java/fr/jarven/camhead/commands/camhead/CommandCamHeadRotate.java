package fr.jarven.camhead.commands.camhead;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import dev.jorel.commandapi.arguments.LiteralArgument;
import fr.jarven.camhead.commands.SubCommandBuider;
import fr.jarven.camhead.commands.arguments.DirectionArgument;
import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Screen;

public class CommandCamHeadRotate extends SubCommandBuider {
	@Override
	public LiteralArgument getArgumentTree() {
		return (LiteralArgument) literal("rotate")
			.then(generateCameraSelector(cameraArgument -> {
				return cameraArgument
					.then(new DirectionArgument("support", Camera.SUPPORT_DIRECTIONS)
							.then(new DirectionArgument("facing", Camera.FACING)
									.executes((sender, args) -> {
										Camera camera = (Camera) args[1];
										BlockFace support = (BlockFace) args[2];
										BlockFace facing = (BlockFace) args[3];
										camera.setSupportDirection(support);
										camera.setAnimationFace(facing);
										sender.sendMessage("Camera rotated");
										return 1;
									})))
					.executesNative((proxy, args) -> {
						Camera camera = (Camera) args[1];
						BlockFace support = getHorizontal90Facing(proxy.getLocation()).getOppositeFace();
						BlockFace facing = getHorizontal45Facing(proxy.getLocation()).getOppositeFace();
						camera.setSupportDirection(support);
						camera.setAnimationFace(facing);
						proxy.sendMessage("Camera rotated: support=" + camera.getSupportDirection().name() + " facing=" + camera.getAnimationDirection().name());
						return 1;
					});
			}))
			.then(generateScreenSelector(screenArgument -> {
				return screenArgument
					.then(new DirectionArgument("support", Screen.SUPPORT_DIRECTIONS)
							.then(new DirectionArgument("facing", Screen.FACING)
									.executes((sender, args) -> {
										Screen screen = (Screen) args[1];
										BlockFace support = (BlockFace) args[2];
										BlockFace facing = (BlockFace) args[3];
										screen.setSupportDirection(support);
										screen.setFacing(facing);
										sender.sendMessage("Screen rotated");
										return 1;
									})))
					.executesNative((proxy, args) -> {
						Screen screen = (Screen) args[1];
						BlockFace support = getHorizontal90Facing(proxy.getLocation()).getOppositeFace();
						BlockFace facing = getHorizontal90Facing(proxy.getLocation()).getOppositeFace();
						proxy.sendMessage("hasBlockForSupport: " + screen.hasBlockForSupport());
						proxy.sendMessage("isSolidBlock: " + screen.isSolidBlock(support));
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

						proxy.sendMessage("Screen rotated: support=" + screen.getSupportDirection().name() + " facing=" + screen.getFacingDirection().name());
						return 1;
					});
			}));
	}

	private BlockFace getHorizontal90Facing(Location direction) {
		float pitch = direction.getYaw() % 360;
		if (pitch < 0) {
			pitch += 360;
		}
		assert pitch >= 0 && pitch < 360;
		if (pitch < 45) {
			return BlockFace.NORTH;
		} else if (pitch < 135) {
			return BlockFace.EAST;
		} else if (pitch < 225) {
			return BlockFace.SOUTH;
		} else if (pitch < 315) {
			return BlockFace.WEST;
		} else {
			return BlockFace.NORTH;
		}
	}

	private BlockFace getHorizontal45Facing(Location direction) {
		float pitch = direction.getYaw() % 360;
		if (pitch < 0) {
			pitch += 360;
		}
		assert pitch >= 0 && pitch < 360;
		if (pitch < 22.5) { // 22.5
			return BlockFace.NORTH_EAST;
		} else if (pitch < 67.5) { // 22.5 + 45
			return BlockFace.EAST;
		} else if (pitch < 112.5) { // 22.5 + 90
			return BlockFace.SOUTH_EAST;
		} else if (pitch < 157.5) { // 22.5 + 135
			return BlockFace.SOUTH;
		} else if (pitch < 202.5) { // 22.5 + 180
			return BlockFace.SOUTH_WEST;
		} else if (pitch < 247.5) { // 22.5 + 225
			return BlockFace.WEST;
		} else if (pitch < 292.5) { // 22.5 + 270
			return BlockFace.NORTH_WEST;
		} else if (pitch < 337.5) { // 22.5 + 315
			return BlockFace.NORTH;
		} else {
			return BlockFace.NORTH_EAST;
		}
	}
}
