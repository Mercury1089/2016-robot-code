package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;

public class StrongholdAuton{
	private final int CENTERED_MOVE_DISTANCE = 3; 
	private Defense defense;
	private DefenseEnum defenseEnum;
	private Camera camera;
	private int pos;
	protected DriveTrain drive;
	
	public StrongholdAuton(DriveTrain d, DefenseEnum dE, Camera c, int p) {
		drive = d;
		defenseEnum = dE;
		camera = c;
		pos = p;
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
