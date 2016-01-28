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
	private Move moveable;

	private double resetGyro;
	private double diff;
	private double turnAngle = 0;

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

		// Invert only two motors. Depends on which side is faulty.
		drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);

		btn = new boolean[11];
		
		moveable = new Move(drive, gyro);
	}

	public void autonomousPeriodic() {

	}

	public void disabledPeriodic() {
		camera.getNTInfo();
		debug();
	}

	public void teleopPeriodic() {
		resetGyro = 0;

		btnPrev = Arrays.copyOf(btn, 11);
		for (int i = 1; i <= 10; i++) {
			btn[i] = gamepad.getRawButton(i);
		}

		// Teleop Tank
		drive.tankDrive(leftStick, rightStick);

		// Reset gyro with the A button on the gamepad
		if (button(1))
			gyro.reset();

		// Gets turnAngle if there is one target
		if (camera.getCenterX().length >= 1) {
			diff = (160.0 - camera.getCenterX()[camera.getLargestRectNum()]) / 320;
			turnAngle = diff * Camera.HFOV;
		}
		// Turn yourself towards the target
		if (button(2)){
			moveable.degreeRotate(turnAngle, 0.6);
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
		SmartDashboard.putString("Gyro", "" + Utilities.round(gyro.getAngle(), 2));
		SmartDashboard.putNumber("Angle", turnAngle);
		SmartDashboard.putNumber("Diagonal Distance", camera.getDiagonalDist());
		SmartDashboard.putString("Area:", Arrays.toString(camera.getRectArea()));
		SmartDashboard.putString("Width:", Arrays.toString(camera.getRectWidth()));
		SmartDashboard.putString("Height:", Arrays.toString(camera.getRectHeight()));
		SmartDashboard.putString("Center X:", Arrays.toString(camera.getCenterX()));
		SmartDashboard.putString("Center Y:", Arrays.toString(camera.getCenterY()));
		SmartDashboard.putString("Horizontal Distance: ", "" + Utilities.round(camera.getHorizontalDist(), 2) + " ft.");
	}
}
