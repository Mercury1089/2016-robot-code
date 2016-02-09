package org.usfirst.frc.team1089.robot;

public class MercEncoder {
	public final static double WHEEL_SIZE_INCHES = 4.0;
	public final static double TICKS_PER_ROTATION = 1440;
	public final static double GEAR_RATIO = 1.0; // TODO FIX THIS (on new robot should be a number above 1.0)
	public final static double DISTANCE_PER_TICK_INCHES = ((WHEEL_SIZE_INCHES * Math.PI) / (TICKS_PER_ROTATION * GEAR_RATIO));
	public final static double DISTANCE_PER_TICK_FEET = DISTANCE_PER_TICK_INCHES / 12;
	
	public static double distanceTravelled(double count, double sign){
		return count * DISTANCE_PER_TICK_FEET * sign ;
	}
	
	public static double convertDistanceToEncoderTicks(double distance, double sign){
		return distance / (DISTANCE_PER_TICK_FEET * sign);
	}
}
