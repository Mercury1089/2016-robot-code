package org.usfirst.frc.team1089.auton;


import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Intake;
import org.usfirst.frc.team1089.robot.Logger;
import org.usfirst.frc.team1089.robot.MercAccelerometer;
import org.usfirst.frc.team1089.robot.Robot;
import org.usfirst.frc.team1089.robot.Shooter;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Timer;

/**
 * The {@code StrongholdAuton} class contains fields and methods for crossing a defense
 * and shooting in the high/low goal during auton
 */
public class StrongholdAuton {
	// states of auton
	private static final int START = 0, BREACH = 1, MOVE1 = 2, STRAIGHTEN = 3, ROTATE1 = 4,
								CALCULATE = 5, MOVE2 = 6, AIM = 7, SHOOT = 8, 
								DONE = 9;
	
	// various safety checks
	public static final double LENGTH_OF_BATTER_FEET = 4.0, 
								MAX_DISTANCE_TO_GOAL_FEET = 20.0, MIN_DISTANCE_TO_GOAL_FEET = 7.0, 
								MAX_CLOSE_DISTANCE_TO_GOAL_FEET = 11.0, MIN_CLOSE_DISTANCE_TO_GOAL_FEET = 4.0,
								MAX_CENTER_DISTANCE_FEET = 6.0, MAX_RECENTER_DISTANCE_FEET = 10.0;
	
	// distances for MOVE1
	private static int MOVE_DISTANCE_POST_DEFENSE_P1_FEET = 6,
						MOVE_DISTANCE_POST_DEFENSE_P2_FEET = 9,
						MOVE_DISTANCE_POST_DEFENSE_SIDEWAY_P3_FEET = 3,
						MOVE_DISTANCE_POST_DEFENSE_P4_FEET = 3, 
						MOVE_DISTANCE_POST_DEFENSE_P5_FEET = 10;
	
	// angles for ROTATE1
	private static int ROTATE_POST_DEFENSE_P1_DEGREES = 60, //TODO From our testing that we had done on the last day of build 
															//45 degrees was too much so I'm not sure why we are going to 60.
						ROTATE_POST_DEFENSE_P2_DEGREES = 60,
						/*ROTATE_POST_DEFENSE_P3_DEGREES = 0,*/
						/*ROTATE_POST_DEFENSE_P4_DEGREES = 0,*/ 
						ROTATE_POST_DEFENSE_P5_DEGREES = -60;
	
	// maximum optimal shooting distances
	private static int SHOOT_DISTANCE_P1_P3_P4_FEET = 11,
					SHOOT_DISTANCE_P2_P5_FEET = 5;
					/*SHOOT_DISTANCE_P3_FEET = 11,
					SHOOT_DISTANCE_P4_FEET = 11,*/ 
					/*SHOOT_DISTANCE_P5_FEET = 5;*/
	
