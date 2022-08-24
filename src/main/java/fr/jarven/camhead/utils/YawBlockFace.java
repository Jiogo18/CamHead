package fr.jarven.camhead.utils;

import org.bukkit.block.BlockFace;

public interface YawBlockFace {
	public static float blockFaceToYaw(BlockFace direction) {
		switch (direction) {
			case NORTH:
				return 180;
			case NORTH_EAST:
				return 225;
			case EAST:
				return 270;
			case SOUTH_EAST:
				return 315;
			case SOUTH:
				return 0;
			case SOUTH_WEST:
				return 45;
			case WEST:
				return 90;
			case NORTH_WEST:
				return 135;
			default:
				return 0;
		}
	}

	public static BlockFace yawToHorizontal90BlockFace(float yaw) {
		yaw = yaw % 360;
		if (yaw < 0) yaw += 360;

		if (yaw < 45) {
			return BlockFace.SOUTH; // 0
		} else if (yaw < 135) {
			return BlockFace.WEST; // 90
		} else if (yaw < 225) {
			return BlockFace.NORTH; // 180
		} else if (yaw < 315) {
			return BlockFace.EAST; // 270
		} else {
			return BlockFace.SOUTH; // 0
		}
	}

	public static BlockFace yawToHorizontal45BlockFace(float yaw) {
		yaw = yaw % 360;
		if (yaw < 0) yaw += 360;

		if (yaw < 22.5) { // 22.5 + 0
			return BlockFace.SOUTH;
		} else if (yaw < 67.5) { // 22.5 + 45
			return BlockFace.SOUTH_WEST;
		} else if (yaw < 112.5) { // 22.5 + 90
			return BlockFace.WEST;
		} else if (yaw < 157.5) { // 22.5 + 135
			return BlockFace.NORTH_WEST;
		} else if (yaw < 202.5) { // 22.5 + 180
			return BlockFace.NORTH;
		} else if (yaw < 247.5) { // 22.5 + 225
			return BlockFace.NORTH_EAST;
		} else if (yaw < 292.5) { // 22.5 + 270
			return BlockFace.EAST;
		} else if (yaw < 337.5) { // 22.5 + 315
			return BlockFace.SOUTH_EAST;
		} else {
			return BlockFace.SOUTH; // 0
		}
	}
}
