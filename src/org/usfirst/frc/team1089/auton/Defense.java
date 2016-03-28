package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;
import org.usfirst.frc.team1089.robot.Logger;
import org.usfirst.frc.team1089.robot.Shooter;

public class Defense{
	private DefenseEnum defenseEnum;
	private DriveTrain drive;
	private Shooter shooter;
	
	public static final int 
	
					BEFORE_DEFENSE_FEET = 3, // the robot is 38-inch long with the bumper. With 2 inches above the line we are 3 feet away from defense.
					THROUGH_DEFENSE_FEET = 4, // defenses are 4 feet by definition
					INITIAL_CDF_FEET = 3, // cheval de frise
					REMAINING_CDF_FEET = 4, // cheval de frise
					INITIAL_PORTCULLIS_FEET = 3, // Portcullis = same as CDF
					REMAINING_PORTCULLIS_FEET = 4, // Portcullis = same as CDF
					CORE_AFTER_DEFENSE = 3, // what we need to clear the defense (so the back of the robot goes back the defense)
					BUFFER_AFTER_DEFENSE = 2, // a buffer to avoid not fully clearing the defense
					AFTER_DEFENSE_FEET = CORE_AFTER_DEFENSE + BUFFER_AFTER_DEFENSE, // this corresponds to 3 feet to clear plus 2 feet of buffer
					ROCK_WALL_SLIPPAGE_COMPENSATION_FEET = 2, // should be adjusted so that back is about two feet (or rather BUFFER_AFTER_DEFENSE) away from defense after breaching it
					THROUGH_DEFENSE_FEET_ROCK_WALL =  THROUGH_DEFENSE_FEET + ROCK_WALL_SLIPPAGE_COMPENSATION_FEET,  // for rockwall we add 2 feet to allow for huge slippage + buffer 
					ROUGH_TERRAIN_SLIPPAGE_COMPENSATION_FEET = 2, // should be adjusted so that back is about two feet (or rather BUFFER_AFTER_DEFENSE) away from defense after breaching it
					THROUGH_DEFENSE_FEET_ROUGH_TERRAIN =  THROUGH_DEFENSE_FEET + ROUGH_TERRAIN_SLIPPAGE_COMPENSATION_FEET;  // for rough terrain we add 2 feet to allow for huge slippage + buffer
	
	public Defense(DriveTrain d, Shooter s, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
		shooter  = s;
	}
	
	public void breach() {
		switch (defenseEnum) {
			case LOW_BAR: {
				Logger.log("Defense LOW_BAR in");
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove(); 		//moveDistance is an asynchronous operation - we need to wait until it is done
				Logger.log("Defense LOW_BAR out");
				break;
			}
			case MOAT: {
				Logger.log("Defense MOAT in");
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 12.0); //TODO test and change ALL these values
				drive.waitMove();		//full speed
				Logger.log("Defense MOAT out");
				break;
			}
			case ROUGH_TERRAIN: {
				Logger.log("Defense ROUGH_TERRAIN in");
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET_ROUGH_TERRAIN + AFTER_DEFENSE_FEET, 0.4, 0, 0, 8.0); 
				drive.waitMove();		//need fast PID voltage - to be changed
				Logger.log("Defense ROUGH_TERRAIN out");
				break;
			}
			case RAMPARTS: {
				Logger.log("Defense RAMPARTS in");
				shooter.raise(Shooter.LOW);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 6); 
				drive.waitMove();		//human speed
				Logger.log("Defense RAMPARTS out");
				break;
			}
			case ROCK_WALL: {
				Logger.log("Defense ROCK_WALL in");
				shooter.raise(Shooter.MEDIUM);
				drive.moveDistance(BEFORE_DEFENSE_FEET + THROUGH_DEFENSE_FEET_ROCK_WALL + AFTER_DEFENSE_FEET, 0.4, 0, 0, 8.0); 
				drive.waitMove();		// full speed
				Logger.log("Defense ROCK_WALL out");
				break;
			}
			case CHEVAL_DE_FRISE: {
				Logger.log("Defense CHEVAL_DE_FRISE in");
				shooter.raise(Shooter.MEDIUM);
				drive.moveDistance(INITIAL_CDF_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove();
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(REMAINING_CDF_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.5); 
				drive.waitMove();
				Logger.log("Defense CHEVAL_DE_FRISE out");
				break;
			}
			case PORTCULLIS: {
				Logger.log("Defense PORTCULLIS in");
				shooter.raise(Shooter.DOWN);
				drive.moveDistance(INITIAL_PORTCULLIS_FEET, 0.4, 0, 0, 4.5);
				drive.waitMove();
				shooter.raise(Shooter.HIGH);
				drive.moveDistance(REMAINING_PORTCULLIS_FEET + AFTER_DEFENSE_FEET, 0.4, 0, 0, 4.7);	//values have to be tested 
				drive.waitMove();
				Logger.log("Defense PORTCULLIS out");
				break;
			}
			default:
				break;
		}
	}
}
