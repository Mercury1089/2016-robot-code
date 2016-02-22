package org.usfirst.frc.team1089.auton;


import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
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
	private static final int BREACH = 0, CENTER = 1, MOVE = 2, ROTATE = 3, SHOOT = 4, DONE = 5;
	private static final double TURN_SPEED = 0.5, DISTANCE_TO_HIGH_GOAL_FEET = 9.0, LENGTH_OF_BATTER_FEET = 4.0, 
								DISTANCE_TO_GET_TO_LOW_GOAL_FEET = DISTANCE_TO_HIGH_GOAL_FEET - LENGTH_OF_BATTER_FEET, 
								MAX_DISTANCE_TO_GOAL_FEET = 20.0, MIN_DISTANCE_TO_GOAL_FEET = 10.0, 
								MAX_CENTER_DISTANCE_FEET = 7.0, MIN_CENTER_DISTANCE_FEET = 0.0;
	private Defense defense;
	private Camera camera;
	private int pos, state = 0, breachAttempts = 0;
	private double centeredMoveDistance, angleToTurn, supportAngle;
	private AimEnum aim;
	private MercAccelerometer accel;
	private Shooter shooter;
	private AnalogGyro gyro;
	protected DriveTrain drive;
	private Robot robot;

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
	 *            the initial position            
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
	public StrongholdAuton(DriveTrain d, Camera c, Shooter s, AnalogGyro g, int p,
							AimEnum a, DefenseEnum dE, MercAccelerometer ac, Robot r) {
		drive = d;
		camera = c;
		pos = p;
		defense = new Defense(drive, shooter, dE);
		aim = a ;
		shooter = s;
		gyro = g;
		accel = ac;
		robot = r;
		
	}

	/**
	 * <pre>
	 * public void move()
	 * </pre>
	 * 
	 * Crosses defense, centers, moves 9 feet away from goal, and shoots in auton
	 * 
	 */
	public void move() {
		switch (state) {
			case BREACH: {//Breaching Phase
				shooter.raise(shooter.MEDIUM);
				if (breachAttempts == 0) {
					defense.breach();
					breachAttempts++; //Breach only once
				}
				else if (breachAttempts == 1) {
					state = DONE;
				}
				if (accel.isFlat()) {
					shooter.raise(shooter.MEDIUM);
					state++;
				}
				  
				break;
			}
			case CENTER: {//Center with goal
				if (aim == AimEnum.NONE) {
					state = DONE;
				}
				else {
					drive.degreeRotate(-gyro.getAngle(), TURN_SPEED); // Assume gyro has been reset to zero before breaching
					Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
					camera.getNTInfo();
					//If the distance from goal is unrealistic, then abort
					if (camera.getHorizontalDist() > MAX_DISTANCE_TO_GOAL_FEET || camera.getHorizontalDist() < MIN_DISTANCE_TO_GOAL_FEET){
						state = DONE;
					}
					else {
						//Assume we are looking at the correct goal
						angleToTurn = Math.asin(Math.sin((camera.getTurnAngle() * camera.getHorizontalDist()) / DISTANCE_TO_HIGH_GOAL_FEET));
						supportAngle = 180 - camera.getTurnAngle() - angleToTurn;
						centeredMoveDistance = (DISTANCE_TO_HIGH_GOAL_FEET * Math.sin(supportAngle)) / Math.sin(camera.getTurnAngle());
						// If distance to center is not unrealistic, continue
						if (centeredMoveDistance > MIN_CENTER_DISTANCE_FEET && centeredMoveDistance < MAX_CENTER_DISTANCE_FEET) {
							state++;
						}
						else {
							state = DONE;
						}
					}
				}
				break;
			}
			case MOVE: {//Move so that distance from goal is 9 feet
				drive.moveDistance(centeredMoveDistance);
				drive.waitMove();
				drive.encoderAngleRotate(angleToTurn);//TODO put in the most accurate turning method
				drive.waitMove();
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
			case ROTATE: {//Rotate again using camera
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
			case SHOOT: {//Shoot into high or low goal
				if (aim == AimEnum.HIGH) {
					robot.shootProcedure();
					//shooter.shootProcedure();
				}
				else if (aim == AimEnum.LOW) {
					drive.moveDistance(DISTANCE_TO_GET_TO_LOW_GOAL_FEET);
					drive.waitMove();
					shooter.raise(shooter.DOWN);
					shooter.shoot();
				}
				state++;
				break;
			}
			case DONE: {
				return;
					
			}
		}
	}
}