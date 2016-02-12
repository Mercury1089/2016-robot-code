package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.Joystick;

/**
 * The {@code DriveTrain} class handles movement with the drive base of the robot.
 */
public class DriveTrain {

	private CANTalon leftFrontTalon, rightFrontTalon, leftBackTalon, rightBackTalon;
	private AnalogGyro gyro;
	private static boolean isMoving = false; // indicates we are in (position)
												// control mode
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 5;
	private static final double TIER_3_DEGREES_FROM_TARGET = 1;
	private static final double TURN_TIMEOUT_MILLIS = 10000;
	private static final double DEADZONE_LIMIT = 0.4;
	private static final double MOVE_THRESH_TICKS = 50;
	private double endPosL, endPosR;
	private double startPosL, startPosR;
	private double changePosTicks;
	private Config config;
	private MercEncoder mercEncoder;

	/**
	 * <pre>
	 * public DriveTrain(CANTalon leftFront,
	 *                   CANTalon rightFront, 
	 *                   CANTalon leftBack, 
	 *                   CANTalon rightBack, 
	 *                   AnalogGyro g) 
	 * </pre>
	 * Constructs a new {@code DriveTrain} with the specified {@code CANTalons} for the wheels, and an {@code AnalogGyro} to check rotation.
	 * @param leftFront  the {@code CANTalon} controlling the left front wheel
	 * @param rightFront the {@code CANTalon} controlling the right front wheel
	 * @param leftBack   the {@code CANTalon} controlling the left back wheel
	 * @param rightBack  the {@code CANTalon} controlling the right back wheel
	 * @param g          the {@code AnalogGyro} used to track rotation
	 */
	public DriveTrain(CANTalon leftFront, CANTalon rightFront, CANTalon leftBack, CANTalon rightBack, AnalogGyro g) {
		config = Config.getCurrent();

		mercEncoder = new MercEncoder();
		leftFrontTalon = leftFront;
		rightFrontTalon = rightFront;
		leftBackTalon = leftBack;
		rightBackTalon = rightBack;
		leftFrontTalon.enableBrakeMode(true);
		rightFrontTalon.enableBrakeMode(true);
		leftBackTalon.enableBrakeMode(true);
		rightBackTalon.enableBrakeMode(true);
		leftFrontTalon.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		rightFrontTalon.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		leftBackTalon.changeControlMode(CANTalon.TalonControlMode.Follower);
		rightBackTalon.changeControlMode(CANTalon.TalonControlMode.Follower);
		leftBackTalon.set(leftFrontTalon.getDeviceID());
		rightBackTalon.set(rightFrontTalon.getDeviceID());
		gyro = g;
	}
	
	/**
	 * <pre>
	 * public void tankDrive(Joystick leftStick, 
	 *                       Joystick rightStick)
	 * </pre>
	 * Drives the base using a {@code Joystick} for the left set of wheels, 
	 * and another {@code Joystick} for the right set of wheels.
	 * @param leftStick  the {@code Joystick} to control the left set of wheels
	 * @param rightStick the {@code Joystick} to control the right set of wheels
	 */
	public void tankDrive(Joystick leftStick, Joystick rightStick) {
		if (isMoving) {
			if (!isOutOfDeadzone(leftStick, 1) && !isOutOfDeadzone(rightStick, 1)) {
				return; // we keep moving as no joystick has been grabbed
			} else {
				setToManual();
			}
		}

		if (isOutOfDeadzone(leftStick, 1)) {
			double rawValue = leftStick.getRawAxis(1);
			leftFrontTalon.set((rawValue - Math.signum(rawValue) * DEADZONE_LIMIT) / (1.0 - DEADZONE_LIMIT)
					* config.LEFT_DRIVE_SIGN);
		} else {
			leftFrontTalon.set(0);
		}

		if (isOutOfDeadzone(rightStick, 1)) {
			double rawValue = rightStick.getRawAxis(1);
			rightFrontTalon.set((rawValue - Math.signum(rawValue) * DEADZONE_LIMIT) / (1.0 - DEADZONE_LIMIT)
					* config.RIGHT_DRIVE_SIGN);
		} else {
			rightFrontTalon.set(0);
		}
	}

	/**
	 * <pre>
	 * public void moveDistance(double changePos)
	 * </pre>
	 * Moves the base based on encoder measurements by the specified distance in feet.
	 * <p>
	 * This is an asynchronous operation. Use waitMove() to wait for completion.
	 * </p>
	 * 
	 * @param changePos the distance to move in feet
	 */
	public void moveDistance(double changePos) {
		changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR + changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(0.6, 0.0000, 0.0);
		rightFrontTalon.setPID(0.6, 0.0000, 0.0);
		leftFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		leftFrontTalon.configNominalOutputVoltage(0, 0);
		rightFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		rightFrontTalon.configNominalOutputVoltage(0.0, 0.0);
		setToAuto();
		leftFrontTalon.enableControl();
		rightFrontTalon.enableControl();
		leftFrontTalon.set(endPosL);
		rightFrontTalon.set(endPosR);
	}

