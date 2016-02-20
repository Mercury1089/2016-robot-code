package org.usfirst.frc.team1089.robot;

//import edu.wpi.first.wpilibj.AnalogAccelerometer;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;

public class MercAccelerometer {
	private final Config config = Config.getCurrent();
	private BuiltInAccelerometer accel;
	
	public MercAccelerometer(){
		accel = new BuiltInAccelerometer(Accelerometer.Range.k4G);		
	}
	
	public double getAccelZ(){
		return accel.getZ();
	}
	
	public double getTilt() {
		return Math.toDegrees(Math.acos(Math.min(getAccelZ(),1.0)/1.0)); // assumes getAccelZ() returns 1.0 when straight
	}
	
	/**
	 * Indicates if the support onto which the accelerometer is attached is significantly tilted
	 * 
	 * @return true if the support onto which the accelerometer is attached is significantly tilted, false otherwise
	 */
	public boolean isFlat() {
		return getTilt() < config.TILT_THRESH_DEGREES; // TODO tweak threshold if needed
	}
	
}
