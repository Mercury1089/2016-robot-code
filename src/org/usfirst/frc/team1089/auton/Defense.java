package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private Shooter shooter;
	
	private static final int MOVE_DEFENSE_DISTANCE_FEET = 7,
					  APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET = 3, REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET = 4,
					  CLEAR_DEFENSE_FEET =3 ;
	
	public Defense(DriveTrain d, Shooter s, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
		shooter  = s;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR: {
				shooter.raise(Shooter.DOWN);
				// then same as other defenses, so no break
			}
			case MOAT:
			case ROUGH_TERRAIN:
			case RAMPARTS:
			case ROCK_WALL: {
				drive.moveDistanceAuton(MOVE_DEFENSE_DISTANCE_FEET + CLEAR_DEFENSE_FEET, 0.4, 0, 0, 4.5); //TODO test and change these values
				drive.waitMove(); // moveDistance is an asynchronous operation - we need to wait until it is done
				break;
			}
			case CHEVAL_DE_FRISE: {
				drive.moveDistance(APPROACH_CHEVAL_DE_FRISE_DISTANCE_FEET);
				drive.waitMove();
				shooter.raise(Shooter.DOWN);
				drive.moveDistanceAuton(REMAINING_CHEVAL_DE_FRISE_DISTANCE_FEET + CLEAR_DEFENSE_FEET, 0.4, 0, 0, 4.5); //TODO test and change these values
				drive.waitMove();
				break;
			}
			default:
				break;
		}
	}
}
