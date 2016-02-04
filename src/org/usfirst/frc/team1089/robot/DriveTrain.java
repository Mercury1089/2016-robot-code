package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.Joystick;

public class DriveTrain {

	public CANTalon lft, rft, lbt, rbt;
	private AnalogGyro gyro;
	private static final double TIER_1_DEGREES_FROM_TARGET = 20;
	private static final double TIER_2_DEGREES_FROM_TARGET = 5;
	private static final double TIER_3_DEGREES_FROM_TARGET = 1;
	private static final double TURN_TIMEOUT_MILLIS = 10000;
	private static final double DEADZONE_LIMIT = 0.2;

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
	}

	public void moveDistance(double ticks, double startPosition) {
		if (Robot.isMoving) {
			if ((lft.getEncPosition() > (startPosition + ticks - 5))
					&& (lft.getEncPosition() < (startPosition + ticks + 5))) {
				Robot.isMoving = false;

			}
		} else {
			lft.changeControlMode(CANTalon.TalonControlMode.Position);
			rft.changeControlMode(CANTalon.TalonControlMode.Position);
			// lft.setPID(0.5, 0.001, 0.0);
			// rft.setPID(0.5, 0.001, 0.0);
			// SmartDashboard.putNumber("P", lft.getP());
			lft.enableControl();
			rft.enableControl();
			lft.set(startPosition + ticks);
			rft.set(-startPosition - ticks);
			Robot.isMoving = true;
		}

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
