package org.usfirst.frc.team1089.auton;


import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Intake;
import org.usfirst.frc.team1089.robot.Logger;
import org.usfirst.frc.team1089.robot.MercAccelerometer;
import org.usfirst.frc.team1089.robot.Robot;
import org.usfirst.frc.team1089.robot.Shooter;

import edu.wpi.first.wpilibj.AnalogGyro;
//import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The {@code StrongholdAuton} class contains fields and methods for crossing a defense
 * and shooting in the high/low goal during auton
 */
public class StrongholdAuton {
	// states of auton
	private static final int START = 0, BREACH = 1, STRAIGHTEN = 2,
								MOVE1 = 3, ROTATE1 = 4,
								CALCULATE = 5, MOVE2 = 6, AIM = 7, SHOOT = 8, 
								DONE = 9;
	
	// various safety checks
	public static final double LENGTH_OF_BATTER_FEET = 4.0, 
								MAX_DISTANCE_TO_GOAL_FEET = 20.0, MIN_DISTANCE_TO_GOAL_FEET = 8.0, // arbitrary distances for abnormal checks after move - can be out of shooting range
								MAX_CLOSE_DISTANCE_TO_GOAL_FEET = 11.0, MIN_CLOSE_DISTANCE_TO_GOAL_FEET = 4.0, // arbitrary distances for abnormal checks after move - can be out of shooting range
								MAX_CENTER_DISTANCE_FEET = 6.0, MAX_CENTER_CLOSE_DISTANCE_FEET = 6.0, // max forward distances for center/re-center - need to allow to reach shooting range!
								MAX_RECENTER_DISTANCE_FEET = 10.0, 
								SPYBOT_DRIVE_DISTANCE_FEET = 4.0,
								MAX_CENTER_DISTANCE_BACKWARDS_FEET = 0.0, // max backward distance for center - need to allow to reach shooting range! (always use positive number)
								MAX_CENTER_CLOSE_DISTANCE_BACKWARDS_FEET = 0.0; // for close distance we do not allow to go back for now (always use positive number)
	
	// distances for MOVE1
	private static int  MOVE_DISTANCE_POST_DEFENSE_P1_FEET = 6 - Defense.BUFFER_AFTER_DEFENSE_FEET,
						MOVE_DISTANCE_POST_DEFENSE_P2_FEET_LEFT_PATH = 9  - Defense.BUFFER_AFTER_DEFENSE_FEET,
						MOVE_DISTANCE_POST_DEFENSE_P2_FEET_CENTER_PATH = 3  - Defense.BUFFER_AFTER_DEFENSE_FEET,
						/*MOVE_DISTANCE_POST_DEFENSE_SIDEWAY_P3_FEET = 3,*/
						MOVE_DISTANCE_POST_DEFENSE_P4_FEET = 3  - Defense.BUFFER_AFTER_DEFENSE_FEET, 
						MOVE_DISTANCE_POST_DEFENSE_P5_FEET = 10  - Defense.BUFFER_AFTER_DEFENSE_FEET;
	
	// angles for ROTATE1
	private static int ROTATE_POST_DEFENSE_P1_DEGREES = 60,
						ROTATE_POST_DEFENSE_P2_DEGREES_LEFT_PATH = 60, 
						ROTATE_POST_DEFENSE_P2_DEGREES_CENTER_PATH = 45,
						/*ROTATE_POST_DEFENSE_P3_DEGREES = 0,*/
						/*ROTATE_POST_DEFENSE_P4_DEGREES = 0,*/ 
						ROTATE_POST_DEFENSE_P5_DEGREES = -60;
	
	// maximum optimal shooting distances
	private static int SHOOT_DISTANCE_P1_P3_P4_FEET = 11, // maximum distance for long shot (farthest optimal distance) - need to be within shooting range!
					SHOOT_DISTANCE_P2_FEET_LEFT_PATH = 5,	// shooting from left, short shot
					SHOOT_DISTANCE_P2_FEET_CENTER_PATH = 11,	//shooting from center, long shot 
					/*SHOOT_DISTANCE_P3_FEET = 11,
					SHOOT_DISTANCE_P4_FEET = 11,*/ 
					SHOOT_DISTANCE_P5_FEET = 5; // maximum distance for short shot (farthest optimal distance) - need to be within shooting range!

	
	private Defense defense;
	private Camera camera;
	private int state = 0, breachAttempts = 0, centerAttempts = 0;
	private PosEnum pos;
	private double centeredMoveDistance;
	private AimEnum aim;
	private DefenseEnum defenseEnum;
	private MercAccelerometer accel;
	private Shooter shooter;
	private AnalogGyro gyro;
	protected DriveTrain drive;
	private Robot robot;
	private Intake intake;

