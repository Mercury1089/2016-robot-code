// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import org.usfirst.frc.team1089.auton.AimEnum;
import org.usfirst.frc.team1089.auton.DefenseEnum;
import org.usfirst.frc.team1089.auton.PosEnum;
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
	private Scaler scaler;
	private Compressor compressor;
	private PortcullisLifter portLifter;

	private MercEncoder mercEncoder; // only used for debugging purpose
	private AnalogGyro gyro;
	private CANTalon leftFront, rightFront, leftBack, rightBack, intakeMotor, lifterMotor, deployerMotor, pLifter;
	private DriveTrain drive;
	private MercAccelerometer accel;
	private ControllerBase cBase;
	private Joystick gamepad, leftStick, rightStick;

	private SendableChooser defenseChooser, shootChooser, posChooser;
	private StrongholdAuton auton;
	private AimEnum aim;
	private DriverStation driverStation;
	private Config config;
	private NTListener ntListener;

	private int shootingAttemptCounter = 0;
	private boolean isShooting = false, isInAuton = false, closeStream = false; 
	private static final int MAX_SHOOTING_ATTEMPT = 5;
	
	@Override
	public void robotInit() {
		config = Config.getInstance();
		camera = new Camera("GRIP/myContoursReport");
		Logger.init();
		ntListener = new NTListener();
		
		driverStation = DriverStation.getInstance();
		accel = new MercAccelerometer();
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

		intakeMotor = new CANTalon(Ports.CAN.INTAKE_TALON_ID);
		lifterMotor = new CANTalon(Ports.CAN.LIFTER_TALON_ID);
		deployerMotor = new CANTalon(Ports.CAN.ENGAGER_TALON_ID);
		pLifter = new CANTalon(Ports.CAN.PORTCULLIS_LIFT_TALON_ID);

		drive = new DriveTrain(leftFront, rightFront, leftBack, rightBack, gyro);
		intake = new Intake(intakeMotor);
		portLifter = new PortcullisLifter(pLifter);
		scaler = new Scaler(lifterMotor, deployerMotor);
		
		gamepad = new Joystick(Ports.USB.GAMEPAD);
		leftStick = new Joystick(Ports.USB.LEFT_STICK);
		rightStick = new Joystick(Ports.USB.RIGHT_STICK);
		cBase = new ControllerBase(gamepad, leftStick, rightStick);

		// Set up our 3 Sendable Choosers for the SmartDashboard

		defenseChooser = new SendableChooser();
		defenseChooser.addDefault("Default", DefenseEnum.DO_NOTHING);
		defenseChooser.addObject("Low Bar", DefenseEnum.LOW_BAR);
		defenseChooser.addObject("Moat", DefenseEnum.MOAT);
		defenseChooser.addObject("Ramparts", DefenseEnum.RAMPARTS);
		defenseChooser.addObject("Rock Wall", DefenseEnum.ROCK_WALL);
		defenseChooser.addObject("Rough Terrain", DefenseEnum.ROUGH_TERRAIN);
		SmartDashboard.putData("Defense: ", defenseChooser);

		posChooser = new SendableChooser();
		posChooser.addDefault("1", PosEnum.POS1);
		posChooser.addObject("2", PosEnum.POS2);
		posChooser.addObject("3", PosEnum.POS3);
		posChooser.addObject("4", PosEnum.POS4);
		posChooser.addObject("5", PosEnum.POS5);
		SmartDashboard.putData("Position: ", posChooser);

		shootChooser = new SendableChooser();
		shootChooser.addDefault("Don't Shoot", AimEnum.NONE);
		shootChooser.addObject("High Goal", AimEnum.HIGH);
		shootChooser.addObject("Low Goal", AimEnum.LOW);
		SmartDashboard.putData("Aim:", shootChooser);

		aim = (AimEnum) shootChooser.getSelected();
		auton = new StrongholdAuton(drive, camera, shooter, intake, gyro, (PosEnum) posChooser.getSelected(), aim,
				(DefenseEnum) defenseChooser.getSelected(), accel, this);
	}
		

	@Override
	public void autonomousInit() {
		gyro.reset();
		auton.resetState();
		
		closeStream = true;
	}

	@Override
	public void autonomousPeriodic() {
		isInAuton = true;
		auton.move();
		
		camera.getNTInfo(); // in case not already called in move()
		debug();
		
		
	}

	@Override
	public void disabledInit() {
		if (closeStream) {
			// When the game ends, close the Logger stream
			// This ends the stream, writes the data to the file,
			// and that's that.
			Logger.close();
			System.out.println("Closed stream!");
		}
	}
	
	@Override
	public void disabledPeriodic() {
		camera.getNTInfo();
		debug();
		
		cBase.rumble(false);
	}
	
	@Override
	public void teleopInit() {
		closeStream = true;
	}
	

	@Override
	// Handle global manipulation of robot here
	public void teleopPeriodic() {
		isInAuton = false;
		// Get initial info
		camera.getNTInfo();

		// Dealing with buttons on the different joysticks
		cBase.update();

		// Teleop Tank with DriveTrain
		drive.tankDrive(leftStick, rightStick);
		
		// Aborts shooting sequence
		if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN7)){
			isShooting = false;
			drive.stop();
		}

		// Aims at target / initiates asynchronous shooting sequence
		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.LB)) {
			aimProc(); // aims at the target / initiates asynchronous shooting sequence
		}

		// TODO maybe here we should always use AimEnum.HIGH because using a Auton setting in teleop is dangerous 
		shootProc(aim); // completes shooting sequence once aiming is successful (if initiated) 

		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.RB)) {
			shooter.shoot(); // shoot ball
		}
		
		if(getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.Y)){
			portLifter.raise();
		}
		
		if(getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.A)){
			portLifter.lower();
		}

		// raising and lowering shooter elevator
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			shooter.raise(Shooter.DOWN);
			intake.lower(true);
			// intake.moveBall(0.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN2)) {
			shooter.raise(Shooter.LOW); // pancake
			// intake.moveBall(0.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			shooter.raise(Shooter.MEDIUM); // shooting height
			intake.lower(true);
			// intake.moveBall(1.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN3)) {
			shooter.raise(Shooter.HIGH); // close shooting height
			// intake.moveBall(0.0);
		}

		//raising, lowering, and powering intake
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN3)) {
			intake.lower(false); // up
		}

		if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN5)) {
			intake.moveBall(-1.0); // pull ball in
			intake.lower(true); // down
		}
		
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN4)) {
			intake.moveBall(0); // stop intake
		}
		
		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			intake.moveBall(1.0); // push ball out
		}

		//makes controller rumble when the robot is able to take a shot
		if (camera.isInDistance() && camera.isInLineWithGoal()) {
			cBase.rumble(true);
		} else {
			cBase.rumble(false);
		}
		Logger.log("WOW DOES THIS WORK????", "IF THIS IS BEING READ, YEET");
		debug();
	}

	@Override
	public void testPeriodic() {
	}
	
	/**
	 * <pre>
	 * public void aimProc()
	 * </pre>
	 * Goes through the aiming procedure to get ready to shoot.
	 */
	public void aimProc() {
		intake.lower(false);

		if (camera.isInDistance() && camera.isInLineWithGoal()) {
			boolean initialPancake = shooter.isElevatorUp();
			shooter.raiseShootingHeight(camera);
			if (shooter.isElevatorUp() != initialPancake){
				Timer.delay(Shooter.RAISE_SHOOTER_CATCHUP_DELAY_SECS); 
			}				// waits for shooter to get in position
			isShooting = true;
			drive.degreeRotateVoltage(camera.getTurnAngle());
		}
	}
	
	/**
	 * <pre>
	 * public void shootProc()
	 * </pre>
	 * Goes through the shooting procedure (requires prior call to aimProc()).
	 */
	public void shootProc(AimEnum aim) {
		double recenteredMoveDistance;
		if (!drive.checkDegreeRotateVoltage() && isShooting) { 
			Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
			camera.getNTInfo();

			if (camera.isInTurnAngle()) {
				isShooting = false;
				if (aim == AimEnum.LOW && isInAuton) {
					// calculates how far the batter is from where we are now
					recenteredMoveDistance = Math.max(0.0, camera.getHorizontalDist() - StrongholdAuton.LENGTH_OF_BATTER_FEET); 
					
					// If distance to center is not unrealistic, continue
					if (recenteredMoveDistance < StrongholdAuton.MAX_RECENTER_DISTANCE_FEET) {					
						drive.moveDistance(recenteredMoveDistance, 0.4, 0, 0, 4.5);
						drive.waitMove();
						shooter.raise(Shooter.DOWN);
					}
				}
				shooter.shoot();
			} else if (shootingAttemptCounter < MAX_SHOOTING_ATTEMPT) {
				drive.degreeRotateVoltage(camera.getTurnAngle());
				shootingAttemptCounter++;
			} else {
				isShooting = false;
			}
		}
	}
	
	/**
	 * <pre>
	 * public boolean isShooting()
	 * </pre>
	 * Gets whether or not the robot is shooting.
	 * @return isShooting
	 */
	public boolean isShooting() {
		return isShooting;
	}
	
	/**
	 * <pre>
	 * public boolean getPressedDown(Joysticks contNum, 
	 *                               int buttonNum)
	 * </pre>
	 * Gets whether or not a button from the specified {@code Joystick} is pressed.
	 * @param contNum the {@code Joystick} to check the button from
	 * @param buttonNum the index of the button to test
	 * @return true if the button on the specified {@code Joystick} is pressed,
	 *         false otherwise
	 */
	public boolean getPressedDown(Joysticks contNum, int buttonNum) {
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
				"" + Utilities.round(mercEncoder.distanceTravelled(leftFront.getEncPosition(), +1.0), 3) + " ft.");
		SmartDashboard.putString("Distance Travelled Right",
				"" + Utilities.round(mercEncoder.distanceTravelled(rightFront.getEncPosition(), +1.0), 3) + " ft.");
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
		SmartDashboard.putBoolean("FMS", driverStation.isFMSAttached());
		SmartDashboard.putBoolean("Is in range", camera.isInDistance());
		SmartDashboard.putBoolean("Is in turn angle", camera.isInTurnAngle());
		SmartDashboard.putBoolean("Is in line with goal", camera.isInLineWithGoal());
		SmartDashboard.putBoolean("Is flat", accel.isFlat());
		
		// Auton
		SmartDashboard.putString("Auton State", auton.getState());
	}
}
