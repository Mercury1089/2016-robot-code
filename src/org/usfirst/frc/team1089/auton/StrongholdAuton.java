package org.usfirst.frc.team1089.auton;


import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Intake;
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
	private static final int START = 0, BREACH = 1, MOVE1 = 2, STRAIGHTEN = 3, ROTATE1 = 4,
								CALCULATE = 5, MOVE2 = 6, ROTATE2 = 7, AIM = 8, SHOOT = 9, 
								DONE = 10;
	
	private static final double TURN_SPEED = 0.5, DISTANCE_TO_HIGH_GOAL_FEET = 9.0, LENGTH_OF_BATTER_FEET = 4.0, 
								DISTANCE_TO_GET_TO_LOW_GOAL_FEET = DISTANCE_TO_HIGH_GOAL_FEET - LENGTH_OF_BATTER_FEET, 
								MAX_DISTANCE_TO_GOAL_FEET = 20.0, MIN_DISTANCE_TO_GOAL_FEET = 10.0, 
								MAX_CENTER_DISTANCE_FEET = 7.0, MIN_CENTER_DISTANCE_FEET = 0.0;
	private Defense defense;
	private Camera camera;
	private int state = 0, breachAttempts = 0;
	private PosEnum pos;
	private double centeredMoveDistance, angleToTurn, supportAngle;
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
				shooter.raise(Shooter.MEDIUM);
				intake.lower(true);
				state++;
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
				}
				  
				break;
			}
			case STRAIGHTEN: {//Straighten
				//TODO for P1/low bar we probably do not need to straighten, but for P2 to P5 we should consider it
				/*if (aim == AimEnum.NONE) {
					state = DONE;
				}
				else {
					drive.degreeRotateVoltage(-gyro.getAngle()); // Assume gyro has been reset to zero before breaching
					drive.waitDegreeRotateVoltage();
					state++;
				}*/
				state++;
				break;
			}
			case MOVE1: {//Move to rotation point if needed
				// TODO consider moving at high speed after passing defense
				state++;
				break;
			}
			case ROTATE1: {//Rotate towards goal without relying on camera
				// TODO take initial position into account, and even for P1 consider increasing angle
				drive.degreeRotateVoltage(35);// will need to eventually take position into account
				drive.waitDegreeRotateVoltage();
				intake.lower(false);
				state++;
				break;
			}
			case CALCULATE: { //Use camera to figure out how far we really are from the goal
				Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
				camera.getNTInfo();
				//If the distance from goal is unrealistic, then abort
				// TODO MAX_DISTANCE_TO_GOAL_FEET and MIN_DISTANCE_TO_GOAL_FEET should be position-specific (or at least not the same for all positions)
				// TODO also we might need to consider if aiming high or low
				// TOOD It might simplify things to always aim high for P1, P3 & P4; and always low for P2 and P5
				if (camera.getHorizontalDist() > MAX_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_DISTANCE_TO_GOAL_FEET){
					state = DONE;
				}
				else {
					//Assume we are looking at the correct goal
					//angleToTurn = Math.asin(Math.sin((camera.getTurnAngle() * camera.getHorizontalDist()) / DISTANCE_TO_HIGH_GOAL_FEET));
					//supportAngle = 180 - camera.getTurnAngle() - angleToTurn;
					// TODO we might not always want to shoot 11 feet from the goal - this needs to take initial position into account
					centeredMoveDistance = camera.getHorizontalDist() - 11;
							//(DISTANCE_TO_HIGH_GOAL_FEET * Math.sin(supportAngle)) / Math.sin(camera.getTurnAngle());
					// If distance to center is not unrealistic, continue
					if (centeredMoveDistance > MIN_CENTER_DISTANCE_FEET && centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
						state++;
					}
					else {
						state = DONE;
					}
				}
				break;
			}
			case MOVE2: {//Move so that distance from goal is 9 feet (or generally speaking an acceptable distance)
				drive.moveDistanceAuton(centeredMoveDistance, 0.4, 0, 0, 4.5);
				drive.waitMove();
				Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
				camera.getNTInfo();
				// TODO why do we rotate here and not in the next step?
				drive.degreeRotateVoltage(camera.getTurnAngle());
				drive.waitDegreeRotateVoltage();
				Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
				camera.getNTInfo();
				// check we are within shooting range
				if (camera.isInDistance() && camera.isInLineWithGoal()) {
					state++;
				}
				else {
					state = DONE;
				}
				break;
			}
			case ROTATE2: {//Rotate again using camera - we could skip if we will aim later
				/*// auto-rotate?
				camera.getNTInfo();
				if (camera.isInTurnAngle()) {
					state++;
				}
				else {
					state = DONE;
				}*/
				state++;
				break;
			}
			case AIM: {// Aim
				if (aim == AimEnum.HIGH) {
					robot.aimProc();
				}
				state++;
				break;
			}
			case SHOOT: {//Shoot into high or low goal
				if (aim == AimEnum.HIGH) {
					robot.shootProc();
					if (!robot.isShooting()) {
						state++;
					}
					//shooter.shootProcedure();
				}
				else if (aim == AimEnum.LOW) {
					drive.moveDistanceAuton(DISTANCE_TO_GET_TO_LOW_GOAL_FEET, 0.4, 0, 0, 4.5);
					drive.waitMove();
					shooter.raise(Shooter.DOWN);
					shooter.shoot();
					state++;
				}
				break;
			}
			case DONE: {
				return;
					
			}
		}
	}
}