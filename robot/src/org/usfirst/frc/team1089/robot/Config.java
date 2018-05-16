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
	public final double COARSE_TURN_ANGLE_MIN_DEGREES;
	public final double COARSE_TURN_ANGLE_MAX_DEGREES;
	public final double IN_LINE_MIN;
	public final double HORIZ_DIST_MIN_FEET;
	public final double HORIZ_DIST_MAX_FEET;
	public final double HORIZ_CLOSE_DIST_MIN_FEET;
	public final double HORIZ_CLOSE_DIST_MAX_FEET;

	public final double AXLE_TRACK_INCHES;
	public final double LEFT_ENC_SIGN;
	public final double RIGHT_ENC_SIGN;
	public final double LEFT_DRIVE_SIGN;
	public final double RIGHT_DRIVE_SIGN;
	public final double WHEEL_SIZE_INCHES;
	public final double GEAR_RATIO;
	
	public final double TILT_THRESH_DEGREES;

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
	
	private final ConfigType _configType; // Do not initialize - constructor will do it
	
	private static Config _current = null; // Do not initialize - getCurrent() does it if necessary.

	/**
	 * <pre>
	 * private Config(ConfigType configType)
	 * </pre>
	 * Constructs a new {@code Config} for the specified robot.
	 * @param configType the robot being used (prototype or competition bot)
	 */
	private Config(ConfigType configType) {
		this._configType = configType;
		
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
			COARSE_TURN_ANGLE_MIN_DEGREES = -2.0;
			COARSE_TURN_ANGLE_MAX_DEGREES = 2.0;
			IN_LINE_MIN = 1.2;
			HORIZ_DIST_MIN_FEET = 7.0;
			HORIZ_DIST_MAX_FEET = 11.0;
			HORIZ_CLOSE_DIST_MIN_FEET = 4.0;
			HORIZ_CLOSE_DIST_MAX_FEET = 5.5;
			
			AXLE_TRACK_INCHES = 15.126 * 2;
			LEFT_ENC_SIGN = 1.0;
			RIGHT_ENC_SIGN = -1.0;
			LEFT_DRIVE_SIGN = -1.0;
			RIGHT_DRIVE_SIGN = 1.0;
			WHEEL_SIZE_INCHES = 4.0;
			GEAR_RATIO = 1.0;
			
			TILT_THRESH_DEGREES = 15.0;	
			break;
		default: // COMPETITION
			// calibrated value for Axis M1013 
			HFOV_DEGREES = 58.0; 
			CAM_ELEVATION_FEET = 32.0 / 12.0;
			
			// NOT native resolution of Axis M1011 or M1013
            // need to match size used in GRIP
			HORIZONTAL_CAMERA_RES_PIXELS = 320;
			
			TURN_ANGLE_MIN_DEGREES = -.7;
			TURN_ANGLE_MAX_DEGREES = .7;
			COARSE_TURN_ANGLE_MIN_DEGREES = -2.0;
			COARSE_TURN_ANGLE_MAX_DEGREES = 2.0;
			IN_LINE_MIN = 1.2;
			HORIZ_DIST_MIN_FEET = 7.0;
			HORIZ_DIST_MAX_FEET = 14.0;
			HORIZ_CLOSE_DIST_MIN_FEET = 4.0;
			HORIZ_CLOSE_DIST_MAX_FEET = 5.5;
			
			AXLE_TRACK_INCHES = 30.329;
			LEFT_ENC_SIGN = 1.0;
			RIGHT_ENC_SIGN = -1.0;
			LEFT_DRIVE_SIGN = -1.0;
			RIGHT_DRIVE_SIGN = 1.0;
			WHEEL_SIZE_INCHES = 9.9;
			GEAR_RATIO = 4.0 / 3.0; // defined as speed in / speed out
			
			TILT_THRESH_DEGREES = 15.0;
			break;
		}
	}

	/**
	 * <pre>
	 * public synchronized static void init(ConfigType configType)
	 * </pre>
	 * Initializes the current {@code Config} to use the specified settings.
	 * @param configType the type to set the {@code Config} to.
	 * @throws IllegalStateException if the {@code Config} has already been initialized.
	 */
	public synchronized static void init(ConfigType configType) {
		if (_current == null) {
			_current = new Config(configType);
		} else if (_current._configType != configType) {
			throw new IllegalStateException("Cannot change configuration type once it has been set.");
		}
	}

	/**
	 * <pre>
	 * public static Config getInstance()
	 * </pre>
	 * Gets the current {@code Config} being used by the robot.
	 * @return the current {@code Config} being used by the robot.
	 */
	public static Config getInstance() {
		if (_current == null) {
			init(DEFAULT_CONFIG_TYPE);
		}
		return _current;
	}
	
	/**
	 * <pre>
	 * public ConfigType getConfigType()
	 * </pre>
	 * Gets the current {@code ConfigType} being used by the robot
	 * @return the current {@code Config} instance's {@code ConfigType}
	 */
	public ConfigType getConfigType() {			
		return getInstance()._configType;
	}
	
	/**
	 * <pre>
	 * public String toString()
	 * </pre>
	 * Returns the current {@code Config}'s {@code ConfigType}.
	 * @return "Competition" in competition state,
	 *         "Proto" in prototype state,
	 *         "Unknown" if there is no current config type
	 */
	public String toString() {
		if (getConfigType() == ConfigType.COMPETITION) {
			return "Competition";
		} else if (getConfigType() == ConfigType.PROTO) {
			return "Proto";
		} else {
			return "Unknown";
		}
	}
}
