package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveTrain {

	private CANTalon lft, rft, lbt, rbt;
	private AnalogGyro gyro;
	public static boolean isMoving = false;
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 5;
	private static final double TIER_3_DEGREES_FROM_TARGET = 1;
	private static final double TURN_TIMEOUT_MILLIS = 10000;
	private static final double DEADZONE_LIMIT = 0.2;
	private static final double MOVE_THRESH = 50;
	private double endPosL, endPosR;

	public DriveTrain(CANTalon leftFront, CANTalon rightFront, CANTalon leftBack, CANTalon rightBack, AnalogGyro g) {
		lft = leftFront;
		rft = rightFront;
		lbt = leftBack;
		rbt = rightBack;
		lft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		rft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		lbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		rbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		lbt.set(lft.getDeviceID());
		rbt.set(rft.getDeviceID());
		gyro = g;
	}

	public void tankDrive(Joystick leftStick, Joystick rightStick) {
		if (!isMoving) {
			if (isOutOfDeadzone(leftStick, 1)) {
				lft.set(-leftStick.getRawAxis(1));
			} else {
				lft.set(0);
			}

			if (isOutOfDeadzone(rightStick, 1)) {
				rft.set(rightStick.getRawAxis(1));
			} else {
				rft.set(0);
			}
		} else {

			if (isOutOfDeadzone(leftStick, 1)) {

				isMoving = false; // driver takes over during CAN turning
				lft.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
				rft.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

			}

			if (isOutOfDeadzone(rightStick, 1)) {

				isMoving = false; // driver takes over during CAN turning
				lft.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
				rft.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

			}
		}
	}

	public void moveDistance(double startPosL, double startPosR, double changePos) {
		endPosL = startPosL + changePos;
		endPosR = startPosR - changePos;
		lft.setPID(0.6, 0.0000, 0.0);
		rft.setPID(0.6, 0.0000, 0.0);
		isMoving = true;
		lft.changeControlMode(CANTalon.TalonControlMode.Position);
		rft.changeControlMode(CANTalon.TalonControlMode.Position);
		lft.enableControl();
		rft.enableControl();
		lft.set(endPosL);
		rft.set(endPosR);

	}

	public boolean checkMove() {
		double leftVel = lft.getEncVelocity();
		double rightVel = rft.getEncVelocity();
		
		if (isMoving && (lft.getEncPosition() > endPosL - MOVE_THRESH && lft.getEncPosition() < endPosL + MOVE_THRESH)
				&& (rft.getEncPosition() > endPosR - MOVE_THRESH && rft.getEncPosition() < endPosR + MOVE_THRESH) && leftVel == 0
				&& rightVel == 0) {

			isMoving = false;
			lft.changeControlMode(TalonControlMode.PercentVbus);
			rft.changeControlMode(TalonControlMode.PercentVbus);
		}
		return isMoving;
	}

	/**
	 * @param s
	 *            - speed value to rotate; + value is CW, - value is CCW
	 */
	public void speedRotate(double s) {
		lft.set(s);
		rft.set(s);
	}

	/**
	 * Stops moving
	 */
	public void stop() {
		lft.set(0);
		rft.set(0);
	}

	/**
	 * 
	 * @param deg
	 *            - degree value to rotate
	 * @param s
	 *            - speed value to rotate
	 * 
	 *            Rotates robot a number of degrees at a certain speed
	 */
	public void degreeRotate(double deg, double s) {
		double startAngle = gyro.getAngle();
		double startTime = System.currentTimeMillis();
		if (deg > 0) {
			s *= -1;
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_1_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_2_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s / 2);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_3_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s / 4);
		}
		stop();
	}

	/**
	 * <pre>
	 * public boolean isOutOfDeadzone(Joystick j)
	 * </pre>
	 * 
	 * Returns true or false
	 * 
	 * @param j1
	 *            the first joystick to get the axis value from
	 * @param j2
	 *            the second joystick to get the axis value from
	 * @param axis
	 *            the axis value to be checked
	 * @return true if at least one axis is greater than deadzone, false
	 *         otherwise
	 */
	public boolean isOutOfDeadzone(Joystick j, int axis) {
		return (Math.abs(j.getRawAxis(axis)) > DEADZONE_LIMIT);
	}

}
