package org.usfirst.frc.team1089.auton;

public class Position4Auton extends Auton {
	
	void move() {

		int forwardTime 				= 2000;			//change as needed
		int turnTime 					= 1000;			//used in two cases
		
		switch (state) {
		case MOVE:
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				forwardTime = 1500;
				state = TURN;
			}

		case TURN:
			if (System.currentTimeMillis() - startTime < turnTime) {
				drive.drive(0.7, -0.7);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				turnTime = 800;
				state = MOVE2;
			}
		case MOVE2:
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				state = TURN2;
			}
		case TURN2:
			if (System.currentTimeMillis() - startTime < turnTime) {
				drive.drive(-0.7, 0.7);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				state = SHOOT;
			}
		case SHOOT:
			// enter shoot here
		}
	}
}