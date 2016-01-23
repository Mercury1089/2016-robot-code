package org.usfirst.frc.team1089.auton;

public class Position3Auton extends Auton {
	void move() {
		int forwardTime 				= 2000;			//change as needed
		int turnTime 				    = 700;			//used in 2 places
		switch (state)

		{
		case (MOVE):
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				drive.drive(0, 0);
				startTime = System.currentTimeMillis();
				forwardTime = 800;
				state = TURN;
			}

		case (TURN):
			if (System.currentTimeMillis() - startTime < turnTime) {
				drive.drive(0.7, -0.7);
			} else {
				drive.drive(0, 0);
				state = MOVE2;
			}

		case (MOVE2):
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				drive.drive(0, 0);
				startTime = System.currentTimeMillis();
				state = TURN2;
			}

		case (TURN2):
			if (System.currentTimeMillis() - startTime < turnTime) {
				drive.drive(-0.7, 0.7);
			} else {
				drive.drive(0, 0);
				state = SHOOT;
			}

		case (SHOOT): {
			// Code

		}
		}
	}
}