	/**
	 * <pre>
	 * public void turnDistance(double changePos)
	 * </pre>
	 * Turns the base based on encoders
	 * by the specified distance in feet alongside the arc created by the axle track.
	 * <p>
	 * This is an asynchronous operation. Use waitMove() to wait for completion.
	 * </p>
	 * 
	 * @param changePos the distance to turn in feet
	 */
	public void turnDistance(double changePos) {
		changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR - changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(0.3, 0.0001, 0.0);
		rightFrontTalon.setPID(0.3, 0.0001, 0.0);
		leftFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		leftFrontTalon.configNominalOutputVoltage(0, 0);
		rightFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		rightFrontTalon.configNominalOutputVoltage(0.0, 0.0);
		setToAuto();
		leftFrontTalon.enableControl();
		rightFrontTalon.enableControl();
		leftFrontTalon.set(endPosL);
		rightFrontTalon.set(endPosR);
	}

	/**
	 * <pre>
	 * public boolean checkMove()
	 * </pre>
	 * Checks to see if the robot is moving.
	 * @return true if the encoder speeds are 0, the {@code CANTalon} positions read within a certain threshold, and the robot is moving, false if otherwise.
	 */
	public boolean checkMove() {
		double leftVel = leftFrontTalon.getEncVelocity();
		double rightVel = rightFrontTalon.getEncVelocity();

		if (isMoving
				&& (leftFrontTalon.getEncPosition() > endPosL - MOVE_THRESH_TICKS
						&& leftFrontTalon.getEncPosition() < endPosL + MOVE_THRESH_TICKS)
				&& (rightFrontTalon.getEncPosition() > endPosR - MOVE_THRESH_TICKS
						&& rightFrontTalon.getEncPosition() < endPosR + MOVE_THRESH_TICKS)
				&& leftVel == 0 && rightVel == 0) {

			setToManual();
		}
		return isMoving;
	}
	
	/**
	 * <pre>
	 * public void waitMove()
	 * </pre>
	 * Hangs the process until the robot is not moving.
	 */
	public void waitMove() {
		while (checkMove()) {
			// do nothing
		}
	}

	/**
	 * <pre>
	 * public void speedRotate(double s)
	 * </pre>
	 * Rotates the robot at a specified speed.
	 * @param s speed value to rotate; 
	 *        positive values are clockwise, negative values are counterclockwise
	 */
	public void speedRotate(double s) {
		if (isMoving) {
			setToManual();
		}
		leftFrontTalon.set(s);
		rightFrontTalon.set(s);
	}

	/**
	 * <pre>
	 * public void stop()
	 * </pre>
	 * Sets both {@code CANTalon} speeds to 0.
	 */
	public void stop() {
		if (isMoving) {
			setToManual();
		}
		leftFrontTalon.set(0);
		rightFrontTalon.set(0);
	}

	/**
	 * <pre>
	 * public void degreeRotate(double deg, 
	 *                          double s)
	 * </pre>
	 * Rotates the robot to a specified amount of degrees at a certain speed.
	 * @param deg amount of degrees to rotate
	 * @param s speed to rotate at
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
	 * public boolean isOutOfDeadzone(Joystick j, 
	 *                                int axis)
	 * </pre>
	 * Gets whether or not the specified {@code Joystick} is out of the deadzone.
	 * @param j the {@code Joystick} to get the axis value from
	 * @param axis the axis to get a value from
	 * @return true if the axis value is out of the deadzone threshold, false otherwise
	 */
	public boolean isOutOfDeadzone(Joystick j, int axis) {
		return (Math.abs(j.getRawAxis(axis)) > DEADZONE_LIMIT);
	}

	/**
	 * <pre>
	 * public double arcLength(double angle)
	 * </pre>
	 * Gets the arc length of an angle based on the axle track.
	 * @param  angle the angle in degrees to convert to an arc length
	 * @return the arc length, in feet, of an angle based on the robot's axle track, in inches  
	 */
	public double arcLength(double angle) {
		return -Math.toRadians(angle) * (config.AXLE_TRACK_INCHES / 2) / 12;
	}

	/**
	 * <pre>
	 * private void setToManual()
	 * </pre>
	 * Sets the control modes of the front {@code CANTalons} to PercentVbus.
	 */
	private void setToManual() {
		isMoving = false;
		leftFrontTalon.changeControlMode(TalonControlMode.PercentVbus);
		rightFrontTalon.changeControlMode(TalonControlMode.PercentVbus);
	}

	/**
	 * <pre>
	 * private void setToAuto()
	 * </pre>
	 * Sets the control modes of the front {@code CANTalons} to Position.
	 */
	private void setToAuto() {
		isMoving = true;
		leftFrontTalon.changeControlMode(CANTalon.TalonControlMode.Position);
		rightFrontTalon.changeControlMode(CANTalon.TalonControlMode.Position);
	}

}
