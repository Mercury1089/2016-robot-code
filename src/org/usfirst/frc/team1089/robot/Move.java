package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.RobotDrive;

public class Move {	
	private RobotDrive drive;
	private AnalogGyro gyro;
	
	public Move(RobotDrive d, AnalogGyro g) {
		gyro = g;
		drive = d;
	}
	
	
	/**
	 * @param s - speed value to rotate; + value is CW, - value is CCW
	 */
	public void speedRotate(double s) {
		drive.tankDrive(s, -s);
	}
	/**
	 * Stops moving
	 */
	public void stop() {
		drive.tankDrive(0, 0);
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
		if (deg < 0){
			s *= -1;
		}
		while (Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - 20) {
			speedRotate(s);
		}
		while (Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - 5) {
			speedRotate(s/2);
		}
		stop();
	}
}
