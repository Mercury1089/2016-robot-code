package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;

public class Defense extends StrongholdAuton{
	private DefenseEnum defenseEnum;
	
	public Defense(DriveTrain d, DefenseEnum dE) {
		super(d, dE);
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR:
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL:
			{
				drive.moveDistance(1);
				break;
			}
			default:
				break;
		}
	}
}
