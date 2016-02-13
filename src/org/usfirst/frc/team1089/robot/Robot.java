// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;
 
import org.usfirst.frc.team1089.auton.AimEnum;
import org.usfirst.frc.team1089.auton.DefenseEnum;
import org.usfirst.frc.team1089.auton.StrongholdAuton;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private static boolean[] btn;
	private static boolean[] btnPrev;
	
	private Camera camera;
	
	private Shooter shooter;
	//private Intake intake;
	private Compressor compressor;

	private MercEncoder mercEncoder; // only used for debugging purpose
	private AnalogGyro gyro;
	private CANTalon leftFront, rightFront, leftBack, rightBack;
	private DriveTrain drive;
	
	// private ControllerBase cBase;
	private Joystick gamepad, leftStick, rightStick;

	private SendableChooser defenseChooser, shootChooser, posChooser;
	private StrongholdAuton auton;

	
	@Override
	public void robotInit() {
		camera = new Camera("GRIP/myContoursReport");
		
		shooter = new Shooter();
		compressor = new Compressor();
		compressor.checkCompressor();
		
		mercEncoder = new MercEncoder();

		// Set up gyro
		gyro = new AnalogGyro(Ports.Analog.GYRO);
		gyro.reset();
		gyro.setSensitivity((1.1 * 5 / 3.38) / 1000); // TODO Add Constants

		leftFront = new CANTalon(Ports.CAN.LEFT_FRONT_TALON_ID);
		leftBack = new CANTalon(Ports.CAN.LEFT_BACK_TALON_ID);
		rightFront = new CANTalon(Ports.CAN.RIGHT_FRONT_TALON_ID);
		rightBack = new CANTalon(Ports.CAN.RIGHT_BACK_TALON_ID);

		drive = new DriveTrain(leftFront, rightFront, leftBack, rightBack, gyro);

		// cBase = new ControllerBase(Ports.USB.GAMEPAD, Ports.USB.LEFT_STICK,
		// Ports.USB.RIGHT_STICK);

		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		gamepad = new Joystick(Ports.USB.GAMEPAD);

		btn = new boolean[ControllerBase.MAX_NUMBER_BUTTONS];

		defenseChooser = new SendableChooser();
		defenseChooser.addDefault("Default", DefenseEnum.DO_NOTHING);
		defenseChooser.addObject("Low Bar", DefenseEnum.LOW_BAR);
		defenseChooser.addObject("Moat", DefenseEnum.MOAT);
		defenseChooser.addObject("Ramparts", DefenseEnum.RAMPARTS);
		defenseChooser.addObject("Rock Wall", DefenseEnum.ROCK_WALL);
		defenseChooser.addObject("Rough Terrain", DefenseEnum.ROUGH_TERRAIN);
		SmartDashboard.putData("Defense: ", defenseChooser);

		posChooser = new SendableChooser();
		posChooser.addDefault("1", 1);
		posChooser.addObject("2", 2);
		posChooser.addObject("3", 3);
		posChooser.addObject("4", 4);
		posChooser.addObject("5", 5);
		SmartDashboard.putData("Position: ", posChooser);

		shootChooser = new SendableChooser();
		shootChooser.addDefault("Don't Shoot", AimEnum.NONE);
		shootChooser.addObject("High Goal", AimEnum.HIGH);
		shootChooser.addObject("Low Goal", AimEnum.LOW);
		SmartDashboard.putData("Aim:", shootChooser);

		auton = new StrongholdAuton(drive, camera, shooter, (int) posChooser.getSelected(), (AimEnum) shootChooser.getSelected(),
				(DefenseEnum) defenseChooser.getSelected());
	}

	@Override
	public void autonomousInit() {
		auton.move(); // if we don't use a state machine then we only want to call auton.move() once
	}

	@Override
	public void autonomousPeriodic() {
		//auton.move();
	}

	@Override
	public void disabledPeriodic() {
		camera.getNTInfo();
		debug();
	}

	@Override
	// Handle global manipulation of robot here
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
		if (button(ControllerBase.GamepadButtons.A)) {
			gyro.reset();
		}

		// Gets turnAngle if there is one target
		// Turn yourself towards the target
		if (button(ControllerBase.GamepadButtons.B)) {
			drive.degreeRotate(camera.getTurnAngle(), 1.0);
		}

		if (button(ControllerBase.GamepadButtons.Y)) {
			leftFront.setEncPosition(0);
			rightFront.setEncPosition(0);
		}

		if (button(ControllerBase.GamepadButtons.X)) {
			drive.turnDistance(drive.arcLength(camera.getTurnAngle()));
		}

		drive.checkMove();

		
		if (button(ControllerBase.GamepadButtons.BACK)) {
			drive.degreeRotate(10, 0.4); 
		}
		 
		if (button(ControllerBase.GamepadButtons.LB)) {
			shooter.shoot();
		}

		if (gamepad.getRawButton(ControllerBase.GamepadButtons.RB)) {
			shooter.raise(false);
			//intake.moveBall(1);
		} else{
			shooter.raise(true);
			//intake.moveBall(0);
		}
		
		/*if (gamepad.getRawButton(ControllerBase.GamepadButtons.RB)) {
			shooter.raise(false);
			//intake.moveBall(-1);
		} else {
			shooter.raise(true);
			//intake.moveBall(0);
		}*/

		camera.getNTInfo(); 
		debug();
	}

	@Override
	public void testPeriodic() {

	}

	public static boolean button(int i) {
		return btn[i] && !btnPrev[i]; 
	}

	/**
	 * <pre>
	 * public void debug()
	 * </pre>
	 * 
	 * Puts info onto the SmartDashboard.
	 */
	public void debug() {
		// DriveTrain
		SmartDashboard.putString("Gyro", "" + Utilities.round(gyro.getAngle(), 2) + " deg.");
		
		SmartDashboard.putNumber("Left Encoder", leftFront.getEncPosition()); 
		SmartDashboard.putNumber("Right Encoder", rightFront.getEncPosition());
		SmartDashboard.putString("Distance Travelled Left",
				"" + Utilities.round(mercEncoder.distanceTravelled(leftFront.getEncPosition() , 1.0), 4) + " ft.");
		SmartDashboard.putString("Distance Travelled Right",
				"" + Utilities.round(mercEncoder.distanceTravelled(rightFront.getEncPosition() , 1.0), 4) + " ft.");
		SmartDashboard.putNumber("leftFront error", leftFront.getClosedLoopError());
		SmartDashboard.putNumber("rightFront error", rightFront.getClosedLoopError());	
		
		// Camera
		SmartDashboard.putString("Area:", Arrays.toString(camera.getRectArea()) + " px.");
		SmartDashboard.putString("Width:", Arrays.toString(camera.getRectWidth()) + " px.");
		SmartDashboard.putString("Height:", Arrays.toString(camera.getRectHeight()) + " px.");
		SmartDashboard.putString("Center X:", Arrays.toString(camera.getCenterX()) + " px.");
		SmartDashboard.putString("Center Y:", Arrays.toString(camera.getCenterY()) + " px.");

		SmartDashboard.putString("Perceived Opening Width", camera.getOpeningWidth() + " in.");
		SmartDashboard.putString("Diagonal Distance", "" + Utilities.round(camera.getDiagonalDist(), 2) + " ft.");
		SmartDashboard.putString("Horizontal Distance: ", "" + Utilities.round(camera.getHorizontalDist(), 2) + " ft.");
		SmartDashboard.putString("Angle to turn", "" + Utilities.round(camera.getTurnAngle(), 2) + " deg.");
		
		SmartDashboard.putBoolean("Is in range", camera.isInDistance());
		SmartDashboard.putBoolean("Is in turn angle", camera.isInTurnAngle());
		SmartDashboard.putBoolean("Is in line with goal", camera.isInLineWithGoal());
	}
}
