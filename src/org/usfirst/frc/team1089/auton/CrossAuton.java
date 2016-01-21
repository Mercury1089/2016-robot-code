package org.usfirst.frc.team1089.auton;

public class CrossAuton extends Auton
{
	void move() {
	
	switch(state)
	{
	case (MOVE):
		if (System.currentTimeMillis() - startTime < 2001)
		{
			drive.tankDrive(0.7, 0.7);
		}
		else
		{
			drive.tankDrive(0, 0);
		}
	}
	}
}
