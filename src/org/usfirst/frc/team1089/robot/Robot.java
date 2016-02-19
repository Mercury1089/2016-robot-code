// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;
 
import org.usfirst.frc.team1089.auton.AimEnum;
import org.usfirst.frc.team1089.auton.DefenseEnum;
import org.usfirst.frc.team1089.auton.StrongholdAuton;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private static boolean[][] btn;
	private static boolean[][] btnPrev;
	private static Joystick[] joysticks;
	
	private Camera camera;
	
	private Shooter shooter;
	private Intake intake;
	private Compressor compressor;

	private MercEncoder mercEncoder; // only used for debugging purpose
	private AnalogGyro gyro;
	private CANTalon leftFront, rightFront, leftBack, rightBack;
	private DriveTrain drive;
	private MercAccelerometer accel;
	// private ControllerBase cBase;
	private Joystick gamepad, leftStick, rightStick;

	private SendableChooser defenseChooser, shootChooser, posChooser;
	private StrongholdAuton auton;
	
	@Override
	public void robotInit() { 
		camera = new Camera("GRIP/myContoursReport");
		accel = new MercAccelerometer();
		shooter = new Shooter();
		compressor = new Compressor();
		compressor.checkCompressor();
		intake = new Intake();
		
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

		btn = new boolean[ControllerBase.MAX_NUMBER_CONTROLLERS][ControllerBase.MAX_NUMBER_BUTTONS];
		btnPrev = new boolean[ControllerBase.MAX_NUMBER_CONTROLLERS][ControllerBase.MAX_NUMBER_BUTTONS];
		joysticks = new Joystick[]{rightStick, leftStick, gamepad};

		//Set up our 3 Sendable Choosers for the SmartDashboard
		
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

		auton = new StrongholdAuton(drive, camera, shooter, gyro, (int) posChooser.getSelected(), (AimEnum) shootChooser.getSelected(),
				(DefenseEnum) defenseChooser.getSelected());
	}

	@Override
	public void autonomousInit() {
		
	}

	@Override
	public void autonomousPeriodic() {
		auton.move();
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
		
		//Dealing with buttons on the different joysticks
		for(int i = 0; i < ControllerBase.MAX_NUMBER_CONTROLLERS; i++) {
			for (int j = 1; j < ControllerBase.MAX_NUMBER_BUTTONS; j++) {
				btnPrev[i][j] = btn[i][j];
			}
		}

		for(int i = 0; i < ControllerBase.MAX_NUMBER_CONTROLLERS; i++) {
			for (int j = 1; j < ControllerBase.MAX_NUMBER_BUTTONS; j++) {
				btn[i][j] = joysticks[i].getRawButton(j);
			}
		}
		

		// Teleop Tank with DriveTrain
		drive.tankDrive(leftStick, rightStick);

		// Reset gyro with the A button on the gamepad
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.A)) {
			gyro.reset();
		}
		
		// begin asynchronous rotations

		// Gets turnAngle if there is one target
		// Turn yourself towards the target
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.B)) {
			drive.autoRotate(camera);
			//drive.turnDistance(1);
		}

		//reset encoders
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.Y)) {
			leftFront.setEncPosition(0);
			rightFront.setEncPosition(0);
		}
		
		// begin asynchronous moves
		
		//Camera Turn
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.X)) {
			drive.turnDistance(drive.arcLength(camera.getTurnAngle()));
		}
	
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.START)) {
			//drive.encoderAngleRotate(360); // this is an asynchronous move
			drive.encoderAngleRotate(/*camera.getTurnAngle()*/45);
		}

		drive.checkMove();
		
		// end asynchronous moves
		
		/*if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			drive.degreeRotate(10, 0.4); 
		}*/
		 
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.LB)) {
			shooter.shoot();			//shoot ball
		}

		//raising and lowering shooter elevator
		if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			shooter.raise(shooter.LOW);				//pancake
			//intake.moveBall(0.0);
		}
		else if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN4)) {
			shooter.raise(shooter.MEDIUM);			//shooting height
			//intake.moveBall(0.0);
		}
		else if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN5)) {
			shooter.raise(shooter.DOWN);
			//intake.moveBall(1.0);
		}
		else if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN6)){
			shooter.raise(shooter.HIGH);			//close shooting height
			//intake.moveBall(0.0);
		}
		
		if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN2)) {
			intake.raise(false);		
		}
		
		if (button(Ports.USB.RIGHT_STICK, ControllerBase.JoystickButtons.BTN3)) {
			intake.raise(true); 
		}
		
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.L3)) {
			intake.moveBall(-1.0);			//pull ball in
		}
		if(button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.R3)) {
			intake.moveBall(0);				//stop intake
		}
		if (button(Ports.USB.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			intake.moveBall(1.0);			//push ball out
		}
		
		if (button(Ports.USB.LEFT_STICK, ControllerBase.JoystickButtons.BTN6)) {
			//drive.turnDistance(1);
			drive.autoRotate(camera);
			if (Math.abs(camera.getTurnAngle()) < 1.5 && !drive.isMoving){
				intake.raise(false);
				shooter.raiseShootingHeight(camera.getHorizontalDist());
				shooter.shoot();
			}
		}
		
		/*if (gamepad.getRawButton(ControllerBase.GamepadButtons.RB)) {
			shooter.raise(false);
			//intake.moveBall(-1.0);
		} else {
			shooter.raise(true);
			//intake.moveBall(0.0);
		}*/

		debug();
	}

	@Override
	public void testPeriodic() {

	}
	
	public void shootProcedure(){
		drive.encoderAngleRotate(camera.getTurnAngle());
		drive.waitMove();
		intake.raise(false);
		shooter.shoot();

	}

	public static boolean button(int contNum, int buttonNum) {
		return btn[contNum][buttonNum] && !btnPrev[contNum][buttonNum]; 
	}

	/**
	 * <pre>
	 * public void debug()
	 * </pre>
	 * 
	 * Puts info onto the SmartDashboard.
	 */
	public void debug() {
		// Display on SmartDash
		SmartDashboard.putString("Gyro", "" + Utilities.round(gyro.getAngle(), 2) + " deg.");
		
		SmartDashboard.putNumber("Left Encoder", leftFront.getEncPosition()); 
		SmartDashboard.putNumber("Right Encoder", rightFront.getEncPosition());
		SmartDashboard.putString("Distance Travelled Left",
				"" + Utilities.round(mercEncoder.distanceTravelled(leftFront.getEncPosition() , 1.0), 4) + " ft.");
		SmartDashboard.putString("Distance Travelled Right",
				"" + Utilities.round(mercEncoder.distanceTravelled(rightFront.getEncPosition() , 1.0), 4) + " ft.");
		SmartDashboard.putNumber("leftFront error", leftFront.getClosedLoopError());
		SmartDashboard.putNumber("rightFront error", rightFront.getClosedLoopError());	
		SmartDashboard.putNumber("Accel Z", accel.getAccelZ());
		
		// Camera
		SmartDashboard.putString("Area:", Arrays.toString(camera.getRectArea()) + " px.");
		SmartDashboard.putString("Width:", Arrays.toString(camera.getRectWidth()) + " px.");
		SmartDashboard.putString("Height:", Arrays.toString(camera.getRectHeight()) + " px.");
		SmartDashboard.putString("Center X:", Arrays.toString(camera.getCenterX()) + " px.");
		SmartDashboard.putString("Center Y:", Arrays.toString(camera.getCenterY()) + " px.");

		SmartDashboard.putString("Perceived Opening Width", Utilities.round(camera.getOpeningWidth(), 2) + " in.");
		SmartDashboard.putString("Diagonal Distance", "" + Utilities.round(camera.getDiagonalDist(), 2) + " ft.");
		SmartDashboard.putString("Horizontal Distance: ", "" + Utilities.round(camera.getHorizontalDist(), 2) + " ft.");
		SmartDashboard.putString("Angle to turn", "" + Utilities.round(camera.getTurnAngle(), 2) + " deg.");		SmartDashboard.putString("Perceived Opening Width", Utilities.round(camera.getOpeningWidth(), 2) + " in.");

		
		SmartDashboard.putBoolean("Is in range", camera.isInDistance());
		SmartDashboard.putBoolean("Is in turn angle", camera.isInTurnAngle());
		SmartDashboard.putBoolean("Is in line with goal", camera.isInLineWithGoal());
	}
}
