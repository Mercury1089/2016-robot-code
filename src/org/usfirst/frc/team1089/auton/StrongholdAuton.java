package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;

public class StrongholdAuton{
	private static final int CENTERED_MOVE_DISTANCE = 3; 
	private Defense defense;
	private Camera camera;
	private int pos;
	protected DriveTrain drive;
	
	public StrongholdAuton(DriveTrain d, Camera c, int p) {
		drive = d;
		camera = c;
		pos = p;
		defense = new Defense(drive);
	}
	
	public void move() {
		defense.breach();
		if(camera.getRectArea().length < 0 ){
			if(pos <= 3)
				drive.degreeRotate(45, 0.5);
			else
				drive.degreeRotate(-45, 0.5);	
		}
		drive.degreeRotate(camera.getTurnAngle(), 0.5);
		drive.moveDistance(CENTERED_MOVE_DISTANCE);
		//shooter.shoot();
	}
}