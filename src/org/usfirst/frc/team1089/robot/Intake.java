package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Intake {
	private CANTalon intake;		//controls wheels
	private DoubleSolenoid elevator;		//wheel elevator - to hold ball in place?
	
	public Intake() {
		intake = new CANTalon(Ports.CAN.INTAKE_TALON_ID);
		elevator = new DoubleSolenoid(Ports.PCM.INTAKE_ELEVATOR_FORWARD, Ports.PCM.INTAKE_ELEVATOR_REVERSE);
	}

	/**
	 * pushes, pulls, or stops ball
	 * @param speed - value to set intake
	 */
	public void moveBall(int speed) {	
		intake.set(speed);	
	}
	
	/**
	 * @return true if wheels are on, false otherwise
	 */
	public boolean isOn() {						
		return intake.isEnabled();
	}
	
	/**
	 * raises or lowers elevator
	 */
	public void raise(boolean readyToShoot) {	//have to check values
		if (readyToShoot)
			elevator.set(DoubleSolenoid.Value.kForward);
		else
			elevator.set(DoubleSolenoid.Value.kReverse);
	}
}
