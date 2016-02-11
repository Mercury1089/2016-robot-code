package org.usfirst.frc.team1089.robot;

public class Config {
	public static double HFOV_DEGREES; // calibrated value for Axis M1011 (M1013
										// should be greater)
	public static double CAM_ELEVATION_FEET;
	public static double HORIZONTAL_CAMERA_RES_PIXELS;
	public static double TURN_ANGLE_MIN_DEGREES;
	public static double TURN_ANGLE_MAX_DEGREES;
	public static double IN_LINE_MIN; // TODO FIX
	public static double AXLE_TRACK_INCHES; // TODO FIX THIS
	public static double LEFT_ENC_SIGN;
	public static double RIGHT_ENC_SIGN;
	public static double LEFT_DRIVE_SIGN;
	public static double RIGHT_DRIVE_SIGN;
	public static double WHEEL_SIZE_INCHES;
	public static double GEAR_RATIO;

	public enum configType {
		PROTO, COMPETITION;
	}

	private static Config current = new Config(configType.PROTO);

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
		current = new Config(confType);
	}

	public static Config getCurrent() {
		return current;
	}
}
