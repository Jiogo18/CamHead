package fr.jarven.camhead.components;

import org.bukkit.Location;

public interface ComponentBase {
	public Location getLocation();
	public void setLocation(Location location);
	public default boolean isExactLocation(Location a, Location b) {
		return a.getWorld().equals(b.getWorld())
			&& a.getX() == b.getX()
			&& a.getY() == b.getY()
			&& a.getZ() == b.getZ();
	}
	public boolean remove();
	public String getName();
}