	/**
	 * <pre>
	 * public StrongholdAuton()
	 * </pre>
	 * Constructs a new {@code StrongholdAuton} with the specified parameters
	 * 
	 * @param d
	 *            the {@code DriveTrain} for driving the robot
	 * @param c
	 *            the {@code Camera} for seeing the goal
	 * @param s
	 *            the {@code Shooter} for shooting the ball into either goal
	 * @param i
	 *            the {@code Intake} for getting the ball            
	 * @param g
	 *            the {@code AnalogGyro} for centering the robot after coming off of a defense
	 * @param p		
	 *            the {@code PosEnum} shows the initial position            
	 * @param a
	 *            the {@code AimEnum} used for position of the shooter based on which goal we are shooting in
	 * @param dE
	 * 			  the {@code DefenseEnum} says which defense we will be crossing
	 * @param ac
	 * 			  the {@code MercAccelerometer} used to see if we have breached, with the z-axis
	 *
	 * @param r
	 *            the robot            
	 */
	public StrongholdAuton(DriveTrain d, Camera c, Shooter s, Intake i, AnalogGyro g, PosEnum p,
							AimEnum a, DefenseEnum dE, MercAccelerometer ac, Robot r) {
		drive = d;
		camera = c;
		pos = p;
		aim = a;
		defenseEnum = dE;
		shooter = s;
		gyro = g;
		accel = ac;
		robot = r;
		intake = i;
		defense = new Defense(drive, shooter, dE);
	}

	public void resetState() {
		Logger.log("Auton.resetState: Reset auton state");
		state = START;
		breachAttempts = 0;
		centerAttempts = 0;
	}
	
	// START = 0, BREACH = 1, STRAIGHTEN = 2, MOVE1 = 3, ROTATE1 = 4,
	// CALCULATE = 5, MOVE2 = 6, AIM = 7, SHOOT = 8, 
	// DONE = 9;
	public String getState() {
		switch(state) {
		case START:
			return "START";
		case BREACH:
			return "BREACH";
		case STRAIGHTEN:
			return "STRAIGHTEN";
		case MOVE1:
			return "MOVE1";			
		case ROTATE1:
			return "ROTATE1";
		case CALCULATE:
			return "CALCULATE";
		case MOVE2:
			return "MOVE2";
		case AIM:
			return "AIM";
		case SHOOT:
			return "SHOOT";
		case DONE:
			return "DONE";
		default:
			return "UNKNOWN";
		}
	}
	
