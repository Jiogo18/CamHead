package fr.jarven.camhead.components;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.lib.skullcreator.SkullCreator;

public class Screen implements ComponentBase, Comparable<Screen>, ConfigurationSerializable {
	private static boolean replaceOnReload = false;
	public static Material MATERIAL_DOWN_SUPPORT = Material.PLAYER_HEAD;
	public static Material MATERIAL_WALL_SUPPORT = Material.PLAYER_WALL_HEAD;
	public static Material MATERIAL_UP_SUPPORT = Material.PLAYER_HEAD;
	public static String headTextureBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzRhZDg4OTNiNDdkMDU5YzJlNGZmYmIwYTk3YjU1YTVjOWEwNTQxN2M5MDNjY2U1MTEyMjgxNjE0NmQ2MDc2MCJ9fX0=";
	public static final BlockFace[] SUPPORT_DIRECTIONS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};
	public static final BlockFace[] FACING = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	private Room room;
	private final String name;
	private Location location;
	private BlockFace supportDirection;
	private BlockFace facingDirection;

	protected Screen(Room room, String name, Location location) {
		RoomManager.assertNameStandard(name);
		this.room = room;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.supportDirection = guessSupportDirection(location);
		this.facingDirection = guessFacingDirection(location);
		replace();
	}

	private Screen(String name, Location location, BlockFace supportDirection, BlockFace facingDirection) {
		RoomManager.assertNameStandard(name);
		this.room = null;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.supportDirection = supportDirection;
		this.facingDirection = facingDirection;
		// replace later when room is set (for makeDirty)
	}

	@Override
	public String getName() {
		RoomManager.assertNameStandard(name);
		return name;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		location = location.getBlock().getLocation();
		if (!this.location.equals(location)) {
			removeInternal();
			this.location = location;
			replace();
			makeDirty();
		}
	}

	public boolean teleport(Location destination) {
		if (location.equals(destination)) {
			return false;
		}
		location = destination;
		makeDirty();
		return true;
	}

	public Room getRoom() {
		return room;
	}

	protected void setRoom(Room room) {
		this.room = room;
		if (replaceOnReload) replace();
	}

	void makeDirty() {
		room.makeDirty();
	}

	@Override
	public int compareTo(Screen c) {
		return name.compareTo(c.name);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Screen) {
			return (room == null || room.equals(((Screen) o).getRoom())) && name.equals(((Screen) o).name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((room != null ? room.getName() : "") + " " + name).hashCode();
	}

	@Override
	public boolean remove() {
		return room.removeScreen(this);
	}

	protected void removeInternal() {
		location.getBlock().setType(Material.AIR);
	}

	public boolean isAtLocation(Location location) {
		return this.location.getWorld().equals(location.getWorld())
			&& this.location.getBlockX() == location.getBlockX()
			&& this.location.getBlockY() == location.getBlockY()
			&& this.location.getBlockZ() == location.getBlockZ();
	}

	public void replace() {
		Block block = location.getBlock();
		Material supportType;
		switch (supportDirection) {
			case DOWN:
				supportType = MATERIAL_DOWN_SUPPORT;
				break;
			case UP:
				supportType = MATERIAL_UP_SUPPORT;
				break;
			default:
				supportType = MATERIAL_WALL_SUPPORT;
				break;
		}
		if (block.getType() != supportType) {
			block.setType(supportType);
		}
		if (block.getBlockData() instanceof Directional) {
			Directional data = (Directional) block.getBlockData();
			data.setFacing(supportDirection.getOppositeFace());
			block.setBlockData(data);
		}
		if (block.getState() instanceof Skull) {
			SkullCreator.blockWithBase64(block, headTextureBase64, false);
		}
		if (block.getBlockData() instanceof Rotatable) {
			Rotatable blockData = (Rotatable) block.getBlockData();
			blockData.setRotation(facingDirection);
			block.setBlockData(blockData);
		}
		if (block.getBlockData() instanceof Wall) {
			Wall blockData = (Wall) block.getBlockData();
			switch (supportDirection) {
				case NORTH:
					blockData.setHeight(BlockFace.NORTH, Height.LOW);
					break;
				case EAST:
					blockData.setHeight(BlockFace.EAST, Height.LOW);
					break;
				case SOUTH:
					blockData.setHeight(BlockFace.SOUTH, Height.LOW);
					break;
				case WEST:
					blockData.setHeight(BlockFace.WEST, Height.LOW);
					break;
				default:
					break;
			}
			block.setBlockData(blockData);
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<>();
		ser.put("name", name);
		ser.put("location", location);
		ser.put("supportDirection", supportDirection.toString());
		ser.put("facingDirection", facingDirection.toString());
		return ser;
	}

	public static Screen deserialize(Map<String, Object> args) {
		String name = String.valueOf(args.get("name"));
		Location location = (Location) args.get("location");
		String supportDirectionString = String.valueOf(args.get("supportDirection"));
		String facingDirectionString = String.valueOf(args.get("facingDirection"));
		BlockFace supportDirection = supportDirectionString != null ? BlockFace.valueOf(supportDirectionString) : guessSupportDirection(location);
		BlockFace facingDirection = facingDirectionString != null ? BlockFace.valueOf(facingDirectionString) : guessFacingDirection(location);
		return new Screen(name, location, supportDirection, facingDirection);
	}

	protected void updateWith(Screen screen) {
		setLocation(screen.getLocation());
	}

	private static BlockFace guessSupportDirection(Location location) {
		for (BlockFace direction : SUPPORT_DIRECTIONS) {
			if (location.getBlock().getRelative(direction).getType().isSolid()) {
				return direction;
			}
		}
		return SUPPORT_DIRECTIONS.length > 0 ? SUPPORT_DIRECTIONS[0] : null;
	}

	private static BlockFace guessFacingDirection(Location location) {
		BlockFace[] directions = FACING;
		List<BlockFace> facesFree = new ArrayList<>();
		List<BlockFace> facesSolid = new ArrayList<>();
		for (int i = 0; i < directions.length; i++) {
			if (!location.getBlock().getRelative(directions[i]).getType().isSolid()) {
				facesFree.add(directions[i]);
			} else {
				facesSolid.add(directions[i]);
			}
		}
		if (facesSolid.size() == 1) {
			return facesSolid.get(0).getOppositeFace();
		} else if (!facesFree.isEmpty()) {
			return facesFree.get(0);
		} else {
			return BlockFace.NORTH;
		}
	}

	public void setSupportDirection(BlockFace support) {
		if (supportDirection != support) {
			supportDirection = support;
			replace();
			makeDirty();
		}
	}

	public void setFacing(BlockFace facing) {
		if (facingDirection != facing) {
			facingDirection = facing;
			replace();
			makeDirty();
		}
	}

	public BlockFace getSupportDirection() {
		return supportDirection;
	}

	public boolean isSolidBlock(BlockFace face) {
		return location.getBlock().getRelative(face).getType().isSolid();
	}

	public boolean hasBlockForSupport() {
		return isSolidBlock(supportDirection);
	}

	public BlockFace getFacingDirection() {
		return facingDirection;
	}

	public static void loadConfig(YamlConfiguration config) {
		MATERIAL_DOWN_SUPPORT = Material.getMaterial(config.getString("screen.materials.downSupport", "PLAYER_HEAD").toUpperCase());
		MATERIAL_UP_SUPPORT = Material.getMaterial(config.getString("screen.materials.upSupport", "PLAYER_WALL_HEAD").toUpperCase());
		MATERIAL_WALL_SUPPORT = Material.getMaterial(config.getString("screen.materials.wallSupport", "PLAYER_HEAD").toUpperCase());
		if (MATERIAL_DOWN_SUPPORT == null) {
			CamHead.LOGGER.warning("Invalid material for screen support: " + config.getString("screen.materials.downSupport"));
			MATERIAL_DOWN_SUPPORT = Material.PLAYER_HEAD;
		}
		if (MATERIAL_UP_SUPPORT == null) {
			CamHead.LOGGER.warning("Invalid material for screen support: " + config.getString("screen.materials.upSupport"));
			MATERIAL_UP_SUPPORT = Material.PLAYER_HEAD;
		}
		if (MATERIAL_WALL_SUPPORT == null) {
			CamHead.LOGGER.warning("Invalid material for screen support: " + config.getString("screen.materials.wallSupport"));
			MATERIAL_WALL_SUPPORT = Material.PLAYER_WALL_HEAD;
		}
		headTextureBase64 = config.getString("screen.materials.headTextureBase64", null);
		replaceOnReload = config.getBoolean("replaceOnReload", false);
	}
}
