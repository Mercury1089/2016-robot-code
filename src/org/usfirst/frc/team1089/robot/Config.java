package org.usfirst.frc.team1089.robot;

/**
 * The {@code Config} class is a static class containing constants used throughout the robot.
 */
public class Config {
	public final double HFOV_DEGREES; // calibrated value for Axis M1011 (M1013
								// should be greater)
	public final double CAM_ELEVATION_FEET;
	public final double HORIZONTAL_CAMERA_RES_PIXELS;
	public final double TURN_ANGLE_MIN_DEGREES;
	public final double TURN_ANGLE_MAX_DEGREES;
	public final double IN_LINE_MIN;
	public final double AXLE_TRACK_INCHES;
	public final double LEFT_ENC_SIGN;
	public final double RIGHT_ENC_SIGN;
	public final double LEFT_DRIVE_SIGN;
	public final double RIGHT_DRIVE_SIGN;
	public final double WHEEL_SIZE_INCHES;
	public final double GEAR_RATIO;

	/**
	 * The {@code ConfigType} is an enum for two possible uses of the code:
	 * in the prototype base, and the competition base.
	 */
	public enum ConfigType {
		PROTO, 
		COMPETITION;
	}
	
	// change the value below to specify the config type to use if not
	// otherwise specified by calling setCurrent()
	public final static ConfigType DEFAULT_CONFIG_TYPE = ConfigType.COMPETITION;
	
	public final ConfigType configType; // Do not initialize - constructor will do it
	
	private static Config current = null; // Do not initialize - getCurrent() does it if necessary.

	private Config(ConfigType configType) {
		
		this.configType = configType;
		
		switch (configType) {
		case PROTO:
			// calibrated value for Axis M1011 (M1013 should be greater)
			HFOV_DEGREES = 41; 
			
			CAM_ELEVATION_FEET = 9.5 / 12;
			
			// NOT native resolution of Axis M1011 or M1013
            // need to match size used in GRIP
			HORIZONTAL_CAMERA_RES_PIXELS = 320;
			
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
			// calibrated value for Axis M1013 
			HFOV_DEGREES = 67; 
			CAM_ELEVATION_FEET = 9.5 / 12;
			
			// NOT native resolution of Axis M1011 or M1013
            // need to match size used in GRIP
			HORIZONTAL_CAMERA_RES_PIXELS = 320;
			
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

	/**
	 * <pre>
	 * public synchronized static void setCurrent(ConfigType configType)
	 * </pre>
	 * Sets the current state of the {@code Config} to the specified type.
	 * @param configType the type to set the {@code Config} to.
	 * @throws IllegalStateException if a different state has already been set
	 */
	public synchronized static void setCurrent(ConfigType configType) {
		if (current == null) {
			current = new Config(configType);
		} else if (current.configType != configType) {
			throw new IllegalStateException("Cannot change configuration type once it has been set.");
		}
	}

	/**
	 * <pre>
	 * public static Config getCurrent()
	 * </pre>
	 * Gets the current {@code Config} being used by the robot.
	 * @return the current {@code Config} being used by the robot.
	 */
	public static Config getCurrent() {
		if (current == null) {
			setCurrent(DEFAULT_CONFIG_TYPE);
		}
		return current;
	}
}
