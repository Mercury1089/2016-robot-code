package org.usfirst.frc.team1089.robot;

import java.util.Calendar;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
//import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.PIDController;
//import edu.wpi.first.wpilibj.PIDSourceType;

/**
 * The {@code DriveTrain} class handles movement with the drive base of the
 * robot.
 */
public class DriveTrain {

	private CANTalon leftFrontTalon, rightFrontTalon, leftBackTalon, rightBackTalon;
	private AnalogGyro gyro;

	private boolean isMoving = false; // indicates we are moving (in
											// position
											// control mode)
	private double startPosL, startPosR; // starting positions in position
											// control mode
	private double endPosL, endPosR; // ending positions in position control
										// mode

	private boolean isDegreeRotating = false; // indicates we are rotating
													// (in gyro control mode)
	private double _heading = 0.0; // heading when rotating
	private long _heading_display_reset_time_ms = 0; // to log time 
	private double _rotate_vmin_adjuster = 0.0; // to adjust vmin dynamically
	private static final double STARTING_VMIN = 0.36;
	long rotateStartMs;

	private static final long ROTATE_INCREASE_DELAY_MS = 1000;
	private static final double MAX_VMIN_ADJUSTER = .20;
	private static final double MIN_VMIN_ADJUSTER = -.10;
	
	private static final double ROTATE_CHECK_PERIOD_MS = 1000;
	private static final double ROTATE_VMIN_ADJUSTER_INCREMENT = 0.01; // what we add to vmin 
	private static final double GYRO_RATE_MIN = 10;
	
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 12; //15;
	private static final double TIER_3_DEGREES_FROM_TARGET = 6;
	private static final double TIER_4_DEGREES_FROM_TARGET = 1;
	//private static final double AUTOROTATE_MAX_ACCEPTABLE_ANGLE_DEGREES = 1.0;
	//private static final int AUTOROTATE_MAX_ATTEMPTS = 5;
	private static final double AUTOROTATE_SPEED = 0.77;
	public static final double AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS = 0.500;
	//private static final double TURN_TIMEOUT_MILLIS = 4000;
	private static final double DEADZONE_LIMIT = 0.3;
	private static final double MOVE_THRESH_TICKS = 500;
	private static final double TURN_THRESH_VELOCITY = 10;

	private static final long WAIT_MOVE_OR_ROTATE_TIMEOUT_MS = 15000;

	private static DriverStation ds = DriverStation.getInstance();

