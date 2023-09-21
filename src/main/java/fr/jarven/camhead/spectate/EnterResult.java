package fr.jarven.camhead.spectate;

import javax.annotation.Nonnull;

import fr.jarven.camhead.components.Camera;
import fr.jarven.camhead.components.Room;

public class EnterResult {
	public enum EnterResultType {
		SUCCESS_ENTER,
		SUCCESS_CHANGE,
		SAME_CAMERA,
		FAILED_UNKNOWN,
		NO_CAMERAS,
		NO_PERMISSION,
		NO_SEAT,
		ROOM_FULL,
		LEAVING
	}

	public final Room room;
	public final Camera camera;
	public final EnterResultType result;

	public EnterResult(@Nonnull Room room, Camera camera, @Nonnull EnterResultType result) {
		this.room = room;
		this.camera = camera;
		this.result = result;
	}

	public EnterResult(@Nonnull Camera camera, @Nonnull EnterResultType result) {
		this(camera.getRoom(), camera, result);
	}

	public Camera getCamera() {
		return camera;
	}

	public Room getRoom() {
		return room;
	}

	public EnterResultType getType() {
		return result;
	}

	public boolean isSuccess() {
		return result == EnterResultType.SUCCESS_ENTER || result == EnterResultType.SUCCESS_CHANGE || result == EnterResultType.SAME_CAMERA;
	}

	public boolean isChange() {
		return result == EnterResultType.SUCCESS_CHANGE;
	}

	public void ifSuccess(Runnable runnable) {
		if (isSuccess()) runnable.run();
	}

	public void ifFailure(Runnable runnable) {
		if (!isSuccess()) runnable.run();
	}
}