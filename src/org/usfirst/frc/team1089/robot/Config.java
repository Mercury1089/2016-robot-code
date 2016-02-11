package org.usfirst.frc.team1089.robot;

public class Config {
	public final double HFOV_DEGREES; // calibrated value for Axis M1011 (M1013
								// should be greater)
	public final double CAM_ELEVATION_FEET;
	public final double HORIZONTAL_CAMERA_RES_PIXELS;
	public final double TURN_ANGLE_MIN_DEGREES;
	public final double TURN_ANGLE_MAX_DEGREES;
	public final double IN_LINE_MIN; // TODO FIX
	public final double AXLE_TRACK_INCHES; // TODO FIX THIS
	public final double LEFT_ENC_SIGN;
	public final double RIGHT_ENC_SIGN;
	public final double LEFT_DRIVE_SIGN;
	public final double RIGHT_DRIVE_SIGN;
	public final double WHEEL_SIZE_INCHES;
	public final double GEAR_RATIO;

	public enum configType {
		PROTO, COMPETITION;
	}

	private static Config current = null; // Do not initialize - getCurrent() does it if necessary.

	private Config(configType confType) {
		switch (confType) {
		case PROTO:
			HFOV_DEGREES = 41; // calibrated value for Axis M1011 (M1013 should
								// be greater)
			CAM_ELEVATION_FEET = 9.5 / 12;
			HORIZONTAL_CAMERA_RES_PIXELS = 320; // NOT native resolution of Axis
												// M1011 or M1013 - need to
												// match size used in GRIP
			TURN_ANGLE_MIN_DEGREES = -1.0;
			TURN_ANGLE_MAX_DEGREES = 1.0;
			IN_LINE_MIN = .4;
			AXLE_TRACK_INCHES = 15.126 * 2;
			LEFT_ENC_SIGN = 1.0;
			RIGHT_ENC_SIGN = -1.0;
			LEFT_DRIVE_SIGN = -1.0;
			RIGHT_DRIVE_SIGN = 1.0;
			WHEEL_SIZE_INCHES = 4.0;
			GEAR_RATIO = 1.0;
			break;
		default: // COMPETITION
			HFOV_DEGREES = 41; // calibrated value for Axis M1011 (M1013 should
								// be greater)
			CAM_ELEVATION_FEET = 9.5 / 12;
			HORIZONTAL_CAMERA_RES_PIXELS = 320; // NOT native resolution of Axis
												// M1011 or M1013 - need to
												// match size used in GRIP
			TURN_ANGLE_MIN_DEGREES = -1.0;
			TURN_ANGLE_MAX_DEGREES = 1.0;
			IN_LINE_MIN = .4;
			AXLE_TRACK_INCHES = 15.126 * 2;
			LEFT_ENC_SIGN = 1.0;
			RIGHT_ENC_SIGN = -1.0;
			LEFT_DRIVE_SIGN = -1.0;
			RIGHT_DRIVE_SIGN = 1.0;
			WHEEL_SIZE_INCHES = 10.0;
			GEAR_RATIO = 4.0 / 3.0;
			break;

		}
	}

	public static void setCurrent(configType confType) {
		if (current == null) {
		current = new Config(confType);
		} else {
			throw new IllegalStateException("Current configuration type can only be set once.");
		}
	}

	public static Config getCurrent() {
		if (current == null) {
			new Config(configType.PROTO); // Default to PROTO if no one has called setCurrent.
		}
		return current;
	}
}
