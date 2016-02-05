package org.usfirst.frc.team1089.auton;

import org.usfirst.frc.team1089.robot.DriveTrain;

public class StrongholdAuton{
	
	private Defense defense;
	private DefenseEnum defenseEnum;
	protected DriveTrain drive;
	
	public StrongholdAuton(DriveTrain d, DefenseEnum dE) {
		drive = d;
		defenseEnum = dE;
	}
	
	public void move() {
		
		defense.breach();
		//shooter.shoot();
	}
}
