// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import org.usfirst.frc.team1089.auton.*;
import java.util.Arrays;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private boolean[] btn, btnPrev;
	private Camera camera;
	private MercEncoder leftEncoder, rightEncoder;
	// private RobotDrive drive;
	private CANTalon leftFront, rightFront, leftBack, rightBack;
	private Joystick gamepad, leftStick, rightStick;
	private AnalogGyro gyro;
	private ControllerBase cBase;
	private DriveTrain drive;
	private double endPosL, endPosR;

	private DefenseEnum defenseEnum; 
	private SendableChooser autonChooser, autonShootChooser;
	private String autonAim;
	private double TURN_RADIUS_INCHES = 13.5; // TODO FIX THIS
	int counter = 0;

	public void robotInit() {

		camera = new Camera("GRIP/myContoursReport");
		leftEncoder = new MercEncoder();
		rightEncoder = new MercEncoder();

		// Set up gyro
		gyro = new AnalogGyro(Ports.Analog.GYRO);
		gyro.reset();
		gyro.setSensitivity((1.1 * 5 / 3.38) / 1000); // TODO Add Constants

		leftFront = new CANTalon(Ports.CAN.LEFT_FRONT_TALON_ID);
		leftBack = new CANTalon(Ports.CAN.LEFT_BACK_TALON_ID);
		rightFront = new CANTalon(Ports.CAN.RIGHT_FRONT_TALON_ID);
		rightBack = new CANTalon(Ports.CAN.RIGHT_BACK_TALON_ID);
		leftFront.enableBrakeMode(true);
		rightFront.enableBrakeMode(true);
		leftBack.enableBrakeMode(true);
		rightBack.enableBrakeMode(true);

		drive = new DriveTrain(leftFront, rightFront, leftBack, rightBack, gyro);

		cBase = new ControllerBase(Ports.USB.GAMEPAD, Ports.USB.LEFT_STICK, Ports.USB.RIGHT_STICK);

		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		gamepad = new Joystick(Ports.USB.GAMEPAD);

		btn = new boolean[ControllerBase.MAX_NUMBER_BUTTONS];

	}

	public void autonomousInit() {
		int position = 1;
	}
	public void autonomousPeriodic() {
		
	}

	public void disabledPeriodic() {
		camera.getNTInfo();
		debug();
		autonChooser = new SendableChooser();
		autonChooser.addDefault("Default", defenseEnum.DO_NOTHING);
		autonChooser.addObject("Low Bar", defenseEnum.LOW_BAR);
		autonChooser.addObject("Moat", defenseEnum.MOAT);
		autonChooser.addObject("Ramparts", defenseEnum.RAMPARTS);
		autonChooser.addObject("RockWall", defenseEnum.ROCK_WALL);
		autonChooser.addObject("RoughTerrain", defenseEnum.ROUGH_TERRAIN);
		SmartDashboard.putData("Defense:", autonChooser);

		autonShootChooser = new SendableChooser();
		autonShootChooser.addDefault("Don't Shoot", "Don't Shoot");
		autonShootChooser.addObject("High Goal", "High Goal");
		autonShootChooser.addObject("Low Goal", "Low Goal");
		autonAim = (String)autonShootChooser.getSelected();
		SmartDashboard.putString("Aim:", autonAim);
	}

	public void teleopPeriodic() {
		// Get initial info
		camera.getNTInfo();

		btnPrev = Arrays.copyOf(btn, ControllerBase.MAX_NUMBER_BUTTONS);

		for (int i = 1; i < ControllerBase.MAX_NUMBER_BUTTONS; i++) {
			btn[i] = gamepad.getRawButton(i);
		}

		// Teleop Tank with DriveTrain
		drive.tankDrive(leftStick, rightStick);

		// Reset gyro with the A button on the gamepad
		if (button(ControllerBase.GamepadButtons.A))
			gyro.reset();

		// Gets turnAngle if there is one target
		// Turn yourself towards the target
		if (button(ControllerBase.GamepadButtons.B)) {
			drive.degreeRotate(camera.getTurnAngle(), 0.5);
		}
		if (button(ControllerBase.GamepadButtons.Y)) {
			leftFront.setEncPosition(0);
			rightFront.setEncPosition(0);
		}

		if (button(ControllerBase.GamepadButtons.X)) {
			drive.moveDistance(encoderDistToGoalFeet());
		}

		drive.checkMove();

		camera.getNTInfo();
		debug();
	}

	public boolean button(int i) {
		return btn[i] && !btnPrev[i];
	}

	public void testPeriodic() {

	}

	public double encoderDistToGoalFeet() {
		// return Math.toRadians(camera.getTurnAngle()) * TURN_RADIUS_INCHES;
		return Math.toRadians(90) * TURN_RADIUS_INCHES / 12;
	}

	/**
	 * <pre>
	 * public void debug()
	 * </pre>
	 * 
	 * Puts info onto the SmartDashboard.
	 */
	public void debug() {
		SmartDashboard.putString("Gyro", "" + Utilities.round(gyro.getAngle(), 2) + " deg.");
		SmartDashboard.putString("Angle to turn", "" + camera.getTurnAngle() + " deg.");
		SmartDashboard.putString("Diagonal Distance", "" + camera.getDiagonalDist() + " ft.");
		SmartDashboard.putNumber("Left Encoder", leftFront.getEncPosition());
		SmartDashboard.putNumber("Right Encoder", rightFront.getEncPosition());
		SmartDashboard.putString("Distance Travelled Left",
				"" + Utilities.round(leftEncoder.distanceTravelled(leftFront.getEncPosition(), 1.0), 4) + " ft.");
		SmartDashboard.putString("Distance Travelled Right",
				"" + Utilities.round(rightEncoder.distanceTravelled(rightFront.getEncPosition(), -1.0), 4) + " ft.");
		SmartDashboard.putString("Area:", Arrays.toString(camera.getRectArea()) + " px.");
		SmartDashboard.putString("Width:", Arrays.toString(camera.getRectWidth()) + " px.");
		SmartDashboard.putString("Height:", Arrays.toString(camera.getRectHeight()) + " px.");
		SmartDashboard.putString("Center X:", Arrays.toString(camera.getCenterX()) + " px.");
		SmartDashboard.putString("Center Y:", Arrays.toString(camera.getCenterY()) + " px.");
		SmartDashboard.putString("Horizontal Distance: ", "" + Utilities.round(camera.getHorizontalDist(), 2) + " ft.");
		SmartDashboard.putString("Perceived Opening Width", camera.getOpeningWidth() + " in.");
		SmartDashboard.putNumber("leftFront error", leftFront.getClosedLoopError());
		SmartDashboard.putNumber("rightFront error", rightFront.getClosedLoopError());
		SmartDashboard.putNumber("end pos L", endPosL);
		SmartDashboard.putNumber("end pos R", endPosR);
		SmartDashboard.putBoolean("Is in range", camera.isInDistance());
		SmartDashboard.putBoolean("Is in turn angle", camera.isInTurnAngle());
	}
}
