package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.RobotDrive;

public class Move {	
	private RobotDrive drive;
	private CANTalon leftTalon, rightTalon;
	private AnalogGyro gyro;
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 5;
	private static final double TIER_3_DEGREES_FROM_TARGET = 2;
	private static final double TURN_TIMEOUT_MILLIS = 10000;
	public Move(CANTalon lM, CANTalon rM, AnalogGyro g) {
		gyro = g;
		leftTalon = lM;
		rightTalon = rM;
		
	}
	
	
	/**
	 * @param s - speed value to rotate; + value is CW, - value is CCW
	 */
	public void speedRotate(double s) {
		leftTalon.set(s);
		rightTalon.set(s);
	}
	/**
	 * Stops moving
	 */
	public void stop() {
		leftTalon.set(0);
		rightTalon.set(0);
	}
	
	/**
	 * 
	 * @param deg - degree value to rotate
	 * @param s - speed value to rotate
	 * 
	 * Rotates robot a number of degrees at a certain speed
	 */
	public void degreeRotate(double deg, double s) {
		double startAngle = gyro.getAngle();
		double startTime = System.currentTimeMillis();
		if (deg > 0){
			s *= -1;
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_1_DEGREES_FROM_TARGET) && 
				(System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_2_DEGREES_FROM_TARGET) && 
				(System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s/2);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_3_DEGREES_FROM_TARGET) && 
				(System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s/4);
		}
		stop();
	}	
}
