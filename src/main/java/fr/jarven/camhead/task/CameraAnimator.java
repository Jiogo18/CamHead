package fr.jarven.camhead.task;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;

import fr.jarven.camhead.components.Camera;

public class CameraAnimator {
	private static BukkitTask task;
	private static final Map<Camera, AnimationStep> camerasAngle = new HashMap<>();
	public static double cameraSpeed = 5;
	public static boolean animateOnlyIfPlayers = true;

	private CameraAnimator() {}

	public static void onEnable(JavaPlugin plugin) {
		task = new BukkitRunnable() {
			@Override
			public void run() {
				camerasAngle.values().forEach(AnimationStep::animate);
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1);
	}

	public static void loadConfig(YamlConfiguration config) {
		CameraAnimator.cameraSpeed = config.getDouble("cameraSpeed", 5);
		CameraAnimator.animateOnlyIfPlayers = config.getBoolean("animateOnlyIfPlayers", true);
	}

	public static void addCamera(Camera camera) {
		if (camerasAngle.containsKey(camera)) {
			AnimationStep step = camerasAngle.get(camera);
			step.update(camera);
		} else {
			camerasAngle.put(camera, new AnimationStep(camera));
		}
	}

	public static void removeCamera(Camera camera) {
		camerasAngle.remove(camera);
	}

	public static void onDisable() {
		camerasAngle.clear();
		if (task != null && !task.isCancelled()) task.cancel();
	}

	private static class AnimationStep {
		private Camera camera;
		private float angleMin;
		private float angleMax;
		private float angle;
		private float direction;
		private float cameramanYaw;

		private AnimationStep(Camera camera) {
			this.angle = 0;
			this.direction = 1;
			update(camera);
		}

		private void update(Camera camera) {
			this.camera = camera;
			switch (camera.getAnimationDirection()) {
				case NORTH:
					angleMin = 90;
					angleMax = 270;
					break;
				case SOUTH:
					angleMin = -90;
					angleMax = 90;
					break;
				case EAST:
					angleMin = 180;
					angleMax = 360;
					break;
				case WEST:
					angleMin = 0;
					angleMax = 180;
					break;
				case NORTH_EAST:
					angleMin = -180;
					angleMax = -90;
					break;
				case NORTH_WEST:
					angleMin = 90;
					angleMax = 180;
					break;
				case SOUTH_EAST:
					angleMin = -90;
					angleMax = 0;
					break;
				case SOUTH_WEST:
					angleMin = 0;
					angleMax = 90;
					break;
				default:
					throw new IllegalArgumentException("Unsuported animation direction: " + camera.getAnimationDirection());
			}
			angle = (angleMin + angleMax) / 2;
			direction = 1;
			cameramanYaw = camera.getCameraman().getLocation().getYaw();
		}

		private void animate() {
			if (animateOnlyIfPlayers && camera.getPlayers().isEmpty()) return;
			angle += direction * cameraSpeed;
			if (angle > angleMax) {
				angle = angleMax;
				direction = -1;
			} else if (angle < angleMin) {
				angle = angleMin;
				direction = 1;
			}
			setAngle(angle);
		}

		private void setAngle(float angle) {
			ArmorStand cameraman = camera.getCameraman();
			EulerAngle head = cameraman.getHeadPose();
			head = head.setY(Math.toRadians(angle - cameramanYaw));
			cameraman.setHeadPose(head);
		}
	}
}
