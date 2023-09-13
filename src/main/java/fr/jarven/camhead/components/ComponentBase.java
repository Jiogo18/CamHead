package fr.jarven.camhead.components;

import org.bukkit.Location;

public interface ComponentBase {
	public Location getLocation();
	public void setLocation(Location location);
	public default boolean isExactLocation(Location a, Location b, float xzEpsilon, float yEpsilon) {
		return a.getWorld().equals(b.getWorld())
			&& Math.abs(a.getX() - b.getX()) <= xzEpsilon
			&& Math.abs(a.getY() - b.getY()) <= yEpsilon
			&& Math.abs(a.getZ() - b.getZ()) <= xzEpsilon;
	}
	public boolean remove();
	public String getName();
}
