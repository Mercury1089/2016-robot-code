package org.usfirst.frc.team1089.robot;

import java.util.Calendar;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.AnalogGyro;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;


import edu.wpi.first.wpilibj.DriverStation;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;

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

	private WPI_TalonSRX leftFrontTalon, rightFrontTalon, leftBackTalon, rightBackTalon;
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
	private static final double STARTING_VMIN = 0.22; //changed from normal value of .17
	long rotateStartMs;

	private static final long ROTATE_INCREASE_DELAY_MS = 1000;
	private static final double MAX_VMIN_ADJUSTER = .20;
	private static final double MIN_VMIN_ADJUSTER = -.20;
	
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

	private static final int TALON_TIMEOUT_MS = 10;
	static final int PRIMARY_PID_LOOP = 0;

	
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
	 *            the {@code WPI_TalonSRX} controlling the left front wheel
	 * @param rightFront
	 *            the {@code WPI_TalonSRX} controlling the right front wheel
	 * @param leftBack
	 *            the {@code WPI_TalonSRX} controlling the left back wheel
	 * @param rightBack
	 *            the {@code WPI_TalonSRX} controlling the right back wheel
	 * @param g
	 *            the {@code WPI_TalonSRX} used to track rotation
	 */
	public DriveTrain(WPI_TalonSRX leftFront, WPI_TalonSRX rightFront, WPI_TalonSRX leftBack, WPI_TalonSRX rightBack, AnalogGyro g) {
		config = Config.getInstance();

		mercEncoder = new MercEncoder();
		leftFrontTalon = leftFront;
		rightFrontTalon = rightFront;
		leftBackTalon = leftBack;
		rightBackTalon = rightBack;
		leftFrontTalon.setNeutralMode(NeutralMode.Brake);
		rightFrontTalon.setNeutralMode(NeutralMode.Brake);
		leftBackTalon.setNeutralMode(NeutralMode.Brake);
		rightBackTalon.setNeutralMode(NeutralMode.Brake);
		leftFrontTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, PRIMARY_PID_LOOP, TALON_TIMEOUT_MS);
		rightFrontTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, PRIMARY_PID_LOOP, TALON_TIMEOUT_MS);
		leftBackTalon.follow(leftFrontTalon);
		rightBackTalon.follow(rightFrontTalon);
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
	public boolean checkDegreeRotateVoltage(double turnAngleThreshMin, double turnAngleThreshMax) {
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

			if (error > turnAngleThreshMax) {
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
					Logger.log("The current battery voltage: " + ds.getBatteryVoltage());
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
			else if (error < turnAngleThreshMin) {
				vout = Math.signum(error) * Math.min(vmax, Math.max(vmin, vmin + kp*(Math.abs(error+offset))));
				vout = Math.pow(vout, BOOST);
				speedRotate(vout); // we rotate until we are told otherwise
				
				if ((Calendar.getInstance().getTimeInMillis() - _heading_display_reset_time_ms) > ROTATE_CHECK_PERIOD_MS) {
					Logger.log("DriveTrain.checkDegreeRotateVoltage: rotating (negative error)");
					Logger.log("Desired heading is: " + _heading);
					Logger.log("Gyro reports an angle of: " + gyro.getAngle());
					Logger.log("Gyro reports a rate of: " + gyro.getRate());
					Logger.log("Normalized voltage currently at: " + vout);
					Logger.log("Vmin adjuster is: " + _rotate_vmin_adjuster);
					Logger.log("The current battery voltage: " + ds.getBatteryVoltage());
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
		double leftPos = leftFrontTalon.getSelectedSensorPosition(0);
		double rightPos = rightFrontTalon.getSelectedSensorPosition(0);
		double leftVel = leftFrontTalon.getSelectedSensorVelocity(0);
		double rightVel = rightFrontTalon.getSelectedSensorVelocity(0);;

		if (isMoving) {
			/*SmartDashboard.putNumber("left velocity", leftVel);
			SmartDashboard.putNumber("right velocity", rightVel);
			SmartDashboard.putNumber("left pos", leftPos);
			SmartDashboard.putNumber("right pos", rightPos);*/

			if ((leftPos > endPosL - MOVE_THRESH_TICKS && leftPos < endPosL + MOVE_THRESH_TICKS)
					&& (rightPos > endPosR - MOVE_THRESH_TICKS && rightPos < endPosR + MOVE_THRESH_TICKS)
					&& Math.abs(leftVel) <= TURN_THRESH_VELOCITY && Math.abs(rightVel) <= TURN_THRESH_VELOCITY) {

				Logger.log("DriveTrain.checkMove: done moving, setting to manual");
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
		startPosL = leftFrontTalon.getSelectedSensorPosition(0);
		startPosR = rightFrontTalon.getSelectedSensorPosition(0);
		endPosL = startPosL + changePosTicks * config.LEFT_ENC_SIGN;
		endPosR = startPosR + changePosTicks * config.RIGHT_ENC_SIGN;
		configPID(p, i, d);
		leftFrontTalon.configPeakOutputForward(maxV, 10);
		configPeakOutputVoltage(maxV, -maxV);
		leftFrontTalon.set(endPosL);
		rightFrontTalon.set(endPosR);
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
		leftFrontTalon.set(ControlMode.PercentOutput, s);
		rightFrontTalon.set(ControlMode.PercentOutput, s);
	}
	
	/**
	 * <pre>
	 * public void stop()
	 * </pre>
	 *
	 * Sets both {@code CANTalon} speeds to 0.
	 */
	public void stop() {
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
		while (checkDegreeRotateVoltage(config.COARSE_TURN_ANGLE_MIN_DEGREES, config.COARSE_TURN_ANGLE_MAX_DEGREES)) {
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

	private void configPID(double p, double i, double d) {
		leftFrontTalon.config_kP(0, p, 10);
		leftFrontTalon.config_kI(0, i, 10);
		leftFrontTalon.config_kD(0, d, 10);

		rightFrontTalon.config_kP(0, p, 10);
		rightFrontTalon.config_kI(0, i, 10);
		rightFrontTalon.config_kD(0, d, 10);
	}

	public void configPeakOutputVoltage(double maxForward, double maxReverse) {
		leftFrontTalon.configPeakOutputForward(maxForward, 10);
		leftFrontTalon.configPeakOutputReverse(maxReverse, 10);
		rightFrontTalon.configPeakOutputForward(maxForward, 10);
		rightFrontTalon.configPeakOutputReverse(maxReverse, 10);
	}
}
