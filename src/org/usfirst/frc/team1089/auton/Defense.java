package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Shooter;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private Shooter shooter;
	
	private static final int 
					BEFORE_DEFENSE_FEET = 4, THROUGH_DEFENSE_FEET = 4,
					INITIAL_CDF_FEET = 4, REMAINING_CDF_FEET = 4,
					AFTER_DEFENSE_FEET = 3, THROUGH_DEFENSE_FEET_ROCK_WALL = 7;  // for rockwall we add 3 feet to allow for huge slippage
	
	public Defense(DriveTrain d, Shooter s, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
		shooter  = s;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR: {
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove(); 		//moveDistance is an asynchronous operation - we need to wait until it is done
				break;
			}
			case MOAT: {
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 12.0); //TODO test and change ALL these values
				drive.waitMove();		//full speed
				break;
			}
			case ROUGH_TERRAIN: {
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 8.0); 
				drive.waitMove();		//need fast PID voltage - to be changed
				break;
			}
			case RAMPARTS: {
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 6); 
				drive.waitMove();		//human speed
				break;
			}
			case ROCK_WALL: {
				shooter.raise(Shooter.MEDIUM);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET_ROCK_WALL + AFTER_DEFENSE_FEET, 0.4, 0, 0, 8.0); 
				drive.waitMove();		// full speed
				break;
			}
			case CHEVAL_DE_FRISE: {
				shooter.raise(Shooter.MEDIUM);
				drive.moveDistance(INITIAL_CDF_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove();
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(REMAINING_CDF_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.5); 
				drive.waitMove();
				break;
			}
			case PORTCULLIS: {
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(INITIAL_CDF_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove();
				//OPEN PORTCULLIS DOOR code
				drive.moveDistance(REMAINING_CDF_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.5);	//change constants 
				drive.waitMove();
				break;
			}
			default:
				break;
		}
	}
}
