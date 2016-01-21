package org.usfirst.frc.team1089.auton;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;

public abstract class Auton {
	
	protected final int MOVE = 0, TURN = 1, SHOOT = 2, MOVE2 = 3, TURN2 = 4;
	protected int state = MOVE;
	private Talon left = new Talon(0);
	private Talon right = new Talon(1);
	protected RobotDrive drive = new RobotDrive(0, 1);

	protected long startTime = System.currentTimeMillis();

	abstract void move();
	
}
