package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private Shooter shooter;
	
	private static final int MOVE_DISTANCE_FEET = 7, 
					  APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET = 3, REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET = 4;
	
	public Defense(DriveTrain d, Shooter s, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
		shooter  = s;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR: {
				shooter.raise(Shooter.DOWN);
			}
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL: {
				drive.moveDistanceAuton(MOVE_DISTANCE_FEET, 0.4, 0.0005, -0.001); //TODO test and change these values
				drive.waitMove(); // moveDistance is an asynchronous operation - we need to wait until it is done
				break;
			}
			case CHEVAL_DE_FRISE: {
				drive.moveDistance(APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET);
				drive.waitMove();
				shooter.raise(Shooter.DOWN);
				drive.moveDistanceAuton(REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET, 0.4, 0.0005, -0.001); //TODO test and change these values
				drive.waitMove();
			}
			default:
				break;
		}
	}
}
