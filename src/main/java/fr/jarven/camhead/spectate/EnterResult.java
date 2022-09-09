package fr.jarven.camhead.spectate;

import fr.jarven.camhead.components.Camera;

public class EnterResult {
	public enum EnterResultType {
		SUCCESS_ENTER,
		SUCCESS_CHANGE,
		SAME_CAMERA,
		FAILED_UNKNOWN,
		NO_CAMERAS,
		NO_PERMISSION,
		NO_SEAT,
		ROOM_FULL
	}

	public final Camera camera;
	public final EnterResultType result;

	public EnterResult(Camera camera, EnterResultType result) {
		this.camera = camera;
		this.result = result;
	}

	public Camera getCamera() {
		return camera;
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