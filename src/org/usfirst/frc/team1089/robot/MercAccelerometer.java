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
	
	//TODO add method to calculate tilt as arcsine of (accel.getZ()/1.0)
}
