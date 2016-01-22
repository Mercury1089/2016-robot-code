// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private boolean[] btn, btnPrev;

	private RobotDrive drive;
	private Joystick gamepad, leftStick, rightStick;

	int count = 0;

	public void robotInit() {
		
		// First off you can't do this with the robot drive, you will get an
		// error and it will cause problems
		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		gamepad = new Joystick(Ports.USB.GAMEPAD);
		drive = new RobotDrive(Ports.PWM.LEFT_TALON, Ports.PWM.RIGHT_TALON);
		//drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
		//drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
		//drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		//drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		btn = new boolean[11];
	}

	public void autonomousPeriodic() {

	}

	public void disabledPeriodic() {
	}

	public void teleopPeriodic() {

		drive.tankDrive(leftStick, rightStick);

		// Get values from NetworkTable and put into SmartDash
		
		btnPrev = Arrays.copyOf(btn, 11);
		for (int i = 1; i <= 10; i++) {
			btn[i] = gamepad.getRawButton(i);
		}

	}

	public boolean button(int i) {
		return btn[i] && !btnPrev[i];
	}

	public void testPeriodic() {

	}

}
