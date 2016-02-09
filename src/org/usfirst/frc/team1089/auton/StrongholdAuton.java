package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;

public class StrongholdAuton{
	private static final int CENTERED_MOVE_DISTANCE = 3; 
	private Defense defense;
	private Camera camera;
	private int pos, aim;
	protected DriveTrain drive;
	
	public StrongholdAuton(DriveTrain d, Camera c, int p, int a, DefenseEnum dE) {
		drive = d;
		camera = c;
		pos = p;
		defense = new Defense(drive, dE);
		aim = a;
	}
	
	public void move() {
		defense.breach();
		if(aim == 2){
			if(camera.getRectArea().length < 0 ){
				if(pos <= 3)
					drive.degreeRotate(45, 0.5);
				else
					drive.degreeRotate(-45, 0.5);	
			}
			drive.degreeRotate(camera.getTurnAngle(), 0.5);
			drive.moveDistance(CENTERED_MOVE_DISTANCE);
			drive.waitMove(); // moveDistance is an asynchronous operation - we need to wait until it is done
			//shooter.shoot();
		}
		/*else if(aim == 3){
		 	switch(pos){
		 		case 1:{
		 			drive.turnDistance(POSITION1_TURN_DISTANCE);
		 			drive.waitMove();
		 			break;
		 		}
		 		case 2:{
		 			drive.turnDistance(POSITION2_TURN_DISTANCE);
		 			drive.waitMove();
		 			break;
		 		}
		 		case 3:{
		 			drive.turnDistance(POSITION3_TURN_DISTANCE);
		 			drive.waitMove();
		 			break;
		 		}
		 		case 4:{
		 			drive.turnDistance(POSITION4_TURN_DISTANCE);
		 			drive.waitMove();
		 			break;
		 		}
		 		case 5:{
		 			drive.turnDistance(POSITION5_TURN_DISTANCE);
		 			drive.waitMove();
		 			break;
		 		}	
		 	}
		 	drive.MoveDistance(DISTANCE_TO_ROTATE_SPOT); //move to spot that we need to go to before we turn to face low goal
		 	drive.waitMove();
			drive.turnDistance(changePos); //turn to face low goal
			drive.waitMove();
			drive.moveDistance(changePos); //drive into low goal area
			drive.waitMove();
			shooter.shootLow();
		}*/
	}
}