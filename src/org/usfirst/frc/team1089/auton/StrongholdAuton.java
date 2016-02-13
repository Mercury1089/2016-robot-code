package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

import edu.wpi.first.wpilibj.AnalogGyro;

public class StrongholdAuton {
	private static final int BREACH = 0, CENTER = 1, MOVE = 2, SHOOT = 3;
	private static final double TURN_SPEED = 0.5, DISTANCE_TO_LOW_GOAL = 10.0;
	private Defense defense;
	private Camera camera;
	private int pos, state = 0;
	private double centeredMoveDistance, angleToTurn, supportAngle;
	private AimEnum aim;
	private DefenseEnum defenseEnum;
	private Shooter shooter;
	private AnalogGyro gyro;
	protected DriveTrain drive;

	public StrongholdAuton(DriveTrain d, Camera c, Shooter s, AnalogGyro g, int p, AimEnum a, DefenseEnum dE) {
		drive = d;
		camera = c;
		pos = p;
		defenseEnum = dE;
		defense = new Defense(drive, dE);
		aim = a ;
		shooter = s;
		gyro = g;
	}

	public void move() {
		switch(state){
			
			case BREACH:{//Breaching Phase
				if (defenseEnum == DefenseEnum.LOW_BAR){
					shooter.raise(false);
				}
				defense.breach();
				/*if(gyro z axis is zero)
				 * shooter.raise(true);
				 * state++;
				 */
				break;
			}
			case CENTER:{
				if (aim == AimEnum.NONE){
					return;
				}
				else{
					drive.degreeRotate(-gyro.getAngle(), TURN_SPEED);
					camera.getNTInfo();
					angleToTurn = Math.asin(Math.sin((camera.getTurnAngle() * camera.getHorizontalDist()) / 10.0));
					supportAngle = 180 - camera.getTurnAngle() - angleToTurn;
					centeredMoveDistance = (10.0 * Math.sin(supportAngle)) / Math.sin(camera.getTurnAngle());
					if(centeredMoveDistance > 0){
						state++;
					}
				}
				break;
			}
			case MOVE:{
				drive.moveDistance(centeredMoveDistance);
				drive.waitMove();
				drive.encoderAngleRotate(angleToTurn);
				drive.waitMove();
				if(camera.isInDistance() && camera.isInLineWithGoal() && camera.isInTurnAngle()){
					state++;
				}
				break;
			}
			case SHOOT:{
				if(aim == AimEnum.HIGH){
					shooter.shoot();
				}
				else if(aim == AimEnum.LOW){
					drive.moveDistance(DISTANCE_TO_LOW_GOAL);
					drive.waitMove();
					shooter.raise(false);
					//intake.moveBall(-1);
				}
				break;
			}
		}
	}
}