	private Defense defense;
	private Camera camera;
	private int state = 0, breachAttempts = 0, centerAttempts = 0;
	private PosEnum pos;
	private double centeredMoveDistance;
	private AimEnum aim;
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
		aim = a ;
		shooter = s;
		gyro = g;
		accel = ac;
		robot = r;
		intake = i;
		defense = new Defense(drive, shooter, dE);
	}

	public void resetState() {
		Logger.log("Reset auton state");
		state = START;
		breachAttempts = 0;
		centerAttempts = 0;
	}
	
	// START = 0, BREACH = 1, MOVE1 = 2, STRAIGHTEN = 3, ROTATE1 = 4,
	// CALCULATE = 5, MOVE2 = 6, AIM = 7, SHOOT = 8, 
	// DONE = 9;
	public String getState() {
		switch(state) {
		case START:
			return "START";
		case BREACH:
			return "BREACH";
		case MOVE1:
			return "MOVE1";
		case STRAIGHTEN:
			return "STRAIGHTEN";
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
		switch (state) {
			case START: {// Adjust shooter/intake
				intake.lower(true);
				state++;
				Logger.log("Auton finish case: START");
				break;
			}
			case BREACH: {//Breaching Phase
				if (breachAttempts == 0) {
					defense.breach();
					breachAttempts++; //Breach only once
				}
				if (accel.isFlat()) { // loops until flat - TODO should we do anything to help if not?
					shooter.raise(Shooter.MEDIUM);
					state++;
					Logger.log("Auton finish case: BREACH");
				}	
				break;
			}
			case STRAIGHTEN: {//Straighten
				if (aim == AimEnum.NONE) {
					state = DONE;
				}
				else {
					if (pos != PosEnum.POS1) { // we only skip correction in POS1
						drive.degreeRotateVoltage(-gyro.getAngle()); // Assume gyro has been reset to zero before breaching
						drive.waitDegreeRotateVoltage();
					}
					state++;
					Logger.log("Auton finish case: STRAIGHTEN");
				}
				break;
			}
			case MOVE1: {//Move to rotation point if needed
				if (pos == PosEnum.POS1) {
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P1_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
				} else if (pos == PosEnum.POS2) {
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P2_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
				} else if (pos == PosEnum.POS3) {
					drive.degreeRotateVoltage(60);
					drive.waitDegreeRotateVoltage();
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_SIDEWAY_P3_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
					drive.degreeRotateVoltage(-60);
					drive.waitDegreeRotateVoltage();
				} else if (pos == PosEnum.POS4) {
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P4_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
				} else if (pos == PosEnum.POS5) {
					drive.moveDistance(MOVE_DISTANCE_POST_DEFENSE_P5_FEET, 0.4, 0, 0, 6.0); //TODO test and change these values
					drive.waitMove();
				}				
				state++;
				Logger.log("Auton finish case: MOVE1");
				break;
			}
			case ROTATE1: {//Rotate towards goal without relying on camera (as we might not see the goal yet)
				if (pos == PosEnum.POS1) {
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P1_DEGREES);// 35
					drive.waitDegreeRotateVoltage();
				} else if (pos == PosEnum.POS2) {
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P2_DEGREES);
					drive.waitDegreeRotateVoltage();
				} else if (pos == PosEnum.POS3) {
					// do nothing
				} else if (pos == PosEnum.POS4) {
					// do nothing
				} else if (pos == PosEnum.POS5) {
					drive.degreeRotateVoltage(ROTATE_POST_DEFENSE_P5_DEGREES);
					drive.waitDegreeRotateVoltage();
				}						
				intake.lower(false);
				state++;
				Logger.log("Auton finish case: ROTATE1");
				break;
			}
			case CALCULATE: { //Use camera to figure out how far we really are from the goal
				camera.getNTInfo(true);
				
				if (pos == PosEnum.POS1 || pos == PosEnum.POS3 || pos == PosEnum.POS4) { // in cases where we expect to be far
					if (camera.getHorizontalDist() > MAX_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_DISTANCE_TO_GOAL_FEET){
						state = DONE;
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0,camera.getHorizontalDist() - SHOOT_DISTANCE_P1_P3_P4_FEET);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
							state++;
							Logger.log("Auton finish case: CALCULATE");
						}
						else {
							state = DONE;
						}
					}
				} else if (pos == PosEnum.POS2 || pos == PosEnum.POS5) { // in cases where we expect to be close
					if (camera.getHorizontalDist() > MAX_CLOSE_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_CLOSE_DISTANCE_TO_GOAL_FEET){
						state = DONE;
					}
					else {
						//Assume we are looking at the correct goal
						centeredMoveDistance = Math.max(0.0, camera.getHorizontalDist() - SHOOT_DISTANCE_P2_P5_FEET);
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
							state++;
							Logger.log("Auton finish case: CALCULATE");
						}
						else {
							state = DONE;
						}
					}
				}
				break;
			}
			case MOVE2: {//Move forward so that distance from goal is an acceptable distance
				drive.moveDistance(centeredMoveDistance, 0.4, 0, 0, 4.5);
				drive.waitMove();
				state++;
				Logger.log("Auton finish case: MOVE2");
				break;
			}
			case AIM: {// Aim
				if (centerAttempts == 0) {
					robot.aimProc();
					centerAttempts++;
				}
				
				if(!drive.checkDegreeRotateVoltage()) {
					state++;
					Logger.log("Auton finish case: AIM");
				}
				break;
			}
			case SHOOT: {//Shoot into high or low goal
				if (aim == AimEnum.HIGH) {
					robot.shootProc(aim);
					if (!robot.isShooting()) {
						state++;
						Logger.log("Auton finish case: SHOOT");
					}
				}
				else if (aim == AimEnum.LOW && (pos != PosEnum.POS3 && pos != PosEnum.POS4)) {
					robot.shootProc(aim);
					if (!robot.isShooting()) {
						state++;
						Logger.log("Auton finish case: SHOOT");
					}
				}
				else {
					state = DONE;
				}
				break;
			}
			case DONE: {
				return;
					
			}
		}
	}
}