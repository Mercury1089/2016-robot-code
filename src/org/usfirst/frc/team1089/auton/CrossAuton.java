package org.usfirst.frc.team1089.auton;

public class CrossAuton extends Auton {
	void move() {
		int forwardTime = 2001; // change as needed
		switch (state) {
		case (MOVE):
			if (System.currentTimeMillis() - startTime < forwardTime) {
				drive.drive(0.7, 0.7);
			} else {
				drive.drive(0, 0);
			}
		}
	}
}
