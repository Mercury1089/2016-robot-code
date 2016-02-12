package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

public class StrongholdAuton {
	private static final int CENTERED_MOVE_DISTANCE_FEET = 3;
	private static final double TURN_SPEED = 0.5;
	private Defense defense;
	private Camera camera;
	private int pos;
	private AimEnum aim;
	private Shooter shooter;
	protected DriveTrain drive;

	public StrongholdAuton(DriveTrain d, Camera c, Shooter s, int p, AimEnum a, DefenseEnum dE) {
		drive = d;
		camera = c;
		pos = p;
		defense = new Defense(drive, dE);
		aim = a ;
		shooter = s;
	}

	public void move() {
		defense.breach(); // first we breach the defense - TODO what if the defense before us is one the of defenses we do not know how to breach?
		
		if (aim == AimEnum.HIGH) { // if we are going for the high goal
			camera.getNTInfo(); // we take a look
			
			if (camera.getRectArea().length < 0) { // if we don't see a target we need to find it
				if (pos <= 3) {
					drive.degreeRotate(45, TURN_SPEED); // TODO could there be a better approach?
				} else {
					drive.degreeRotate(-45, TURN_SPEED);
				}
			}
			
			camera.getNTInfo(); // we take another look
			// TODO what if we still don't see a target?
			
			drive.degreeRotate(camera.getTurnAngle(), TURN_SPEED);
			
			drive.moveDistance(CENTERED_MOVE_DISTANCE_FEET);
			drive.waitMove(); // moveDistance is an asynchronous operation - we
								// need to wait until it is done
			
			camera.getNTInfo(); // we take a final look
			
			if (camera.isInDistance() && camera.isInLineWithGoal() && camera.isInTurnAngle()) { // only if we are clear
				shooter.shoot();
			} else {
				// we do nothing - the pilot will take over from here
			}
		}
		/*
		 * else if(aim == AimEnum.LOW){ switch(pos){ case 1:{
		 * drive.turnDistance(POSITION1_TURN_DISTANCE); drive.waitMove(); break;
		 * } case 2:{ drive.turnDistance(POSITION2_TURN_DISTANCE);
		 * drive.waitMove(); break; } case 3:{
		 * drive.turnDistance(POSITION3_TURN_DISTANCE); drive.waitMove(); break;
		 * } case 4:{ drive.turnDistance(POSITION4_TURN_DISTANCE);
		 * drive.waitMove(); break; } case 5:{
		 * drive.turnDistance(POSITION5_TURN_DISTANCE); drive.waitMove(); break;
		 * } } drive.MoveDistance(DISTANCE_TO_ROTATE_SPOT); //move to spot that
		 * we need to go to before we turn to face low goal drive.waitMove();
		 * drive.turnDistance(changePos); //turn to face low goal
		 * drive.waitMove(); drive.moveDistance(changePos); //drive into low
		 * goal area drive.waitMove(); shooter.raise(true);
			intake.moveBall(-1); }
		 */
	}
}