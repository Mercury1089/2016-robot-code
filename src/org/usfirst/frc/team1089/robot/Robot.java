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
import edu.wpi.first.wpilibj.DriverStation.Alliance;
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
	private DefenseEnum defense;
	private PosEnum pos;
	private DriverStation driverStation;
	private Config config;

	private int shootingAttemptCounter = 0;
	private boolean isShooting = false, isInAuton = false; 
	private static final int MAX_SHOOTING_ATTEMPT = 5;
	
	public void resetAll(){
		isShooting = false;
		isInAuton = false;
		shootingAttemptCounter = 0;
	}
	
	@Override
	public void robotInit() {
		config = Config.getInstance();
		camera = new Camera("GRIP/myContoursReport");
		camera.runListener();
				
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
		defenseChooser.addDefault("Do Nothing", DefenseEnum.DO_NOTHING);
		defenseChooser.addObject("Low Bar", DefenseEnum.LOW_BAR);
		defenseChooser.addObject("Moat", DefenseEnum.MOAT);
		defenseChooser.addObject("Ramparts", DefenseEnum.RAMPARTS);
		defenseChooser.addObject("Rock Wall", DefenseEnum.ROCK_WALL);
		defenseChooser.addObject("Rough Terrain", DefenseEnum.ROUGH_TERRAIN);
		defenseChooser.addObject("Cheval De Frise", DefenseEnum.CHEVAL_DE_FRISE);
		defenseChooser.addObject("Portcullis", DefenseEnum.PORTCULLIS);
		SmartDashboard.putData("Defense: ", defenseChooser);

		posChooser = new SendableChooser();
		posChooser.addDefault("1", PosEnum.POS1);
		posChooser.addObject("2", PosEnum.POS2);
		posChooser.addObject("3", PosEnum.POS3);
		posChooser.addObject("4", PosEnum.POS4);
		posChooser.addObject("5", PosEnum.POS5);
		posChooser.addObject("Spybot", PosEnum.SPYBOT);
		SmartDashboard.putData("Position: ", posChooser);

		shootChooser = new SendableChooser();
		shootChooser.addDefault("Don't Shoot", AimEnum.NONE);
		shootChooser.addObject("High Goal", AimEnum.HIGH);
		shootChooser.addObject("Low Goal", AimEnum.LOW);
		SmartDashboard.putData("Aim:", shootChooser);
	}
		

	@Override
	public void autonomousInit() {
		Logger.init();
		logTeamAndAllianceInfo();
		Logger.log("Robot.autonomousInit: Entering Auton");
		
		this.resetAll();
		drive.stop();

		aim = (AimEnum) shootChooser.getSelected();
		defense = (DefenseEnum) defenseChooser.getSelected();
		pos = (PosEnum) posChooser.getSelected();
		
		if ((defense == DefenseEnum.LOW_BAR && pos != PosEnum.POS1) || (defense != DefenseEnum.LOW_BAR && pos == PosEnum.POS1)) {
			defense = DefenseEnum.DO_NOTHING;
		}
		
		if (aim == AimEnum.LOW && (pos == PosEnum.POS3 || pos == PosEnum.POS4)) {
			defense =  DefenseEnum.DO_NOTHING;
		}
		
		auton = new StrongholdAuton(drive, camera, shooter, intake, gyro, (PosEnum) posChooser.getSelected(), aim,
				defense, accel, this);
		
		gyro.reset();
		auton.resetState();

		Logger.log("Robot.autonomousInit: Entered Auton");
	}

	@Override
	public void autonomousPeriodic() {
		isInAuton = true;
		auton.move();
		
		camera.getNTInfo(false); // in case not already called in move()
		SmartDashboard.putString("Auton State", auton.getState());
		debug();		
	}

	@Override
	public void disabledInit() {
		Logger.log("Robot.disabledInit: Entered DISABLED");
		Logger.close();
		System.out.println("Closed stream!");
	}
	 
	@Override
	public void disabledPeriodic() {
		camera.getNTInfo(false);
		debug();
		
		cBase.rumble(false);
	}

	@Override
	public void teleopInit() {
		Logger.log("Robot.teleopInit: Ending Auton (if in Auton before), Entering Teleop");
		drive.stop();
		this.resetAll();
		Logger.log("Robot.teleopInit: Entered Teleop");
	}
	

	@Override
	// Handle global manipulation of robot here
	public void teleopPeriodic() {	
		isInAuton = false;
		// Get initial info
		camera.getNTInfo(false);

		// Dealing with buttons on the different joysticks
		cBase.update();

		// Teleop Tank with DriveTrain
		drive.tankDrive(leftStick, rightStick);
		
		// Aborts shooting sequence
		if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN7)){
			Logger.log("INPUT: Abort button pressed");
			isShooting = false;
			drive.stop();
		}

		// Aims at target / initiates asynchronous shooting sequence
		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.LB)) {
			Logger.log("INPUT: Aim and shoot");
			Logger.log("SHOT!!!");
			aimProc(); // aims at the target / initiates asynchronous shooting sequence
		}

		// TODO maybe here we should always use AimEnum.HIGH because using a Auton setting in teleop is dangerous 
		shootProc(aim); // completes shooting sequence once aiming is successful (if initiated) 

		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.RB)) {
			Logger.log("INPUT: Shoot without aiming");
			shooter.shoot(); // shoot ball
		}
		
		// raising and lowering shooter elevator
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			Logger.log("INPUT: Put shooter arm in lowest position");
			shooter.raise(Shooter.DOWN);
			intake.lower(true);
			// intake.moveBall(0.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN2)) {
			Logger.log("INPUT: Put shooter arm in low pancake position");
			shooter.raise(Shooter.LOW); // pancake
			// intake.moveBall(0.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN1)) {
			Logger.log("INPUT: Put shooter in medium shooting position");
			shooter.raise(Shooter.MEDIUM); // shooting height
			intake.lower(true);
			// intake.moveBall(1.0);
		} else if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN3)) {
			Logger.log("INPUT: Put shooter in highest position");
			shooter.raise(Shooter.HIGH); // close shooting height
			// intake.moveBall(0.0);
		}

		//raising, lowering, and powering intake
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN3) || getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.Y)) {
			Logger.log("INPUT: Raise intake door");
			intake.lower(false); // up
		}

		if (getPressedDown(ControllerBase.Joysticks.LEFT_STICK, ControllerBase.JoystickButtons.BTN5)) {
			Logger.log("INPUT: Turn intake motor on in forward direction");
			intake.moveBall(-1.0); // pull ball in
			intake.lower(true); // down
		}
		
		if (getPressedDown(ControllerBase.Joysticks.RIGHT_STICK, ControllerBase.JoystickButtons.BTN4)) {
			Logger.log("INPUT: Turn intake motor off");
			intake.moveBall(0); // stop intake
		}
		
		if (getPressedDown(ControllerBase.Joysticks.GAMEPAD, ControllerBase.GamepadButtons.BACK)) {
			Logger.log("INPUT: Reverse intake");
			intake.moveBall(1.0); // push ball out
		}

		//makes controller rumble when the robot is able to take a shot
		if (camera.isInDistance() && camera.isInLineWithGoal()) {
			//Logger.log("In range to shoot! Press the button!");
			cBase.rumble(true);
		} else {
			cBase.rumble(false);
		}
		/*
		while(drive.checkDegreeRotateVoltage()) {
			Timer.delay(0.1);
		}*/
		
		
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
		shootingAttemptCounter = 0;
		Logger.log("Robot.aimProc: about to raise intake");
		intake.lower(false);
		Logger.log("Robot.aimProc: intake raised");

		if (camera.isInDistance() && camera.isInLineWithGoal()) {
			Logger.log("Robot.aimProc: in distance and in line with goal");
			boolean initialPancake = shooter.isElevatorUp();
			shooter.raiseShootingHeight(camera);
			if (shooter.isElevatorUp() != initialPancake){
				Logger.log("Robot.aimProc: waiting for shooter to catch up...");
				Timer.delay(Shooter.RAISE_SHOOTER_CATCHUP_DELAY_SECS); 
				Logger.log("Robot.aimProc: done waiting for shooter to catch up");
			}				// waits for shooter to get in position
			isShooting = true;
			drive.degreeRotateVoltage(camera.getTurnAngle());
			Logger.log("Robot.aimProc: shooting sequence started. Good luck.");
		}
		else {
			Logger.log("Robot.aimProc: not in distance or not in line with goal. Sorry.");
		}
	}
	
	/**
	 * <pre>
	 * public void shootProc()
	 * </pre>
	 * Goes through the shooting procedure (requires prior call to aimProc()).
	 * 
	 * @param aim aim action
	 */
	public void shootProc(AimEnum aim) {
		double recenteredMoveDistance;
		if (!drive.checkDegreeRotateVoltage() && isShooting) { 
			Logger.log("Robot.shootProc: done rotating, shooting sequence continuing");
			camera.getNTInfo(true);

			if (camera.isInTurnAngle()) {
				Logger.log("Robot.shootProc: in turn angle, will shoot");
				isShooting = false;
				if (aim == AimEnum.LOW && isInAuton) {
					Logger.log("shootProc: AimEnum.LOW auton hack called");
					// calculates how far the batter is from where we are now
					recenteredMoveDistance = Math.max(0.0, camera.getHorizontalDist() - StrongholdAuton.LENGTH_OF_BATTER_FEET); 
					
					// If distance to center is not unrealistic, continue
					if (recenteredMoveDistance < StrongholdAuton.MAX_RECENTER_DISTANCE_FEET) {					
						drive.moveDistance(recenteredMoveDistance, 0.4, 0, 0, 4.5);
						drive.waitMove();
						shooter.raise(Shooter.DOWN);
					}
					Logger.log("Robot.shootProc: AimEnum.LOW auton hack done");
				} 
				
				Logger.log("Robot.shootProc: SHOOTING PARAMETERS ARE AS FOLLOW:");
				Logger.log("SHOOTING: Camera Diagonal Distance: " + Utilities.round(camera.getDiagonalDist(), 3) + " ft.");
				Logger.log("SHOOTING: Camera Horizontal Distance: " + Utilities.round(camera.getHorizontalDist(), 3) + " ft.");
				Logger.log("SHOOTING: Camera Opening Width: " + Utilities.round(camera.getOpeningWidth(), 3) + " in.");
				Logger.log("SHOOTING: Camera Turn Angle: " + Utilities.round(camera.getTurnAngle(), 3) + " deg.");
				Logger.log("SHOOTING: Accel Tilt" + Utilities.round(accel.getTilt(), 3) + " deg.");
				Logger.log("SHOOTING: Is flat: " + accel.isFlat());
				Logger.log("SHOOTING: Pressure: " + Utilities.round(compressor.getPressurePSI(), 3) + " PSI");
				Logger.log("SHOOTING: Is enough pressure: " + compressor.isInShotPressure());
				
				shooter.shoot();
				Logger.log("Robot.shootProc: shot made!");
			} else if (shootingAttemptCounter < MAX_SHOOTING_ATTEMPT) {
				drive.degreeRotateVoltage(camera.getTurnAngle());
				shootingAttemptCounter++;
				Logger.log("Robot.shootProc: not in turn angle, will try again");
			} else {
				isShooting = false; 
				Logger.log("Robot.shootProc: gave up trying");
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
		SmartDashboard.putString("Press", "" + Utilities.round(compressor.getPressurePSI(), 3) + " PSI");
		SmartDashboard.putBoolean("Is enough pressure", compressor.isInShotPressure());
	}
	
	public void logTeamAndAllianceInfo() {
		if (driverStation != null) {
			Logger.log("TEAM Location: " + driverStation.getLocation());
			
			Alliance alliance = driverStation.getAlliance();
			
			if (alliance != null) { 
				Logger.log("TEAM Alliance: " + alliance.toString());
			}
			
			if (config != null) {
				Logger.log("CONFIG: " + config.toString());
			}
		}				
	}
}
