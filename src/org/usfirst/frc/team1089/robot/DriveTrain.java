package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

/**
 * The {@code DriveTrain} class handles movement with the drive base of the
 * robot.
 */
public class DriveTrain {

	private CANTalon leftFrontTalon, rightFrontTalon, leftBackTalon, rightBackTalon;
	private AnalogGyro gyro;

	public static boolean isMoving = false; // indicates we are moving (in
											// position
											// control mode)
	private double startPosL, startPosR; // starting positions in position
											// control mode
	private double endPosL, endPosR; // ending positions in position control
										// mode

	public static boolean isDegreeRotating = false; // indicates we are rotating
													// (in gyro control mode)
	double _heading = 0.0; // heading when rotating

	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 12;
	private static final double TIER_3_DEGREES_FROM_TARGET = 6;
	private static final double TIER_4_DEGREES_FROM_TARGET = 1;
	private static final double AUTOROTATE_MAX_ACCEPTABLE_ANGLE_DEGREES = 1.5;
	private static final int AUTOROTATE_MAX_ATTEMPTS = 5;
	private static final double AUTOROTATE_SPEED = 0.77;
	private static final double AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS = 0.500;
	private static final double TURN_TIMEOUT_MILLIS = 4000;
	private static final double DEADZONE_LIMIT = 0.3;
	private static final double MOVE_THRESH_TICKS = 100;
	private static final double TURN_THRESH_VELOCITY = 10;
	private int autoRotCounter = 0;
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
	 * 
	 * Constructs a new {@code DriveTrain} with the specified {@code CANTalons}
	 * for the wheels, and an {@code AnalogGyro} to check rotation.
	 * 
	 * @param leftFront
	 *            the {@code CANTalon} controlling the left front wheel
	 * @param rightFront
	 *            the {@code CANTalon} controlling the right front wheel
	 * @param leftBack
	 *            the {@code CANTalon} controlling the left back wheel
	 * @param rightBack
	 *            the {@code CANTalon} controlling the right back wheel
	 * @param g
	 *            the {@code AnalogGyro} used to track rotation
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
	 * 
	 * Drives the base using a {@code Joystick} for the left set of wheels, and
	 * another {@code Joystick} for the right set of wheels.
	 * 
	 * @param leftStick
	 *            the {@code Joystick} to control the left set of wheels
	 * @param rightStick
	 *            the {@code Joystick} to control the right set of wheels
	 */
	public void tankDrive(Joystick leftStick, Joystick rightStick) {
		if (isMoving || isDegreeRotating) {
			if (!isOutOfDeadzone(leftStick, 1) && !isOutOfDeadzone(rightStick, 1)) {
				return; // we keep moving as no joystick has been grabbed
			} else {
				if (isMoving) {
					setToManual();
				} else { // if (isDegreeRotating)
					isDegreeRotating = false; // in case we were rotating
				}
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
	 * 
	 * Moves the base based on encoder measurements by the specified distance in
	 * feet.
	 * <p>
	 * This is an asynchronous operation. Use waitMove() to wait for completion.
	 * </p>
	 * 
	 * @param changePos
	 *            the distance to move in feet
	 */
	public void moveDistance(double changePos) {
		double changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR + changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(0.4, 0.0005, -0.001);
		rightFrontTalon.setPID(0.4, 0.0005, -0.001);
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
	 * 
	 * Turns the base based on encoders by the specified distance in feet
	 * alongside the arc created by the axle track.
	 * <p>
	 * This is an asynchronous operation. Use waitMove() to wait for completion.
	 * </p>
	 * 
	 * @param changePos
	 *            the distance to turn in feet
	 */
	public void turnDistance(double changePos) {
		double changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR - changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(0.3, 0.000, -0.000);
		rightFrontTalon.setPID(0.3, 0.000, -0.000);
		leftFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		leftFrontTalon.configNominalOutputVoltage(6, -6);
		rightFrontTalon.configPeakOutputVoltage(12.0, -12.0);
		rightFrontTalon.configNominalOutputVoltage(6, -6);
		leftFrontTalon.setCloseLoopRampRate(.01);
		rightFrontTalon.setCloseLoopRampRate(.01);
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
	 * 
	 * Checks to see if the robot is moving.
	 * 
	 * @return true if the encoder speeds are 0, the {@code CANTalon} positions
	 *         read within a certain threshold, and the robot is moving, false
	 *         if otherwise.
	 */
	public boolean checkMove() {
		double leftPos = leftFrontTalon.getEncPosition();
		double rightPos = rightFrontTalon.getEncPosition();
		double leftVel = leftFrontTalon.getEncVelocity();
		double rightVel = rightFrontTalon.getEncVelocity();

		if (isMoving) {
			SmartDashboard.putNumber("left velocity", leftVel);
			SmartDashboard.putNumber("right velocity", rightVel);
			SmartDashboard.putNumber("left pos", leftPos);
			SmartDashboard.putNumber("right pos", rightPos);

			if ((leftPos > endPosL - MOVE_THRESH_TICKS && leftPos < endPosL + MOVE_THRESH_TICKS)
					&& (rightPos > endPosR - MOVE_THRESH_TICKS && rightPos < endPosR + MOVE_THRESH_TICKS)
					&& Math.abs(leftVel) <= TURN_THRESH_VELOCITY && Math.abs(rightVel) <= TURN_THRESH_VELOCITY) {

				setToManual();
			}
		}
		return isMoving;
	}

	/**
	 * <pre>
	 * public void waitMove()
	 * </pre>
	 * 
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
	 * 
	 * Rotates the robot at a specified speed.
	 * 
	 * @param s
	 *            speed value to rotate; positive values are clockwise, negative
	 *            values are counterclockwise
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
	 * 
	 * Sets both {@code CANTalon} speeds to 0.
	 */
	public void stop() {
		if (isMoving) {
			setToManual();
		}
		isDegreeRotating = false;
		leftFrontTalon.set(0);
		rightFrontTalon.set(0);
	}

	/**
	 * <pre>
	 * public void degreeRotate(double deg, 
	 *                          double s)
	 * </pre>
	 * 
	 * Rotates the robot to a specified amount of degrees at a certain speed.
	 * 
	 * @param deg
	 *            amount of degrees to rotate
	 * @param s
	 *            speed to rotate at
	 */
	public void degreeRotate(double deg, double s) {
		double startAngle = gyro.getAngle();
		double startTime = System.currentTimeMillis();
		//isMoving = true;
		if (deg < 0) {
			s *= -1; // speed sign same as desired angle
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_1_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_2_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s / 1.75);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_3_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s / 1.90);
		}
		while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_4_DEGREES_FROM_TARGET)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
			speedRotate(s / 2.0);
		}
		stop();
		//isMoving = false;
	}
	
	/**
	 * Calls degreeRotate() if not in correct angle.
	 * 
	 * Network Table info is fetched prior to returning
	 * 
	 * @param c the camera to get the angle from 
	 */
	public void autoRotate(Camera c) {
		autoRotCounter = 0;
		double deg = 0;
		double startTime = System.currentTimeMillis();
		do {
			c.getNTInfo();
			deg = c.getTurnAngle();
			degreeRotate(deg, AUTOROTATE_SPEED);
			Timer.delay(AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
			autoRotCounter++;
		} while ((Math.abs(deg) > AUTOROTATE_MAX_ACCEPTABLE_ANGLE_DEGREES)
				&& (autoRotCounter <= AUTOROTATE_MAX_ATTEMPTS)
				&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS * 2)); 
		
		// finally we we refresh the network table info
		// in case this routine is called directly from shootProcedure
		c.getNTInfo();
	}
	
	/**
	 * Calls degreeRotate() if not in correct angle.
	 * 
	 * Network Table info is fetched prior to returning
	 * 
	 * @param c the camera to get the angle from 
	 */
	public void autoRotateNew(Camera c) {
		autoRotCounter = 0;
		double earliestStartTime = System.currentTimeMillis();
		c.getNTInfo();
		double deg = c.getTurnAngle(); // delta from initial position
		double setpoint = deg + gyro.getAngle(); // setpoint
		double s;
		
		do {
			double startTime = System.currentTimeMillis();
			double startAngle = gyro.getAngle();
			if (deg > 0) {
				s = AUTOROTATE_SPEED; // speed sign same as desired angle
			} else {
				s = -AUTOROTATE_SPEED; // speed sign same as desired angle
			}

			while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_1_DEGREES_FROM_TARGET)
					&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
				speedRotate(s);
			}
			while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_2_DEGREES_FROM_TARGET)
					&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
				speedRotate(s / 1.75);
			}
			while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_3_DEGREES_FROM_TARGET)
					&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
				speedRotate(s / 1.90);
			}
			while ((Math.abs(gyro.getAngle() - startAngle) < Math.abs(deg) - TIER_4_DEGREES_FROM_TARGET)
					&& (System.currentTimeMillis() - startTime <= TURN_TIMEOUT_MILLIS)) {
				speedRotate(s / 2.0);
			}
			stop(); // we stop so that startAngle and deg are in sync if we loop again
			
			// calculates new delta based on setpoint and current position
			deg = setpoint - gyro.getAngle();
			
			autoRotCounter++;
		} while ((Math.abs(deg) > AUTOROTATE_MAX_ACCEPTABLE_ANGLE_DEGREES)
				&& (autoRotCounter <= AUTOROTATE_MAX_ATTEMPTS)
				&& (System.currentTimeMillis() - earliestStartTime <= TURN_TIMEOUT_MILLIS * 2));
		
		// finally we force a delay and we refresh the network table info
		// in case this routine is called directly from shootProcedure
		Timer.delay(AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
		c.getNTInfo();
	}

	/**
	 * <pre>
	 * public void degreeRotateVoltage(double heading)
	 * </pre>
	 * 
	 * Turns the base based to the specified heading
	 * <p>
	 * This is an asynchronous operation. Use waitDegreeRotateVoltage() to wait
	 * for completion.
	 * </p>
	 * 
	 * @param heading
	 *            the heading in degree
	 */
	public void degreeRotateVoltage(double heading) {
		isDegreeRotating = true; // we flag that we are rotating asynchronously
		gyro.reset(); // we start at zero since heading is relative to where we
						// are
						// (but we could also save the start angle and subtract
						// in check method)
		_heading = heading; // we save where we want to go
	}

	public boolean checkDegreeRotateVoltage() {
		if (isDegreeRotating) { // only if we have been told to rotate
			double vmax = Math.pow(0.75, 1.0);		//change to 3 for cubic
			double vmin = Math.pow(0.35, 1.0);
			double dmax = 20.0;
			double dmin = 5.0;
			double error = _heading - gyro.getAngle();
			double kp = (vmax - vmin) / (dmax - dmin);
			/*// speed sign same as desired angle
			double vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, kp*(Math.abs(error));
			vout = Math.pow(vout, 1.0/3);*/
			double vout = 0;
			
			
			if(error > config.TURN_ANGLE_MAX_DEGREES){
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error-5))));
			}		
			else if(error < config.TURN_ANGLE_MIN_DEGREES){
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error+5))));
			}
			
			if (Math.abs(error) <= AUTOROTATE_MAX_ACCEPTABLE_ANGLE_DEGREES) {
				isDegreeRotating = false; // we take the flag down
				stop(); // we stop the motors
			} else {
				speedRotate(vout); // we rotate until we are told otherwise
			}
		}
		return isDegreeRotating;
	}

	/**
	 * <pre>
	 * public void waitDegreeRotateVoltage()
	 * </pre>
	 * 
	 * Hangs the process until the robot is not rotating.
	 */
	public void waitDegreeRotateVoltage() {
		while (checkDegreeRotateVoltage()) {
			// do nothing
		}
	}

	public void encoderAngleRotate(double rotDegrees) {
		turnDistance(arcLength(rotDegrees));
	}

	/**
	 * <pre>
	 * public boolean isOutOfDeadzone(Joystick j, 
	 *                                int axis)
	 * </pre>
	 * 
	 * Gets whether or not the specified {@code Joystick} is out of the
	 * deadzone.
	 * 
	 * @param j
	 *            the {@code Joystick} to get the axis value from
	 * @param axis
	 *            the axis to get a value from
	 * @return true if the axis value is out of the deadzone threshold, false
	 *         otherwise
	 */
	public boolean isOutOfDeadzone(Joystick j, int axis) {
		return (Math.abs(j.getRawAxis(axis)) > DEADZONE_LIMIT);
	}

	/**
	 * <pre>
	 * public double arcLength(double angle)
	 * </pre>
	 * 
	 * Gets the arc length of an angle based on the axle track.
	 * 
	 * @param angle
	 *            the angle in degrees to convert to an arc length
	 * @return the arc length, in feet, of an angle based on the robot's axle
	 *         track, in inches
	 */
	public double arcLength(double angle) {
		return -Math.toRadians(angle) * (config.AXLE_TRACK_INCHES / 2) / 12;
	}

	/**
	 * <pre>
	 * private void setToManual()
	 * </pre>
	 * 
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
	 * 
	 * Sets the control modes of the front {@code CANTalons} to Position.
	 */
	private void setToAuto() {
		isMoving = true;
		leftFrontTalon.changeControlMode(CANTalon.TalonControlMode.Position);
		rightFrontTalon.changeControlMode(CANTalon.TalonControlMode.Position);
	}

}
