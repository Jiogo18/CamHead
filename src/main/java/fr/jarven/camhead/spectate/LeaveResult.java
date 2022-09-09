package fr.jarven.camhead.spectate;

import fr.jarven.camhead.components.Camera;

public class LeaveResult {
	public enum LeaveResultType {
		SUCCESS,
		FAILED_UNKNOWN,
		NOT_SPECTATING,
		ALREADY_LEAVING,
		NO_PERMISSION
	}

	public final Camera camera;
	public final LeaveResultType result;

	public LeaveResult(Camera camera, LeaveResultType result) {
		this.camera = camera;
		this.result = result;
	}

	public Camera getCamera() {
		return camera;
	}

	public LeaveResultType getType() {
		return result;
	}

	public boolean isSuccess() {
		return result == LeaveResultType.SUCCESS || result == LeaveResultType.ALREADY_LEAVING;
	}

	public void ifSuccess(Runnable runnable) {
		if (isSuccess()) runnable.run();
	}

	public void ifFailure(Runnable runnable) {
		if (!isSuccess()) runnable.run();
	}
}