package org.usfirst.frc.team1089.robot;

/**
 * The {@code MercEncoder} class contains fields and methods for reading the encoders on the robot.
 */
public class MercEncoder {

	private final Config config = Config.getCurrent();

	public final static double TICKS_PER_ROTATION = 1440;
	public final double DISTANCE_PER_TICK_INCHES = 
		//	((config.WHEEL_SIZE_INCHES * Math.PI) / (TICKS_PER_ROTATION * config.GEAR_RATIO));
	1.0/(TICKS_PER_ROTATION * config.GEAR_RATIO / (Math.PI * config.WHEEL_SIZE_INCHES));
	
	public final double DISTANCE_PER_TICK_FEET = DISTANCE_PER_TICK_INCHES / 12;

	// unused?
	public MercEncoder() {
			
	}

	/**
	 * <pre>
	 * public double distanceTravelled(double count,
	 *                                 double sign)
	 * </pre>
	 * Gets the distance travelled based on ticks
	 * @param count the amount of ticks travelled
	 * @param sign 1 or -1 to determine the direction of travel
	 * @return the distance travelled in feet
	 */
	public double distanceTravelled(double count, double sign) {
		return count * DISTANCE_PER_TICK_FEET * sign;
	}

	/**
	 * <pre>
	 * public double convertDistanceToEncoderTicks(double distance,
	 *                                             double sign)
	 * </pre>
	 * Converts the distance travelled to encoder ticks.
	 * @param distance the distance travelled in feet
	 * @param sign 1 or -1 to determine the direction of travel
	 * @return the distance travelled in feet converted to encoder ticks
	 */
	public double convertDistanceToEncoderTicks(double distance, double sign) {
		return distance / (DISTANCE_PER_TICK_FEET * sign);
	}
}
