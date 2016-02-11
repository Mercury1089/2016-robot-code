package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Intake {
	private CANTalon intake;		//controls wheels
	private DoubleSolenoid elevator;		//wheel elevator - to hold ball in place?
	
	public Intake(CANTalon i, DoubleSolenoid ds) {
		intake = i;
		elevator = ds;
	}

	public void pull() {
		intake.set(1);
	}
	
	public void push() {
		intake.set(-1);	
	}
	
	public void stop() {
		intake.set(0);
	}
	
	public void raise(boolean readyToShoot) {	//have to check values, account for deadzone, make sure there is a limit
		if (readyToShoot)
			elevator.set(DoubleSolenoid.Value.kForward);
		else
			elevator.set(DoubleSolenoid.Value.kReverse);
	}
}
