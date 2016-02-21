package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.MercAccelerometer;
import org.usfirst.frc.team1089.robot.Shooter;

import edu.wpi.first.wpilibj.AnalogGyro;

/**
 * The {@code StrongholdAuton} class contains fields and methods for crossing a defense
 * and shooting in the high/low goal during auton
 */

public class StrongholdAuton {
	private static final int BREACH = 0, CENTER = 1, MOVE = 2, SHOOT = 3, DONE = 4;
	private static final double TURN_SPEED = 0.5, DISTANCE_TO_LOW_GOAL = 7.0;
	private Defense defense;
	private Camera camera;
	private int pos, state = 0, breachAttempts = 0;
	private double centeredMoveDistance, angleToTurn, supportAngle;
	private AimEnum aim;
	private MercAccelerometer accel;
	private Shooter shooter;
	private AnalogGyro gyro;
	protected DriveTrain drive;

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
	 * @param a
	 *            the {@code AimEnum} used for position of the shooter based on which goal we are shooting in
	 * @param dE
	 * 			  the {@code DefenseEnum} says which defense we will be crossing
	 * @param ac
	 * 			  the {@code MercAccelerometer} used to see if we have breached, with the z-axis
	 *  
	 */
	
	public StrongholdAuton(DriveTrain d, Camera c, Shooter s, AnalogGyro g, int p,
							AimEnum a, DefenseEnum dE, MercAccelerometer ac) {
		drive = d;
		camera = c;
		pos = p;
		defense = new Defense(drive, shooter, dE);
		aim = a ;
		shooter = s;
		gyro = g;
		accel = ac;
	}

	/**
	 * <pre>
	 * public void move()
	 * 
	 * Crosses defense, centers, moves 10 feet away from goal, and shoots in auton
	 * 
	 */
	public void move() {
		switch (state) {
			case BREACH: {//Breaching Phase
				if (breachAttempts == 0) {
					defense.breach();
					breachAttempts++;
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
					drive.degreeRotate(-gyro.getAngle(), TURN_SPEED);
					camera.getNTInfo();
					angleToTurn = Math.asin(Math.sin((camera.getTurnAngle() * camera.getHorizontalDist()) / 10.0));
					supportAngle = 180 - camera.getTurnAngle() - angleToTurn;
					centeredMoveDistance = (10.0 * Math.sin(supportAngle)) / Math.sin(camera.getTurnAngle());
					if (centeredMoveDistance > 0) {
						state++;
					}
					else {
						state = DONE;
					}
				}
				break;
			}
			case MOVE: {//Move so that distance from goal is 10 feet
				drive.moveDistance(centeredMoveDistance);
				drive.waitMove();
				drive.encoderAngleRotate(angleToTurn);
				drive.waitMove();
				if (camera.isInDistance() && camera.isInLineWithGoal() && camera.isInTurnAngle()) {
					state++;
				}
				else {
					state = DONE;
				}
				break;
			}
			case SHOOT: {//Shoot into high or low goal
				if (aim == AimEnum.HIGH) {
					//shooter.shootProcedure();
				}
				else if (aim == AimEnum.LOW) {
					drive.moveDistance(DISTANCE_TO_LOW_GOAL);
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