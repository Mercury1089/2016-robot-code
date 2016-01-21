package org.usfirst.frc.team1089.auton;

public class Position3Auton extends Auton
{
	void move() 
	{
		switch(state)
		
		{
			case (MOVE):
				if (System.currentTimeMillis() - startTime <  2000)
				{
					drive.tankDrive(0.7, 0.7);
				}
				else
				{
					drive.tankDrive(0, 0);
					startTime = System.currentTimeMillis();
					state = TURN;
				}
			
			case (TURN):
				if (System.currentTimeMillis() - startTime <  700)
				{
					drive.tankDrive(0.7, -0.7);
				}
				else  
				{
					drive.tankDrive(0, 0);
					state = MOVE2;
				}
			
			case (MOVE2):
				if (System.currentTimeMillis() - startTime <  1000)
				{
					drive.tankDrive(0.7, 0.7);
				}
				else
				{
					drive.tankDrive(0, 0);
					startTime = System.currentTimeMillis();
					state = TURN2;
				}
			
			case (TURN2):
				if (System.currentTimeMillis() - startTime <  700)
				{
					drive.tankDrive(-0.7, 0.7);
				}
				else  
				{
					drive.tankDrive(0, 0);
					state = SHOOT;
				}
			
			case (SHOOT):
				{	
					//Code
				
				}
		}
	}
}