	/**
	 * <pre>
	 * public void move()
	 * </pre>
	 * 
	 * Crosses defense, centers, moves 10 feet away from goal, and shoots in auton
	 * 
	 */
	public void move() {  
		
		if (state == START && defenseEnum == DefenseEnum.DO_NOTHING){ // we only do this hack once
			Logger.log("Auton X entered DefenseEnum.DO_NOTHING hack");
			state = DONE;
			Logger.log("Auton X forced DONE state because DefenseEnum.DO_NOTHING was selected");
		}
		
		if (state == START && pos == PosEnum.SPYBOT){ // we only do this hack once or we will never shoot
			Logger.log("Auton X entered PosEnum.SPYBOT hack");
			drive.moveDistance(SPYBOT_DRIVE_DISTANCE_FEET, 0.4, 0, 0, 6.0);
			drive.waitMove(); // we cannot go to the next state until we are done moving (or if we timeout)
			state = AIM;
			Logger.log("Auton X forced AIM state because PosEnum.SPYBOT was selected");
		}
		
		switch (state) {
			case START: {// Adjust shooter/intake

				breachAttempts = 0;
				if(pos == PosEnum.POS1) {
					Logger.log("Auton START about to lower intake because PosEnum.POS1 was selected");
					intake.lower(true);
					Logger.log("Auton START lowered intake because PosEnum.POS1 was selected");
				}
				
				state++;
				breachAttempts = 0;
				Logger.log("Auton START FINISHED");
				break;
			}
			case BREACH: {//Breaching Phase
				if (breachAttempts == 0) {
					Logger.log("Auton BREACH about to attempt first breach");
					defense.breach();
					breachAttempts++; //Breach only once
					Logger.log("Auton BREACH attempted breach");
				}
				if (accel.isFlat()) { // loops until flat (but only while autonPeriodic is called) - TODO should we do anything to help if not?
					Logger.log("Auton BREACH about to raise shooter");
					shooter.raise(Shooter.MEDIUM);
					Logger.log("Auton BREACH raised shooter");
					state++;
					Logger.log("Auton BREACH FINISHED");
				} else {
					// accel is not flat - do nothing for now
				}
				break;
			}
			case STRAIGHTEN: {//Straighten
				Logger.log("Auton STRAIGHTEN about to raise intake");
				intake.lower(false);
				Logger.log("Auton STRAIGHTEN raised intake");
				
				if (aim == AimEnum.NONE) {
					state = DONE;
					Logger.log("Auton STRAIGHTEN FINISHED. Jumping to DONE state because AimEnum.NONE was selected");
				}
				else {
					if (pos != PosEnum.POS1) { // we only skip correction in POS1
						Logger.log("Auton STRAIGHTEN about to straighten using gyro");
						drive.degreeRotateVoltage(-gyro.getAngle()); // Assume gyro has been reset to zero before breaching
						drive.waitDegreeRotateVoltage();
						Logger.log("Auton STRAIGHTEN straightened using gyro");
					}
					state++;
					Logger.log("Auton STRAIGHTEN FINISHED");
				}
				break;
			}
			case MOVE1: {//Move to rotation point if needed
				if (pos == PosEnum.POS1) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS1");
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P1_FEET, 0.42, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS1");
				} else if (pos == PosEnum.POS2_LEFT_PATH) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS2_LEFT_PATH");
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P2_FEET_LEFT_PATH, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS2_LEFT_PATH");
				} else if (pos == PosEnum.POS2_CENTER_PATH) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS2_CENTER_PATH");
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P2_FEET_CENTER_PATH, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS2_CENTER_PATH");	
				} else if (pos == PosEnum.POS3) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS3");
					//drive.degreeRotateVoltage(60);
					//drive.waitDegreeRotateVoltage();
					//drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_SIDEWAY_P3_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					//drive.waitMove();			
					//drive.degreeRotateVoltage(-60);
					//drive.waitDegreeRotateVoltage();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS3");
				} else if (pos == PosEnum.POS4) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS4");
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P4_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS4");
				} else if (pos == PosEnum.POS5) {
					Logger.log("Auton MOVE1 about to attempt move PosEnum.POS5");
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P5_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					Logger.log("Auton MOVE1 attempted move PosEnum.POS5");
				}				
				state++;
				Logger.log("Auton MOVE1 FINISHED");
				break;
			}
			case ROTATE1: {//Rotate towards goal without relying on camera (as we might not see the goal yet)
				if (pos == PosEnum.POS1) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS1");
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P1_DEGREES);
					drive.waitDegreeRotateVoltage();
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS1");
				} else if (pos == PosEnum.POS2_LEFT_PATH) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS2_LEFT_PATH");
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P2_DEGREES_LEFT_PATH);
					drive.waitDegreeRotateVoltage();
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS2_LEFT_PATH");
				} else if (pos == PosEnum.POS2_CENTER_PATH) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS2_CENTER_PATH");
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P2_DEGREES_CENTER_PATH);
					drive.waitDegreeRotateVoltage();
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS2_CENTER_PATH");	
				} else if (pos == PosEnum.POS3) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS3");
					// do nothing
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS3");
				} else if (pos == PosEnum.POS4) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS4");
					// do nothing
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS4");
				} else if (pos == PosEnum.POS5) {
					Logger.log("Auton ROTATE1 about to attempt rotate PosEnum.POS5");
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P5_DEGREES);
					drive.waitDegreeRotateVoltage();
					Logger.log("Auton ROTATE1 attempted rotation PosEnum.POS5");
				}
				Logger.log("Auton ROTATE1 about to raise intake");
				intake.lower(false);
				Logger.log("Auton ROTATE1 raised intake");
				
				state++;
				Logger.log("Auton ROTATE1 FINISHED");
				break;
			}
			case CALCULATE: { //Use camera to figure out how far we really are from the goal
				camera.getNTInfo(true);
				
				if (pos == PosEnum.POS1 || pos == PosEnum.POS3 || pos == PosEnum.POS4) { // in cases where we expect to be far
					if (camera.getHorizontalDist() > MAX_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_DISTANCE_TO_GOAL_FEET){
						state = DONE;
						Logger.log("Auton CALCULATE FINISHED (abnormal horizontal distance) PosEnum.POS1, POS3 or POS4");
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0,camera.getHorizontalDist() - SHOOT_DISTANCE_P1_P3_P4_FEET);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
							state++;
							Logger.log("Auton CALCULATE FINISHED (OK) PosEnum.POS1, POS3 or POS4");
						}
						else {
							state = DONE;
							Logger.log("Auton CALCULATE FINISHED (abnormal centered move distance) PosEnum.POS1, POS3 or POS4");
						}
					}
				}
				else if (pos == PosEnum.POS2_LEFT_PATH) { // in cases where we expect to be close
					if (camera.getHorizontalDist() > MAX_CLOSE_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_CLOSE_DISTANCE_TO_GOAL_FEET){
						state = DONE;
						Logger.log("Auton CALCULATE FINISHED (abnormal horizontal distance) PosEnum.POS2_LEFT_PATH");
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0,camera.getHorizontalDist() - SHOOT_DISTANCE_P2_FEET_LEFT_PATH);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_CLOSE_DISTANCE_FEET) {
							state++;
							Logger.log("Auton CALCULATE FINISHED (OK) PosEnum.POS2_LEFT_PATH");
						}
						else {
							state = DONE;
							Logger.log("Auton CALCULATE FINISHED (abnormal centered move distance) PosEnum.POS2_LEFT_PATH");
						}
					}
				}
				else if (pos == PosEnum.POS2_CENTER_PATH) { // in cases where we expect to be far
					if (camera.getHorizontalDist() > MAX_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_DISTANCE_TO_GOAL_FEET){
						state = DONE;
						Logger.log("Auton CALCULATE FINISHED (abnormal horizontal distance) PosEnum.POS2_CENTER_PATH");
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0,camera.getHorizontalDist() - SHOOT_DISTANCE_P2_FEET_CENTER_PATH);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
							state++;
							Logger.log("Auton CALCULATE FINISHED (OK) PosEnum.POS2_CENTER_PATH");
						}
						else {
							state = DONE;
							Logger.log("Auton CALCULATE FINISHED (abnormal centered move distance) PosEnum.POS2_CENTER_PATH");
						}
					}
				}
				else if (pos == PosEnum.POS5) { // in cases where we expect to be close
					if (camera.getHorizontalDist() > MAX_CLOSE_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_CLOSE_DISTANCE_TO_GOAL_FEET){
						state = DONE;
						Logger.log("Auton CALCULATE FINISHED (abnormal horizontal distance) PosEnum.POS5");
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0, camera.getHorizontalDist() - SHOOT_DISTANCE_P5_FEET);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_CLOSE_DISTANCE_FEET) {
							state++;
							Logger.log("Auton CALCULATE FINISHED (OK) PosEnum.POS5");
						}
						else {
							state = DONE;
							Logger.log("Auton CALCULATE FINISHED (abnormal centered move distance) PosEnum.POS5");
						}
					}
				}
				break;
			}
			case MOVE2: {//Move forward so that distance from goal is an acceptable distance
				Logger.log("Auton MOVE2 about to attempt move");
				drive.moveDistance(centeredMoveDistance, 0.4, 0, 0, 4.5);
				drive.waitMove();
				Logger.log("Auton MOVE2 attempted move");
				state++;
				Logger.log("Auton MOVE2 FINISHED");
				break;
			}
			case AIM: {// Aim
				if (centerAttempts == 0) {
					Logger.log("Auton AIM about to start aim proc");
					robot.aimProc();
					centerAttempts++;
					Logger.log("Auton AIM started aim proc");
				}
				
				if(!drive.checkDegreeRotateVoltage()) {
					state++;
					Logger.log("Auton AIM FINISHED ");
				}
				break;
			}
			case SHOOT: {//Shoot into high or low goal
				if (aim == AimEnum.HIGH) {
					robot.shootProc(aim);
					if (!robot.isShooting()) {
						state++;
						Logger.log("Auton SHOOT FINISHED (AimEnum.HIGH)");
					}
				}
				else if (aim == AimEnum.LOW) {
					robot.shootProc(aim);
					if (!robot.isShooting()) {
						state++;
						Logger.log("Auton SHOOT FINISHED (AimEnum.LOW)");
					}
				}
				else {
					state = DONE;
					Logger.log("Auton SHOOT FINISHED (not aiming)");
				}
				break;
			}
			case DONE: {
				return;
					
			}
		}
	}
}