package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;

public class Scaler {
	private CANTalon lifter;
	
	public Scaler(CANTalon lifterMotor) {
		lifter = lifterMotor;
		lifter.changeControlMode(TalonControlMode.PercentVbus);
		lifter.enableBrakeMode(true);
	}
	
	
}
