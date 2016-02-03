package org.usfirst.frc.team1089.robot;

public class Encoder {
	public final double WHEEL_SIZE_INCHES = 4;
	public final double TICKS_PER_ROTATION = 1440;
	public final double DISTANCE_PER_TICK_INCHES = (4.0 * Math.PI / TICKS_PER_ROTATION);
	public final double DISTANCE_PER_TICK_FEET = DISTANCE_PER_TICK_INCHES / 12;
	
	public double distanceTravelled(double count, double sign){
		return count * DISTANCE_PER_TICK_FEET * sign ;
	}
	public double convertDistanceToEncoderTicks(double distance, double sign){
		return distance / (DISTANCE_PER_TICK_FEET * sign);
	}
}
