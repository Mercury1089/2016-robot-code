package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private Shooter shooter;
	
	private final int MOVE_DISTANCE_FEET = 7, 
					  APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET = 3, REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET = 4;
	
	public Defense(DriveTrain d, Shooter s, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
		shooter  = s;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR: {
				shooter.raise(shooter.DOWN);
			}
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL: {
				drive.moveDistance(MOVE_DISTANCE_FEET);
				drive.waitMove(); // moveDistance is an asynchronous operation - we need to wait until it is done
				break;
			}
			case CHEVAL_DE_FRISE: {
				drive.moveDistance(APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET);
				drive.waitMove();
				shooter.raise(shooter.DOWN);
				drive.moveDistance(REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET);
				drive.waitMove();
			}
			default:
				break;
		}
	}
}
