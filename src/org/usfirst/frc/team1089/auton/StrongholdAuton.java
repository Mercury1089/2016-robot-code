package org.usfirst.frc.team1089.auton;

public class StrongholdAuton extends Auton {
	private int position;
	private Defense defense;
	public StrongholdAuton(int position, Defense defense) {
		this.position = position;
		this.defense = defense;
	}
	@Override
	void move() {
		// moveForward(5 feet) // In other words - approach the defense
		defense.breach();
		
	}
}
