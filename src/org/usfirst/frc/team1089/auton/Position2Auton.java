package org.usfirst.frc.team1089.auton;

public class Position2Auton extends Auton {

	void move() {

		int forwardTime 				= 2000;			//change as needed
		int turnTime 					= 1000;
		
		switch (state) {
	
		case MOVE:
		
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				state = TURN;
			}
			
		case TURN:
			
			if (System.currentTimeMillis() - startTime < turnTime) {
				drive.drive(0.4, -0.4);
			} else {
				startTime = System.currentTimeMillis();
				drive.drive(0, 0);
				state = SHOOT;
			}
			
		case SHOOT:
			// shooting code
		}

	}
}
