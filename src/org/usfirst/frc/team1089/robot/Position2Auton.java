package org.usfirst.frc.team1089.robot;

public class Position2Auton extends Auton
{
	void move()
	{
	
	switch(state)
	{
	case MOVE:
		if (System.currentTimeMillis() - startTime < 2000)
		{
			drive.tankDrive(0.7, 0.7);
		}
		else
		{
			startTime = System.currentTimeMillis();
			drive.tankDrive(0, 0);
			state = TURN;
		}
	
	case TURN:
		if (System.currentTimeMillis() - startTime < 1000)
		{
			drive.tankDrive(0.4, -0.4);
		}
		else
		{
			startTime = System.currentTimeMillis();
			drive.tankDrive(0, 0);
			state = SHOOT;
		}
	case SHOOT:
		//enter shoot here
	}
	}
}
