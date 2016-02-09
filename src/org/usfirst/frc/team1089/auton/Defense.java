package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.Camera;
import org.usfirst.frc.team1089.robot.DriveTrain;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private final int MOVE_DISTANCE = 7;
	
	public Defense(DriveTrain d) {
		drive = d;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR:
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL:
			{
				drive.moveDistance(MOVE_DISTANCE);
				break;
			}
			default:
				break;
		}
	}
}
