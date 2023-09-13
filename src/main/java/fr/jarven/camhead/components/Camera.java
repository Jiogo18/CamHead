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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import fr.jarven.camhead.CamHead;
import fr.jarven.camhead.task.CameraAnimator;
import fr.jarven.camhead.utils.YawBlockFace;
import net.md_5.bungee.api.ChatColor;

public class Camera implements ComponentBase, Comparable<Camera>, ConfigurationSerializable {
	private static boolean replaceOnReload = false;
	public static Material MATERIAL_BLOCK = Material.END_ROD;
	private static final SharedItem[] CAMERAMAN_ITEMS = new SharedItem[] {null, null, null, null, null, null};
	private static final SharedItem[] SEAT_ITEMS = new SharedItem[] {null, null, null, null, null, null};
	private static final EnumMap<BlockFace, Vector> CAMERAMAN_OFFSET = new EnumMap<>(BlockFace.class);
	private static final EnumMap<BlockFace, Vector> SEAT_OFFSET = new EnumMap<>(BlockFace.class);
	public static final BlockFace[] SUPPORT_DIRECTIONS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
	public static final BlockFace[] ANIMATION_DIRECTIONS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST};
	private Room room;
	private final String name;
	private Location location;
	private BlockFace supportDirection;
	private BlockFace animationDirection;
	private UUID cameramanUUID = null;
	private UUID seatUUID = null;
	private ArmorStand cameraman = null;
	private ArmorStand seat = null;
	private final Set<Player> players = new HashSet<>();

	protected Camera(Room room, String name, Location location) {
		RoomManager.assertNameStandard(name);
		this.room = room;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.supportDirection = guessSupportDirection(location);
		this.animationDirection = guessAnimationDirection(location);
		replace();
		CameraAnimator.addCamera(this);
	}

	private Camera(String name, Location location, BlockFace supportDirection, BlockFace animationDirection, UUID cameraman, UUID seat) {
		RoomManager.assertNameStandard(name);
		this.room = null;
		this.name = name;
		this.location = location.getBlock().getLocation();
		this.supportDirection = supportDirection;
		this.animationDirection = animationDirection;
		this.cameramanUUID = cameraman;
		this.seatUUID = seat;
		getCameraman();
		getCameraSeat();
		// replace latter when the room is set (for makeDirty)
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
		return location.clone().add(0.5, -0.5, 0.5).add(getSeatOffset());
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
		setLocation(destination);
		return true;
	}

	private void loadChunk() {
		location.getChunk().load();
	}

	public @Nonnull Room getRoom() {
		Room r = this.room;
		if (r == null) {
			throw new IllegalStateException("Screen " + name + " is not in a room");
		}
		return r;
	}

	protected void setRoom(Room room) {
		this.room = room;
		if (replaceOnReload) replace();
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
		getCameraman(true).ifPresent(ArmorStand::remove);
		this.cameramanUUID = null;
		this.cameraman = null;
		getCameraSeat(true).ifPresent(ArmorStand::remove);
		this.cameramanUUID = null;
		this.seat = null;
	}

	public boolean isAtLocation(Location location) {
		return isExactLocation(location, this.location, 0.1f, 0.4f);
	}

	public boolean isAtTpLocation(Location location) {
		return isExactLocation(getTpLocation(), location, 0.1f, 0.4f);
	}

	public void replace() {
		replaceSupport();
		replaceCameraman();
		replaceSeat();
	}

	public void replaceSupport() {
		Block block = location.getBlock();
		if (block.getType() != MATERIAL_BLOCK) {
			block.setType(MATERIAL_BLOCK);
		}
		if (block.getBlockData() instanceof Directional) {
			Directional data = (Directional) block.getBlockData();
			data.setFacing(supportDirection.getOppositeFace());
			block.setBlockData(data);
		}
	}

	public void replaceCameraman() {
		Location cameramanLocation = location.clone().add(0.5, -0.75, 0.5).add(getCameramanOffset());
		cameramanLocation.setYaw(getSupportYaw());

		if (!getCameraman(true).isPresent()) {
			cameraman = cameramanLocation.getWorld().spawn(cameramanLocation, ArmorStand.class);
			cameramanUUID = cameraman.getUniqueId();
			cameraman.addScoreboardTag("camhead_cameraman");
			cameraman.addScoreboardTag("camhead_camera_" + name);
			cameraman.setCustomName("CamHead Cameraman " + name);
			makeDirty();
		} else if (!cameramanLocation.equals(cameraman.getLocation())) {
			cameraman.teleport(cameramanLocation);
		}
		cameraman.setVisible(false);
		cameraman.setInvulnerable(true);
		cameraman.setGravity(false);
		cameraman.setSmall(false);
		cameraman.setBasePlate(false);
		SharedItem.createArmor(cameraman.getEquipment(), CAMERAMAN_ITEMS);
	}

	public void replaceSeat() {
		removePlayers();
		Location seatLocation = location.clone().add(0.5, -1.5, 0.5).add(getSeatOffset());

		if (!getCameraSeat(true).isPresent()) {
			seat = seatLocation.getWorld().spawn(seatLocation, ArmorStand.class);
			seatUUID = seat.getUniqueId();
			seat.addScoreboardTag("camhead_seat");
			seat.addScoreboardTag("camhead_camera_" + name);
			seat.setCustomName("CamHead Seat " + name);
			makeDirty();
		} else if (!seatLocation.equals(seat.getLocation())) {
			seat.teleport(seatLocation);
		}
		seat.setVisible(false);
		seat.setInvulnerable(true);
		seat.setGravity(false);
		seat.setSmall(true);
		seat.setBasePlate(false);
		SharedItem.createArmor(seat.getEquipment(), SEAT_ITEMS);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> ser = new HashMap<>();
		ser.put("name", name);
		ser.put("location", location);
		ser.put("supportDirection", supportDirection.toString());
		ser.put("animationDirection", animationDirection.toString());
		ser.put("cameramanUUID", cameramanUUID.toString());
		ser.put("seatUUID", seatUUID.toString());
		return ser;
	}

	public static Camera deserialize(Map<String, Object> args) {
		String name = String.valueOf(args.get("name"));
		Location location = (Location) args.get("location");
		String supportDirectionString = (String) args.get("supportDirection");
		String animationDirectionString = (String) args.get("animationDirection");
		BlockFace supportDirection = supportDirectionString != null ? BlockFace.valueOf(supportDirectionString) : guessSupportDirection(location);
		BlockFace animationDirection = animationDirectionString != null ? BlockFace.valueOf(animationDirectionString) : guessAnimationDirection(location);
		UUID cameramanUUID = null;
		try {
			String cameramanUUIDString = (String) args.get("cameramanUUID");
			cameramanUUID = UUID.fromString(cameramanUUIDString);
		} catch (Exception e) {
			CamHead.LOGGER.warning("Failed to load cameraman's UUID for camera " + name + " at " + location);
			e.printStackTrace();
		}
		UUID seatUUID = null;
		try {
			String seatUUIDString = (String) args.get("seatUUID");
			seatUUID = UUID.fromString(seatUUIDString);
		} catch (Exception e) {
			CamHead.LOGGER.warning("Failed to load seat's UUID for camera " + name + " at " + location);
			e.printStackTrace();
		}
		return new Camera(name, location, supportDirection, animationDirection, cameramanUUID, seatUUID);
	}

	protected void updateWith(Camera camera) {
		setLocation(camera.getLocation());
	}

	private static BlockFace guessSupportDirection(Location location) {
		for (BlockFace direction : SUPPORT_DIRECTIONS) {
			Material material = location.getBlock().getRelative(direction).getType();
			if (material.isSolid() && material != Material.BARRIER) {
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
			Material material = location.getBlock().getRelative(directions[i]).getType();
			if (material.isSolid() && material != Material.BARRIER) {
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

	public float getSupportYaw() {
		Vector direction = supportDirection.getDirection();
		if (direction.getY() != 0) {
			float animationYaw = getAnimationYaw();
			// round to 90°
			return Math.round(animationYaw / 90) * 90.0f;
		}
		return 180 + YawBlockFace.blockFaceToYaw(supportDirection);
	}

	private float getAnimationYaw() {
		return YawBlockFace.blockFaceToYaw(animationDirection);
	}

	private boolean isEntityLoaded(ArmorStand entity) {
		return entity != null && entity.isValid() && !entity.isDead();
	}

	public ArmorStand getOrCreateSeat() {
		if (!getCameraSeat(true).isPresent()) replaceSeat();
		return seat;
	}

	public Optional<ArmorStand> getCameraSeat(boolean loadChunk) {
		if (!isEntityLoaded(seat)) {
			if (seatUUID != null) {
				if (loadChunk) loadChunk();
				seat = (ArmorStand) Bukkit.getEntity(seatUUID);
			} else {
				seat = null;
			}
		}
		return Optional.ofNullable(seat);
	}

	public Optional<ArmorStand> getCameraSeat() {
		return getCameraSeat(false);
	}

	public UUID getSeatUUID() {
		return seatUUID;
	}

	public ArmorStand getOrCreateCameraman() {
		if (!getCameraman(true).isPresent()) replaceCameraman();
		return cameraman;
	}

	public Optional<ArmorStand> getCameraman(boolean loadChunk) {
		if (!isEntityLoaded(cameraman)) {
			if (cameramanUUID != null) {
				if (loadChunk) loadChunk();
				cameraman = (ArmorStand) Bukkit.getEntity(cameramanUUID);
			} else {
				cameraman = null;
			}
		}
		return Optional.ofNullable(cameraman);
	}

	public Optional<ArmorStand> getCameraman() {
		return getCameraman(false);
	}

	public UUID getCameramanUUID() {
		return cameramanUUID;
	}

	private static void loadDirectionOffsets(EnumMap<BlockFace, Vector> directionOffsets, YamlConfiguration config, String path) {
		directionOffsets.clear();
		for (String key : config.getConfigurationSection(path).getKeys(false)) {
			BlockFace face;
			try {
				face = BlockFace.valueOf(key);
			} catch (IllegalArgumentException e) {
				CamHead.LOGGER.warning("Invalid direction offset: " + key + " (at " + path + ")");
				continue;
			}

			String[] vec = config.getString(path + "." + key).split(",");
			if (vec.length != 3) {
				CamHead.LOGGER.warning(path + "." + key + " is not a vector (must be 3 numbers separated by commas)");
				continue;
			}
			try {
				double x = Double.parseDouble(vec[0]);
				double y = Double.parseDouble(vec[1]);
				double z = Double.parseDouble(vec[2]);
				directionOffsets.put(face, new Vector(x, y, z));
			} catch (NumberFormatException e) {
				CamHead.LOGGER.warning("camera.cameraman.offset." + key + " is not a vector");
				e.getStackTrace();
			}
		}
	}

	public static void loadConfig(YamlConfiguration config) {
		try {
			MATERIAL_BLOCK = Material.valueOf(config.getString("camera.block.material", "BARRIER").toUpperCase());
		} catch (IllegalArgumentException e) {
			CamHead.LOGGER.warning("Invalid material for camera block: " + config.getString("camera.block.material"));
			MATERIAL_BLOCK = Material.BARRIER;
		}
		SharedItem.loadSharedItems(CAMERAMAN_ITEMS, config, "camera.cameraman.inventory");
		SharedItem.loadSharedItems(SEAT_ITEMS, config, "camera.seat.inventory");
		loadDirectionOffsets(CAMERAMAN_OFFSET, config, "camera.cameraman.offset");
		loadDirectionOffsets(SEAT_OFFSET, config, "camera.seat.offset");
		replaceOnReload = config.getBoolean("replaceOnReload", false);
	}

	public Vector getCameramanOffset() {
		return CAMERAMAN_OFFSET.getOrDefault(supportDirection, new Vector(0, 0, 0));
	}

	public Vector getSeatOffset() {
		return SEAT_OFFSET.getOrDefault(supportDirection, new Vector(0, 0, 0));
	}

	public boolean hasCameraman() {
		return getCameraman(true).isPresent();
	}

	public boolean hasSeat() {
		return getCameraSeat(true).isPresent();
	}

	public boolean canHaveSpectators() {
		return hasSeat() && this.room.canEnter();
	}

	public boolean addPlayer(Player player) {
		if (!hasSeat()) return false;
		if (!isEntityLoaded(seat)) return false;
		double distance = player.getWorld() != seat.getWorld() ? 1000 : player.getLocation().distance(seat.getLocation());
		if (distance > 100) {
			player.sendMessage(ChatColor.RED + "You are too far away to watch this camera");
			return false;
		}
		if (!this.seat.addPassenger(player)) return false;

		this.players.add(player);
		return true;
	}

	public void removePlayer(Player player) {
		this.players.remove(player);
		if (hasSeat())
			this.seat.removePassenger(player);
	}

	public Set<Player> getPlayers() {
		return this.players;
	}

	public void removePlayers() {
		Set<Player> playersInside = new HashSet<>(this.players);
		for (Player player : playersInside) {
			CamHead.spectatorManager.leave(player);
		}
	}
}
