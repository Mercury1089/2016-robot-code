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
	private double diff;

	private boolean targetIsLeft = false, targetIsRight = false;
	private double turningTime = -1.0;
	private double turnAngle;

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
		// Invert only two motors. Depends on which side is faulty.
		drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);

		btn = new boolean[11];
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
		if (camera.getCenterX().length == 1){
		diff = (160.0 - camera.getCenterX()[0]) / 320;
		}
		// Turn yourself towards the target if there is one target.
		if (button(3) && camera.getCenterX().length == 1) {
			boolean inRange = diff * Ports.HFOV <= 1.0;
			diff = (160.0 - camera.getCenterX()[0]) / 320;
			//drive.tankDrive(-diff * Ports.HFOV, diff * Ports.HFOV);
			SmartDashboard.putNumber("Angle", diff * Ports.HFOV);
			inRange = Math.abs(diff) <= 3;	
			degreeRotate(diff * Ports.HFOV, 0.4);
			}
		if (button(4)){
			degreeRotate(-90, 0.5);
		}
/*			if (camera.getCenterX()[0] > 155) {
				targetIsRight = true;
				targetIsLeft = false;
			} else if (camera.getCenterX()[0] < 145) {
				targetIsRight = false;
				targetIsLeft = true;
			} else {
				targetIsRight = false;
				targetIsLeft = false;
			}
		}
		if(!targetIsRight && !targetIsLeft) {
			drive.tankDrive(leftStick, rightStick);
		}
		if (targetIsRight) {
			if (turningTime == -1.0){ 
				turningTime = System.currentTimeMillis();
			}
			drive.tankDrive(-.3, .3); 
			if (camera.getCenterX()[0] < 155 && camera.getCenterX()[0] > 145 || (System.currentTimeMillis() - turningTime) >= 8000) {
				targetIsRight = false;
				targetIsLeft = false;
				turningTime = -1.0;
			}
		} else if (targetIsLeft) {
			if (turningTime == -1.0){
				turningTime = System.currentTimeMillis();
			}
			drive.tankDrive(.3, -.3);
			if (camera.getCenterX()[0] < 155 && camera.getCenterX()[0] > 145 || (System.currentTimeMillis() - turningTime) >= 8000) {
				targetIsRight = false;
				targetIsLeft = false;
				turningTime = -1.0;
			}
		} else {
			drive.tankDrive(leftStick, rightStick);
			targetIsRight = false;
			targetIsLeft = false;
			turningTime = -1.0;
			*/
		

		camera.getNTInfo();
		debug();
	}
	
	public void degreeRotate(double deg, double s){
		double startAngle = gyro.getAngle();
		while(Math.abs(gyro.getAngle() - startAngle) <= Math.abs(deg) - 15){
			if (deg < 0){
				s *= -1;
			}
			drive.tankDrive(s, -s);
		}
		while (Math.abs(gyro.getAngle() - startAngle) <= Math.abs(deg) - 5){
			drive.tankDrive(s/2, -s/2);
		}
		drive.tankDrive(leftStick, rightStick);
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
		SmartDashboard.putBoolean("TIR", targetIsRight);
		SmartDashboard.putBoolean("TIL", targetIsLeft);
		SmartDashboard.putNumber("Difference", diff);
		// SmartDashboard.putNumber("Debug", debug);
	}

}
