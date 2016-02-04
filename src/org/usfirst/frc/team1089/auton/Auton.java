package org.usfirst.frc.team1089.auton;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;

public abstract class Auton {
	protected CANTalon rightFront = new CANTalon (1);
	protected CANTalon leftFront = new CANTalon (0);
	protected CANTalon rightBack = new CANTalon (2);
	protected CANTalon leftBack = new CANTalon (3);
	protected Encoder leftEnc = new Encoder (4, 5);
	protected Encoder rightEnc = new Encoder (6, 7);
	
	protected final int MOVE = 0, TURN = 1, SHOOT = 2, MOVE2 = 3, TURN2 = 4;
	protected int state = MOVE;
	protected RobotDrive drive = new RobotDrive(0, 1);

	protected long startTime = System.currentTimeMillis();

	abstract void move();
	
}

// IF ANYONE USES THIS COMPUTER, PLEASE PUSH AND COMMIT TO GIT BEFORE PULLING!!! PLS