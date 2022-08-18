package fr.jarven.camhead.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.jarven.camhead.task.CameraAnimator;

public class Camera implements ComponentBase, Comparable<Camera>, ConfigurationSerializable {
	public static Material MATERIAL_SUPPORT = Material.END_ROD;
	public static Material MATERIAL_CAMERA = Material.END_ROD;
	public static int MATERIAL_CAMERA_CUSTOM_MODEL_DATA = 0;
	public static final BlockFace[] SUPPORT_DIRECTIONS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
	public static final BlockFace[] FACING = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
	private Room room;
	private final String name;
	private Location location;
	private Location tpLocation;
	private BlockFace supportDirection;
	private BlockFace animationDirection;
	private ArmorStand cameraman = null;
	private ArmorStand seat = null;

	protected Camera(Room room, String name, Location location) {
		RoomManager.assertNameStandard(name);
		this.room = room;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.tpLocation = location.clone().add(0.5, -0.5, 0.5);
		this.supportDirection = guessSupportDirection(location);
		this.animationDirection = guessAnimationDirection(location);
		replace();
		CameraAnimator.addCamera(this);
	}

	private Camera(String name, Location location, BlockFace supportDirection, BlockFace animationDirection, ArmorStand cameraman, ArmorStand seat) {
		RoomManager.assertNameStandard(name);
		this.room = null;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.tpLocation = location.clone().add(0.5, -0.5, 0.5);
		this.supportDirection = supportDirection;
		this.animationDirection = animationDirection;
		this.cameraman = cameraman;
		this.seat = seat;
		// replace latter with the room
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	public Location getTpLocation() {
		return tpLocation;
	}

	@Override
	public void setLocation(Location location) {
		location = location.getBlock().getLocation();
		if (!this.location.equals(location)) {
			removeInternal();
			this.location = location;
			this.tpLocation = location.clone().add(0.5, -0.5, 0.5);
			replace();
			makeDirty();
		}
	}

	public boolean teleport(Location destination) {
		if (location.equals(destination)) {
			return false;
		}
		setLocation(destination);
		return true;
	}

	public Room getRoom() {
		return room;
	}

	protected void setRoom(Room room) {
		this.room = room;
		replace();
		CameraAnimator.addCamera(this);
	}

	void makeDirty() {
		room.makeDirty();
	}

	@Override
	public int compareTo(Camera c) {
		return name.compareTo(c.name);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Camera) {
			return (room == null || room.equals(((Camera) o).getRoom())) && name.equals(((Camera) o).name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((room != null ? room.getName() : "") + " " + name).hashCode();
	}

	@Override
	public boolean remove() {
		return room.removeCamera(this);
	}

	protected void removeInternal() {
		CameraAnimator.removeCamera(this);
		location.getBlock().setType(Material.AIR);
		if (cameraman != null && cameraman.isValid()) {
			cameraman.remove();
			cameraman = null;
		}
		if (seat != null && seat.isValid()) {
			seat.remove();
			seat = null;
		}
	}

	public boolean isAtLocation(Location location) {
		return isExactLocation(location, this.location);
	}

	public boolean isAtTpLocation(Location location) {
		return isExactLocation(this.tpLocation, location);
	}

	public void replace() {
		replaceSupport();
		replaceCameraman();
		replaceSeat();
	}

	public void replaceSupport() {
		Block block = location.getBlock();
		block.setType(MATERIAL_SUPPORT);
		if (block.getBlockData() instanceof Directional) {
			Directional data = (Directional) block.getBlockData();
			data.setFacing(supportDirection.getOppositeFace());
			block.setBlockData(data);
		}
	}

	public void replaceCameraman() {
		Location cameramanLocation = location.clone().add(0.5, -1.5, 0.5);
		if (cameraman == null || cameraman.isDead()) {
			cameraman = cameramanLocation.getWorld().spawn(cameramanLocation, ArmorStand.class);
			cameraman.addScoreboardTag("camhead_cameraman");
			makeDirty();
		} else if (!cameramanLocation.equals(cameraman.getLocation())) {
			cameraman.teleport(cameramanLocation);
		}
		cameraman.setVisible(false);
		cameraman.setInvulnerable(true);
		cameraman.setGravity(false);
		ItemStack cameraItem = new ItemStack(MATERIAL_CAMERA);
		ItemMeta meta = cameraItem.getItemMeta();
		meta.setCustomModelData(MATERIAL_CAMERA_CUSTOM_MODEL_DATA);
		cameraItem.setItemMeta(meta);
		cameraman.getEquipment().setHelmet(cameraItem);
	}

	public void replaceSeat() {
		Location seatLocation = location.clone().add(0.5, -2.25, 0.5);
		if (seat == null || seat.isDead()) {
			seat = seatLocation.getWorld().spawn(seatLocation, ArmorStand.class);
			seat.addScoreboardTag("camhead_seat");
			makeDirty();
		} else if (!seatLocation.equals(seat.getLocation())) {
			seat.teleport(seatLocation);
		}
		seat.setVisible(false);
		cameraman.setInvulnerable(true);
		seat.setGravity(false);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<>();
		ser.put("name", name);
		ser.put("location", location);
		ser.put("supportDirection", supportDirection.toString());
		ser.put("animationDirection", animationDirection.toString());
		ser.put("cameramanUUID", cameraman != null ? cameraman.getUniqueId().toString() : "");
		ser.put("seatUUID", seat != null ? seat.getUniqueId().toString() : "");
		return ser;
	}

	public static Camera deserialize(Map<String, Object> args) {
		String name = String.valueOf(args.get("name"));
		Location location = (Location) args.get("location");
		String supportDirectionString = (String) args.get("supportDirection");
		String animationDirectionString = (String) args.get("animationDirection");
		BlockFace supportDirection = supportDirectionString != null ? BlockFace.valueOf(supportDirectionString) : guessSupportDirection(location);
		BlockFace animationDirection = animationDirectionString != null ? BlockFace.valueOf(animationDirectionString) : guessAnimationDirection(location);
		location.getWorld().loadChunk(location.getChunk()); // load to get the entity
		String cameramanUUID = (String) args.getOrDefault("cameramanUUID", null);
		ArmorStand cameraman = null;
		if (cameramanUUID != null) {
			cameraman = (ArmorStand) Bukkit.getEntity(UUID.fromString(cameramanUUID));
		}
		String seatUUID = (String) args.getOrDefault("seatUUID", null);
		ArmorStand seat = null;
		if (seatUUID != null) {
			seat = (ArmorStand) Bukkit.getEntity(UUID.fromString(seatUUID));
		}
		return new Camera(name, location, supportDirection, animationDirection, cameraman, seat);
	}

	protected void updateWith(Camera camera) {
		setLocation(camera.getLocation());
	}

	private static BlockFace guessSupportDirection(Location location) {
		for (BlockFace direction : SUPPORT_DIRECTIONS) {
			if (location.getBlock().getRelative(direction).getType().isSolid()) {
				return direction;
			}
		}
		return SUPPORT_DIRECTIONS.length > 0 ? BlockFace.DOWN : null;
	}

	/**
	 * Guess the best direction for the animation
	 * 0 wall: North
	 * 1 wall: opposite of the wall
	 * 2 wall: the opposite of the corner, or a face without the wall
	 * 3 wall: the face without the wall
	 */
	private static BlockFace guessAnimationDirection(Location location) {
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		List<BlockFace> facesFree = new ArrayList<>();
		List<BlockFace> facesSolid = new ArrayList<>();
		for (int i = 0; i < directions.length; i++) {
			if (location.getBlock().getRelative(directions[i]).getType().isSolid()) {
				facesSolid.add(directions[i]);
			} else {
				facesFree.add(directions[i]);
			}
		}
		switch (facesFree.size()) {
			case 0:
			case 4:
				return BlockFace.NORTH;
			case 1:
				return facesFree.get(0);
			case 2:
				return getAnimationIf2FreeFaces(facesFree);
			case 3:
				return facesSolid.get(0).getOppositeFace();
			default:
				throw new IllegalStateException("Camera has more than 4 solid blocks: " + facesFree.size());
		}
	}

	private static BlockFace getAnimationIf2FreeFaces(List<BlockFace> facesFree) {
		if (facesFree.get(0).getOppositeFace().equals(facesFree.get(1))) {
			// corridor
			return facesFree.get(0);
		} else {
			// corner
			if (facesFree.contains(BlockFace.NORTH)) {
				if (facesFree.contains(BlockFace.EAST)) {
					return BlockFace.NORTH_EAST;
				} else {
					return BlockFace.NORTH_WEST;
				}
			} else { // south
				if (facesFree.contains(BlockFace.EAST)) {
					return BlockFace.SOUTH_EAST;
				} else {
					return BlockFace.SOUTH_WEST;
				}
			}
		}
	}

	public void setSupportDirection(BlockFace support) {
		if (supportDirection != support) {
			supportDirection = support;
			replace();
			makeDirty();
		}
	}

	public void setAnimationFace(BlockFace facing) {
		if (animationDirection != facing) {
			animationDirection = facing;
			CameraAnimator.addCamera(this); // update
			replace();
			makeDirty();
		}
	}

	public BlockFace getSupportDirection() {
		return supportDirection;
	}

	public BlockFace getAnimationDirection() {
		return animationDirection;
	}

	public ArmorStand getCameraSeat() {
		if (seat == null) replace();
		return seat;
	}

	public ArmorStand getCameraman() {
		if (cameraman == null) replace();
		return cameraman;
	}

	public static void loadConfig(YamlConfiguration config) {
		MATERIAL_SUPPORT = Material.valueOf(config.getString("camera.materials.support", "END_RODE"));
		MATERIAL_CAMERA = Material.valueOf(config.getString("camera.materials.cameraItem", "END_RODE"));
		MATERIAL_CAMERA_CUSTOM_MODEL_DATA = config.getInt("camera.materials.cameraItemCustomModelData", 0);
	}
}
