package org.usfirst.frc.team1089.robot;

public class MercEncoder {
	
	private Config config;
	public final static double TICKS_PER_ROTATION = 1440;
	public final double DISTANCE_PER_TICK_INCHES = ((config.WHEEL_SIZE_INCHES * Math.PI) / (TICKS_PER_ROTATION * config.GEAR_RATIO));
	public final double DISTANCE_PER_TICK_FEET = DISTANCE_PER_TICK_INCHES / 12;
	
	public MercEncoder(){
		config = config.getCurrent();
	}
	
	public double distanceTravelled(double count, double sign){
		return count * DISTANCE_PER_TICK_FEET * sign ;
	}
	
	public double convertDistanceToEncoderTicks(double distance, double sign){
		return distance / (DISTANCE_PER_TICK_FEET * sign);
	}
}
