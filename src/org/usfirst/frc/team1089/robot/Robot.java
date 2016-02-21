// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;
 
import org.usfirst.frc.team1089.auton.AimEnum;
import org.usfirst.frc.team1089.auton.DefenseEnum;
import org.usfirst.frc.team1089.auton.StrongholdAuton;
import org.usfirst.frc.team1089.robot.ControllerBase.Joysticks;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {	
	private Camera camera;
	
	private Shooter shooter;
	private Intake intake;
	private Compressor compressor;

	private MercEncoder mercEncoder; // only used for debugging purpose
	private AnalogGyro gyro;
	private CANTalon leftFront, rightFront, leftBack, rightBack;
	private DriveTrain drive;
	private MercAccelerometer accel;
	private ControllerBase cBase;
	private Joystick gamepad, leftStick, rightStick;

	private SendableChooser defenseChooser, shootChooser, posChooser;
	private StrongholdAuton auton;
	private DriverStation driverStation;
	private Config config;
	
	@Override
	public void robotInit() { 
		config = Config.getCurrent();
		camera = new Camera("GRIP/myContoursReport");
		
		driverStation = DriverStation.getInstance();
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

		gamepad = new Joystick(Ports.USB.GAMEPAD);
		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		cBase = new ControllerBase(gamepad, leftStick, rightStick);

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
				(DefenseEnum) defenseChooser.getSelected(), accel);
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
		cBase.update();

		// Teleop Tank with DriveTrain
		drive.tankDrive(leftStick, rightStick);

		// Reset gyro with the A button on the gamepad
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.A)) {
			gyro.reset();
		}	

		// Gets turnAngle if there is one target
		// Turn yourself towards the target
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.B)) {
			drive.autoRotate(camera);
			//drive.turnDistance(1);
		}

		//reset encoders
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.Y)) {
			leftFront.setEncPosition(0);
			rightFront.setEncPosition(0);
		}
		
		// begin asynchronous moves
		
		//Camera Turn
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.X)) {
			drive.encoderAngleRotate(360);
		}
	
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.START)) {
			//drive.encoderAngleRotate(360); // this is an asynchronous move
			drive.encoderAngleRotate(camera.getTurnAngle());
		}

		drive.checkMove();
		
		// end asynchronous moves
		
		/*if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			drive.degreeRotate(10, 0.4); 
		}*/
		 
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.RB)) {
			shooter.shoot();			//shoot ball
		}

		//raising and lowering shooter elevator
		if (button(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN2)) {
			shooter.raise(shooter.LOW);				//pancake
			//intake.moveBall(0.0);
		}
		else if (button(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			shooter.raise(shooter.MEDIUM);			//shooting height
			//intake.moveBall(0.0);
		}
		else if (button(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			shooter.raise(shooter.DOWN);
			//intake.moveBall(1.0);
		}
		else if (button(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN3)){
			shooter.raise(shooter.HIGH);			//close shooting height
			//intake.moveBall(0.0);
		}
		
		if (button(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN2)) {
			intake.lower(true);				//down
		}
		
		if (button(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN3)) {
			intake.lower(false); 			//up
		}
		
		if (button(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN5)) {
			intake.moveBall(-1.0);			//pull ball in
		}
		if(button(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN4)) {
			intake.moveBall(0);				//stop intake
		}
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			intake.moveBall(1.0);			//push ball out
		}
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.L3)){
			intake.lower(false);
		}
		
		if (button(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.LB)) {
			shootProcedure();
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
		intake.lower(false);
		
		if (camera.isInDistance() && camera.isInLineWithGoal()) {
			shooter.raiseShootingHeight(camera);
			Timer.delay(0.500); // waits for shooter to get in position
			drive.autoRotate/*New*/(camera);
			if (camera.isInTurnAngle()){ // assumes NT info is up to date coming out of rotation routine
				shooter.shoot();
			}
		}
	}

	public boolean button(Joysticks contNum, int buttonNum) {
		return cBase.getPressedDown(contNum, buttonNum); 
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
		
		// DriveTrain
		SmartDashboard.putString("Gyro", "" + Utilities.round(gyro.getAngle(), 3) + " deg.");
		SmartDashboard.putNumber("Left Encoder", leftFront.getEncPosition()); 
		SmartDashboard.putNumber("Right Encoder", rightFront.getEncPosition());
		SmartDashboard.putString("Distance Travelled Left",
				"" + Utilities.round(mercEncoder.distanceTravelled(leftFront.getEncPosition() , +1.0), 3) + " ft.");
		SmartDashboard.putString("Distance Travelled Right",
				"" + Utilities.round(mercEncoder.distanceTravelled(rightFront.getEncPosition() , +1.0), 3) + " ft.");
		SmartDashboard.putNumber("leftFront error", leftFront.getClosedLoopError());
		SmartDashboard.putNumber("rightFront error", rightFront.getClosedLoopError());	
		
		// Accelerometer
		SmartDashboard.putNumber("Accel Z", Utilities.round(accel.getAccelZ(), 3));
		SmartDashboard.putNumber("Accel Tilt", Utilities.round(accel.getTilt(), 3));
		
		// Camera
		SmartDashboard.putString("Area:", Arrays.toString(camera.getRectArea()) + " px.");
		SmartDashboard.putString("Width:", Arrays.toString(camera.getRectWidth()) + " px.");
		SmartDashboard.putString("Height:", Arrays.toString(camera.getRectHeight()) + " px.");
		SmartDashboard.putString("Center X:", Arrays.toString(camera.getCenterX()) + " px.");
		SmartDashboard.putString("Center Y:", Arrays.toString(camera.getCenterY()) + " px.");

		SmartDashboard.putString("Perceived Opening Width", Utilities.round(camera.getOpeningWidth(), 3) + " in.");
		SmartDashboard.putString("Diagonal Distance", "" + Utilities.round(camera.getDiagonalDist(), 3) + " ft.");
		SmartDashboard.putString("Horizontal Distance: ", "" + Utilities.round(camera.getHorizontalDist(), 3) + " ft.");
		SmartDashboard.putString("Angle to turn", "" + Utilities.round(camera.getTurnAngle(), 3) + " deg.");
		SmartDashboard.putString("Perceived Opening Width", Utilities.round(camera.getOpeningWidth(), 3) + " in.");
		
		// Compound and miscellaneous indicators
		SmartDashboard.putString("Config Type", config.toString());
		SmartDashboard.putBoolean("FMS" , driverStation.isFMSAttached());
		SmartDashboard.putBoolean("Is in range", camera.isInDistance());
		SmartDashboard.putBoolean("Is in turn angle", camera.isInTurnAngle());
		SmartDashboard.putBoolean("Is in line with goal", camera.isInLineWithGoal());
		SmartDashboard.putBoolean("Is flat", accel.isFlat());
	}
}
