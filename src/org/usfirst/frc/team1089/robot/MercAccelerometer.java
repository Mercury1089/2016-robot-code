package org.usfirst.frc.team1089.robot;

//import edu.wpi.first.wpilibj.AnalogAccelerometer;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;

public class MercAccelerometer {
	private BuiltInAccelerometer accel;
	
	public MercAccelerometer(){
		accel = new BuiltInAccelerometer(Accelerometer.Range.k4G);		
	}
	
	public double getAccelZ(){
		return accel.getZ();
	}
	
	public double getTilt() {
		return Math.toDegrees(Math.acos(getAccelZ()/1.0)); // assumes getAccelZ() returns 1.0 when straight
	}
	
}
