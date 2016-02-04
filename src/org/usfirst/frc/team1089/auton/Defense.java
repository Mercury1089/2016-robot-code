package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.MercEncoder;

public class Defense extends StrongholdAuton{
	private double endPosL, endPosR;
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
				endPosL = drive.lft.getEncPosition() + MercEncoder.convertDistanceToEncoderTicks(1, 1.0);
				endPosR = drive.rft.getEncPosition() + MercEncoder.convertDistanceToEncoderTicks(1, -1.0);
				drive.moveDistance(endPosL, endPosR);
				break;
			}
			default:
				break;
		}
	}
}
