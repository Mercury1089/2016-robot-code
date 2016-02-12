package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	
	private final int MOVE_DISTANCE_FEET = 7;
	
	public Defense(DriveTrain d, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR:
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL:
			{
				drive.moveDistance(MOVE_DISTANCE_FEET);
				drive.waitMove(); // moveDistance is an asynchronous operation - we need to wait until it is done
				break;
			}
			default:
				break;
		}
	}
}