	//private int autoRotCounter = 0;
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
		config = Config.getInstance();

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
	 * public boolean checkDegreeRotateVoltage()
	 * </pre>
	 *
	 * Checks to see if the robot is rotating.
	 *
	 * @return true if the robot is rotating, false
	 *         if otherwise.
	 */ 
	public boolean checkDegreeRotateVoltage() {
		if (isDegreeRotating) { // only if we have been told to rotate
			final double BOOST = 301.0; //3.0; //change to 1 for linear, 3 for cubic
			double vmax = Math.pow(0.7, 1.0/BOOST); // WAS 0.77 FOR OLD DRIVETRAIN, BUT CONSIDER REDUCING FURTHER (E.G. 0.67 IF KEEPING VMIN AS 0.27)
			double vmin = Math.pow(STARTING_VMIN + _rotate_vmin_adjuster, 1.0/BOOST); // WAS 0.37 FOR OLD DRIVETRAIN. MIGHT BE BORDERLINE TOO SMALL?
			double dmax = 45.0; // WAS 60.0 FOR OLD DRIVETRAIN - CONSIDER INCREASING A LITTLE (OR EVEN PUT 60 BACK TO MAKE IT EASIER TO TUNE)
			double dmin = 0.0; // 5.0;
			double error = _heading - gyro.getAngle();
			double kp = (vmax - vmin) / (dmax - dmin);
			// speed sign same as desired angle
			double vout = 0;
			double offset = 0; // 5

			if (error > config.TURN_ANGLE_MAX_DEGREES) {
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error-offset))));
				vout = Math.pow(vout, BOOST);
				speedRotate(vout); // we rotate until we are told otherwise
				
				if ((Calendar.getInstance().getTimeInMillis() - _heading_display_reset_time_ms) > ROTATE_CHECK_PERIOD_MS) {
					Logger.log("DriveTrain.checkDegreeRotateVoltage: rotating (positive error)");
					Logger.log("Desired heading is: " + _heading);
					Logger.log("Gyro reports an angle of: " + gyro.getAngle());
					Logger.log("Gyro reports a rate of: " + gyro.getRate());
					Logger.log("Normalized voltage currently at: " + vout);
					Logger.log("Vmin adjuster is: " + _rotate_vmin_adjuster);
					_heading_display_reset_time_ms = Calendar.getInstance().getTimeInMillis();
				}
				
				if ((System.currentTimeMillis() - rotateStartMs) > ROTATE_INCREASE_DELAY_MS && Math.abs(gyro.getRate()) < GYRO_RATE_MIN) { // only if we are slowly rotating
					Logger.log("DriveTrain.checkDegreeRotateVoltage: Turning rate below threshold.");
					Logger.log("Desired heading is: " + _heading);
					Logger.log("Gyro reports an angle of: " + gyro.getAngle());
					Logger.log("Gyro reports a rate of: " + gyro.getRate());
					Logger.log("Normalized voltage currently at: " + vout);
					rotateStartMs = System.currentTimeMillis();
					raiseVMinAdjuster();
				}
			}
			else if (error < config.TURN_ANGLE_MIN_DEGREES) {
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error+offset))));
				vout = Math.pow(vout, BOOST);
				speedRotate(vout); // we rotate until we are told otherwise
				
				if ((Calendar.getInstance().getTimeInMillis() - _heading_display_reset_time_ms) > ROTATE_CHECK_PERIOD_MS) {
					Logger.log("DriveTrain.checkDegreeRotateVoltage: rotating (negative error)");
					Logger.log("Desired heading is: " + _heading);
					Logger.log("Gyro reports an angle of: " + gyro.getAngle());
					Logger.log("Gyro reports a rate of: " + gyro.getRate());
					Logger.log("Normalized voltage currently at: " + vout);
					_heading_display_reset_time_ms = Calendar.getInstance().getTimeInMillis();	
					Logger.log("Vmin adjuster is: " + _rotate_vmin_adjuster);
				}
				
				if ((System.currentTimeMillis() - rotateStartMs) > ROTATE_INCREASE_DELAY_MS && Math.abs(gyro.getRate()) < GYRO_RATE_MIN) { // only if we are slowly rotating
					Logger.log("DriveTrain.checkDegreeRotateVoltage: Turning rate below threshold.");
					Logger.log("Desired heading is: " + _heading);
					Logger.log("Gyro reports an angle of: " + gyro.getAngle());
					Logger.log("Gyro reports a rate of: " + gyro.getRate());
					Logger.log("Normalized voltage currently at: " + vout);
					rotateStartMs = System.currentTimeMillis();
					raiseVMinAdjuster();
				}
			}
			else {
				Logger.log("DriveTrain.checkDegreeRotateVoltage: done rotating");
				Logger.log("Desired heading was: " + _heading);
				Logger.log("Gyro reports a post-rotation angle of: " + gyro.getAngle());
				Logger.log("Gyro reports a post-rotation rate of: " + gyro.getRate());
				Logger.log("Normalized voltage used on final approach was : " + vout);
				Logger.log("Vmin adjuster is: " + _rotate_vmin_adjuster);
				isDegreeRotating = false; // we take the flag down
				stop(); // we stop the motors
			}
		}
		return isDegreeRotating;
	}

	public void turnDistance(double changePos) {
		double changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR - changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(0.2, 0.000, -0.000);
		rightFrontTalon.setPID(0.2, 0.000, -0.000);
		leftFrontTalon.configPeakOutputVoltage(9.0, -9.0);
		leftFrontTalon.configNominalOutputVoltage(4, -4);
		rightFrontTalon.configPeakOutputVoltage(9.0, -9.0);
		rightFrontTalon.configNominalOutputVoltage(4, -4);
		leftFrontTalon.setCloseLoopRampRate(.01);
		rightFrontTalon.setCloseLoopRampRate(.01);
		setToAuto();
		leftFrontTalon.enableControl();
		rightFrontTalon.enableControl();
		leftFrontTalon.set(endPosL);
		rightFrontTalon.set(endPosR);
	}

	public boolean checkMove2() {
		double leftPos = leftFrontTalon.getEncPosition();
		double rightPos = rightFrontTalon.getEncPosition();
		double leftVel = leftFrontTalon.getEncVelocity();
		double rightVel = rightFrontTalon.getEncVelocity();

		if (isMoving) {
			/*
			 * SmartDashboard.putNumber("left velocity", leftVel);
			 * SmartDashboard.putNumber("right velocity", rightVel);
			 * SmartDashboard.putNumber("left pos", leftPos);
			 * SmartDashboard.putNumber("right pos", rightPos);
			 */

			if ((leftPos > endPosL - MOVE_THRESH_TICKS && leftPos < endPosL + MOVE_THRESH_TICKS)
					&& (rightPos > endPosR - MOVE_THRESH_TICKS && rightPos < endPosR + MOVE_THRESH_TICKS)
					&& Math.abs(leftVel) <= TURN_THRESH_VELOCITY && Math.abs(rightVel) <= TURN_THRESH_VELOCITY) {

				Logger.log("DriveTrain.checkMove2: done moving, setting to manual");
				setToManual();
			}
		}
		return isMoving;
	}
	
	
	public boolean checkDegreeRotateVoltagePractice() {					//DMAX = 60 is best!
		if (isDegreeRotating) { // only if we have been told to rotate
			final double BOOST = 301.0; //3.0; //change to 1 for linear, 3 for cubic
			double vmax = Math.pow(0.7, 1.0/BOOST); // 0.75
			double vmin = Math.pow(0.27, 1.0/BOOST); // 0.35
			double dmax = 60.0; // 25.0; // 20.0; // TODO ALSO TRY 60.0
			double dmin = 0.0; // 5.0;
			double error = _heading - gyro.getAngle();
			double kp = (vmax - vmin) / (dmax - dmin);
			// speed sign same as desired angle
			double vout = 0;
			double offset = 0; // 5

			if (error > config.TURN_ANGLE_MAX_DEGREES) {
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error-offset))));
				vout = Math.pow(vout, BOOST);
				speedRotate(vout); // we rotate until we are told otherwise
			}
			else if (error < config.TURN_ANGLE_MIN_DEGREES) {
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error+offset))));
				vout = Math.pow(vout, BOOST);
				speedRotate(vout); // we rotate until we are told otherwise
			}
			else {
				Logger.log("DriveTrain.checkDegreeRotateVoltagePractice: done rotating");
				isDegreeRotating = false; // we take the flag down
				stop(); // we stop the motors
			}
		}
		return isDegreeRotating;
	}
	/**
	 * <pre>
	 * public boolean checkDegreeRotateVoltageNew()
	 * </pre>
	 *
	 * Checks to see if the robot is rotating.
	 *
	 * @return true if the robot is rotating, false
	 *         if otherwise.
	 */
	public boolean checkDegreeRotateVoltageNew() {
		if (isDegreeRotating) { // only if we have been told to rotate
			double deg = _heading - gyro.getAngle();
			double s = 0;

			if (deg > 0) {
				s = AUTOROTATE_SPEED; // speed sign same as desired angle
			} else {
				s = -AUTOROTATE_SPEED; // speed sign same as desired angle
			}

			if ((Math.abs(gyro.getAngle()) < Math.abs(_heading) - TIER_1_DEGREES_FROM_TARGET)) {
				speedRotate(s);
			}
			else if ((Math.abs(gyro.getAngle()) < Math.abs(_heading) - TIER_2_DEGREES_FROM_TARGET)) {
				speedRotate(s / 1.75);
			}
			else if ((Math.abs(gyro.getAngle()) < Math.abs(_heading) - TIER_3_DEGREES_FROM_TARGET)) {
				speedRotate(s / 1.90);
			}
			else if ((Math.abs(gyro.getAngle()) < Math.abs(_heading) - TIER_4_DEGREES_FROM_TARGET)) {
				speedRotate(s / 2.0);
			}
			else {
				Logger.log("DriveTrain.checkDegreeRotateVoltageNew: done rotating");
				isDegreeRotating = false; // we take the flag down
				stop(); // we stop the motors
			}
		}
		return isDegreeRotating;
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
			/*SmartDashboard.putNumber("left velocity", leftVel);
			SmartDashboard.putNumber("right velocity", rightVel);
			SmartDashboard.putNumber("left pos", leftPos);
			SmartDashboard.putNumber("right pos", rightPos);*/

			if ((leftPos > endPosL - MOVE_THRESH_TICKS && leftPos < endPosL + MOVE_THRESH_TICKS)
					&& (rightPos > endPosR - MOVE_THRESH_TICKS && rightPos < endPosR + MOVE_THRESH_TICKS)
					&& Math.abs(leftVel) <= TURN_THRESH_VELOCITY && Math.abs(rightVel) <= TURN_THRESH_VELOCITY) {

				Logger.log("DriveTrain.checkMove: done moving, setting to manual");
				setToManual();
			}
		}
		return isMoving;
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
	public void degreeRotateVoltage(double heading) {				//THE RAJ METHOD
		isDegreeRotating = true; // we flag that we are rotating asynchronously
		rotateStartMs = System.currentTimeMillis();
		
		gyro.reset(); // we start at zero since heading is relative to where we
						// are
						// (but we could also save the start angle and subtract
						// in check method)
		_heading = heading; // we save where we want to go
		_heading_display_reset_time_ms = Calendar.getInstance().getTimeInMillis();
		//_rotate_vmin_adjuster = 0.0; // we keep the value of multiple rotations
	}
	
	/**
	 * <pre>
	 * public void degreeRotateVoltageNew(double heading)
	 * </pre>
	 *
	 * Turns the base based to the specified heading
	 * <p>
	 * This is an asynchronous operation. Use waitDegreeRotateVoltageNew() to wait
	 * for completion.
	 * </p>
	 *
	 * @param heading
	 *            the heading in degree
	 */
	public void degreeRotateVoltageNew(double heading) {
		isDegreeRotating = true; // we flag that we are rotating asynchronously
		gyro.reset(); // we start at zero since heading is relative to where we
						// are
						// (but we could also save the start angle and subtract
						// in check method)
		_heading = heading; // we save where we want to go
		_heading_display_reset_time_ms = Calendar.getInstance().getTimeInMillis();
		_rotate_vmin_adjuster = 0.0;

		//insert initial kick here if needed
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
	 * @param p
	 * 			proportional coefficient
	 * @param i
	 * 			integral coefficient
	 * @param d
	 * 			derivative coefficient
	 * @param maxV
	 * 			maximum voltage           
	 */

	public void moveDistance(double changePos, double p, double i, double d, double maxV) {
		double changePosTicks = mercEncoder.convertDistanceToEncoderTicks(changePos, 1.0);
		startPosL = leftFrontTalon.getEncPosition();
		startPosR = rightFrontTalon.getEncPosition();
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR + changePosTicks * config.RIGHT_ENC_SIGN;
		leftFrontTalon.setPID(p, i, d);
		rightFrontTalon.setPID(p, i, d);
		leftFrontTalon.configPeakOutputVoltage(maxV, -maxV);
		leftFrontTalon.configNominalOutputVoltage(0, 0);
		rightFrontTalon.configPeakOutputVoltage(maxV, -maxV);
		rightFrontTalon.configNominalOutputVoltage(0.0, 0.0);
		setToAuto();
		leftFrontTalon.enableControl();
		rightFrontTalon.enableControl();
		leftFrontTalon.set(endPosL);
		rightFrontTalon.set(endPosR);
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
		setToManual();
		isDegreeRotating = false;
		leftFrontTalon.set(0);
		rightFrontTalon.set(0);
	}
	
	public void raiseVMinAdjuster() {
		if(_rotate_vmin_adjuster < MAX_VMIN_ADJUSTER) {
			_rotate_vmin_adjuster += ROTATE_VMIN_ADJUSTER_INCREMENT;
			Logger.log("Changed (raised) Rotate Vmin Adjuster to " + _rotate_vmin_adjuster);
		}
	}
	
	public void lowerVMinAdjuster() {
		if(_rotate_vmin_adjuster > MIN_VMIN_ADJUSTER) {
		  _rotate_vmin_adjuster -= ROTATE_VMIN_ADJUSTER_INCREMENT;
		  Logger.log("Changed (lowered) Rotate Vmin Adjuster to " + _rotate_vmin_adjuster);
		}
	}
	
	public void resetVMinAdjuster() {
		_rotate_vmin_adjuster = 0;
		Logger.log("Changed (reset) Rotate Vmin Adjuster to " + _rotate_vmin_adjuster);
	}
	
	public double getVMinTotal() {
		return STARTING_VMIN + _rotate_vmin_adjuster;
	}
	
	public double getVMinStarting() {
		return STARTING_VMIN;
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
		if (!isMoving && !isDegreeRotating) {
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
	}

	/**
	 * <pre>
	 * public void waitDegreeRotateVoltage()
	 * </pre>
	 *
	 * Hangs the process until the robot is not rotating.
	 */
	public void waitDegreeRotateVoltage() {
		long start = Calendar.getInstance().getTimeInMillis();
		// Assumes we only use in Auton
		while (checkDegreeRotateVoltage()) {
			if(!ds.isAutonomous() || (Calendar.getInstance().getTimeInMillis()  - start) >= WAIT_MOVE_OR_ROTATE_TIMEOUT_MS) {
				Logger.log("DriveTrain.waitDegreeRotateVoltage: TIMEOUT!");
				stop(); // we stop everything
				break;
			}
		}
	}

	/**
	 * <pre>
	 * public void waitDegreeRotateVoltageNew()
	 * </pre>
	 *
	 * Hangs the process until the robot is not rotating.
	 *
	public void waitDegreeRotateVoltageNew() {
		while (checkDegreeRotateVoltageNew()) {
			// do nothing
		}
	}
	 */

	/**
	 * <pre>
	 * public void waitMove()
	 * </pre>
	 *
	 * Hangs the process until the robot is not moving.
	 */
	public void waitMove() {
		long start = Calendar.getInstance().getTimeInMillis();
		// Assumes we only use in Auton
		while (checkMove()) {
			if(!ds.isAutonomous() || (Calendar.getInstance().getTimeInMillis()  - start) >= WAIT_MOVE_OR_ROTATE_TIMEOUT_MS) {
				Logger.log("DriveTrain.waitMove: TIMEOUT!");
				stop();
				break;
			};
		}
	}
}
