// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import com.sun.org.apache.regexp.internal.RE;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private boolean[] btn, btnPrev;
	private Camera camera;
	private RobotDrive drive;
	private Joystick gamepad, leftStick, rightStick;
	private AnalogGyro gyro;

	private double resetGyro;

	public void robotInit() {

		camera = new Camera("GRIP/myContoursReport");

		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		gamepad = new Joystick(Ports.USB.GAMEPAD);
		drive = new RobotDrive(Ports.PWM.LEFT_TALON, Ports.PWM.RIGHT_TALON);

		// Set up gyro
		gyro = new AnalogGyro(Ports.Analog.GYRO);
		gyro.reset();
		gyro.setSensitivity((1.1 * 5 / 3.38) / 1000);
		// gyro.initGyro();
		// gyro.calibrate();

		// Motors are inverted; this hasn't solved that problem.
		//
		// drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		// drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
		// drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		// drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);

		btn = new boolean[11];
	}

	public void autonomousPeriodic() {

	}

	public void disabledPeriodic() {
		camera.getNTInfo();
		debug();
	}

	public void teleopPeriodic() {
		drive.tankDrive(leftStick, rightStick);
		resetGyro = 0;

		btnPrev = Arrays.copyOf(btn, 11);
		for (int i = 1; i <= 10; i++) {
			btn[i] = gamepad.getRawButton(i);
		}
		
		// Reset gyro with the A button on the gamepad
		if (button(1))
			gyro.reset();
		
		// Rotate 90 deg. with the B button on the gamepad
		if (button(2)) {
			if (resetGyro != 1) {
				gyro.reset();
				resetGyro = 1;
			}
			while (Math.abs(gyro.getAngle()) <= 60) {
				drive.tankDrive(0.7, -0.7);
			}
			while (Math.abs(gyro.getAngle()) <= 90) {
				drive.tankDrive(0.35, -0.35);
			}
		}
		camera.getNTInfo();
		debug();
	}

	public boolean button(int i) {
		return btn[i] && !btnPrev[i];
	}

	public void testPeriodic() {

	}

	/**
	 * <pre>
	 * public void debug()
	 * </pre>
	 * 
	 * Puts info onto the SmartDashboard.
	 */
	public void debug() {
		camera.debug();
		SmartDashboard.putString("Gyro", "" + Camera.round(gyro.getAngle(), 2));
	}

}
