package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.Joystick;

public class DriveTrain {

	private CANTalon lft, rft, lbt, rbt;
	private AnalogGyro gyro;
	private static boolean isMoving = false; // indicates we are in (position) control mode
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 5;
	private static final double TIER_3_DEGREES_FROM_TARGET = 1;
	private static final double TURN_TIMEOUT_MILLIS = 10000;
	private static final double DEADZONE_LIMIT = 0.4;
	private static final double MOVE_THRESH_TICKS = 50;
	public static final double AXLE_TRACK_INCHES = 15.126*2; // TODO FIX THIS
	public static final double LEFT_ENC_SIGN = 1.0;
	public static final double RIGHT_ENC_SIGN = -1.0;
	public static final double LEFT_DRIVE_SIGN = -1.0;
	public static final double RIGHT_DRIVE_SIGN = 1.0;
	private double endPosL, endPosR;
	private double startPosL, startPosR;
	private double changePosTicks;

	public DriveTrain(CANTalon leftFront, CANTalon rightFront, CANTalon leftBack, CANTalon rightBack, AnalogGyro g) {
		Config config = Config.getCurrent();
		
		lft = leftFront;
		rft = rightFront;
		lbt = leftBack;
		rbt = rightBack;
		lft.enableBrakeMode(true);
		rft.enableBrakeMode(true);
		lbt.enableBrakeMode(true);
		rbt.enableBrakeMode(true);
		lft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		rft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		lbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		rbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		lbt.set(lft.getDeviceID());
		rbt.set(rft.getDeviceID());
		gyro = g;
	}

	public void tankDrive(Joystick leftStick, Joystick rightStick) {
		if (isMoving) {
			if (!isOutOfDeadzone(leftStick, 1) && !isOutOfDeadzone(rightStick, 1)) {
				return; // we keep moving as no joystick has been grabbed
			}
			else {
				setToManual();
			}
		}
	
		if (isOutOfDeadzone(leftStick, 1)) {
			double rawValue = leftStick.getRawAxis(1);
			lft.set((rawValue - Math.signum(rawValue)*DEADZONE_LIMIT) / (1.0 - DEADZONE_LIMIT) * LEFT_DRIVE_SIGN);
		} else {
			lft.set(0);
		}

		if (isOutOfDeadzone(rightStick, 1)) {
			double rawValue = rightStick.getRawAxis(1);
			rft.set((rawValue - Math.signum(rawValue)*DEADZONE_LIMIT) / (1.0 - DEADZONE_LIMIT) * RIGHT_DRIVE_SIGN);
		} else {
			rft.set(0);
		}
	}

	/**
	 * Moves by the specified distance in feet.
	 * <p>
	 * This is an asynchronous operation.  Use waitMove() to wait for completion.
	 * </p>
	 * 
	 * @param changePos the distance in feet
	 */
	public void moveDistance(double changePos) {
		changePosTicks = MercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = lft.getEncPosition();
		startPosR = rft.getEncPosition();
		endPosL = startPosL + changePosTicks;
		endPosR = startPosR + changePosTicks * RIGHT_ENC_SIGN;
		lft.setPID(0.6, 0.0000, 0.0);
		rft.setPID(0.6, 0.0000, 0.0);
		lft.configPeakOutputVoltage(12.0, -12.0);
		lft.configNominalOutputVoltage(0, 0);
		rft.configPeakOutputVoltage(12.0, -12.0);
		rft.configNominalOutputVoltage(0.0, 0.0);
		setToAuto();
		lft.enableControl();
		rft.enableControl();
		lft.set(endPosL);
		rft.set(endPosR);
	}

	/**
	 * Turns by the specified distance in feet alongside the arc created by the axle track.
	 * <p>
	 * This is an asynchronous operation.  Use waitMove() to wait for completion.
	 * </p
	 * 
	 * @param changePos the distance in feet
	 */
	public void turnDistance(double changePos) {
		changePosTicks = MercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = lft.getEncPosition();
		startPosR = rft.getEncPosition();
		endPosL = startPosL + changePosTicks;
		endPosR = startPosR - changePosTicks * RIGHT_ENC_SIGN;
		lft.setPID(0.3, 0.0001, 0.0);
		rft.setPID(0.3, 0.0001, 0.0);
		lft.configPeakOutputVoltage(6.0, -6.0);
		lft.configNominalOutputVoltage(0, 0);
		rft.configPeakOutputVoltage(6.0, -6.0);
		rft.configNominalOutputVoltage(0.0, 0.0);
		setToAuto();
		lft.enableControl();
		rft.enableControl();
		lft.set(endPosL);
		rft.set(endPosR);
	}

	public boolean checkMove() {
		double leftVel = lft.getEncVelocity();
		double rightVel = rft.getEncVelocity();

		if (isMoving && (lft.getEncPosition() > endPosL - MOVE_THRESH_TICKS && lft.getEncPosition() < endPosL + MOVE_THRESH_TICKS)
				&& (rft.getEncPosition() > endPosR - MOVE_THRESH_TICKS && rft.getEncPosition() < endPosR + MOVE_THRESH_TICKS)
				&& leftVel == 0 && rightVel == 0) {

			setToManual();
		}
		return isMoving;
	}
	
	public void waitMove() {
		while (checkMove()) {
			// do nothing
		}
	}

	/**
	 * @param s
	 *            speed value to rotate; + value is CW, - value is CCW
	 */
	public void speedRotate(double s) {
		if (isMoving) {
			setToManual();
		}		
		lft.set(s);
		rft.set(s);
	}

	/**
	 * Stops moving
	 */
	public void stop() {
		if (isMoving) {
			setToManual();
		}
		lft.set(0);
		rft.set(0);
	}

	/**
	 * 
	 * @param deg
	 *            degree value to rotate
	 * @param s
	 *            speed value to rotate
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
	 * Returns if joystick is out of dead zone
	 * 
	 * @param j
	 *            the joystick to get the axis value from
	 * @param axis
	 *            the axis value to be checked
	 * @return true if the axis is greater than deadzone, false
	 *         otherwise
	 */
	public boolean isOutOfDeadzone(Joystick j, int axis) {
		return (Math.abs(j.getRawAxis(axis)) > DEADZONE_LIMIT);
	}
	
	public static double arcLength(double angle) {
		return -Math.toRadians(angle) * (AXLE_TRACK_INCHES/2) / 12;
	}

	private void setToManual() {
		isMoving = false;
		lft.changeControlMode(TalonControlMode.PercentVbus);
		rft.changeControlMode(TalonControlMode.PercentVbus);
	}

	private void setToAuto() {
		isMoving = true;
		lft.changeControlMode(CANTalon.TalonControlMode.Position);
		rft.changeControlMode(CANTalon.TalonControlMode.Position);
	}

}
