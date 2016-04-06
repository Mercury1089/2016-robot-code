package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;

public class Scaler {
	private CANTalon lifter;
	private AnalogPotentiometer scalerPot;
	private DoubleSolenoid deployer ;
	private static final double SCALER_UP_POS = 4.375, SCALER_DOWN_POS = 1.0;
	
	public Scaler(CANTalon lifterMotor, CANTalon deployerMotor) {
		lifter = lifterMotor;
		lifter.changeControlMode(TalonControlMode.Position);
		lifter.enableBrakeMode(true);
		scalerPot = new AnalogPotentiometer(Ports.Analog.SCALER_POT);
		deployer = new DoubleSolenoid(Ports.PCM.LIFT_DEPLOYER_FORWARD, Ports.PCM.LIFT_DEPLOYER_REVERSE);
		
	}
	
	public void raise(boolean up) {
		if (up){
			deployer.set(DoubleSolenoid.Value.kForward);
		}
		else{
			deployer.set(DoubleSolenoid.Value.kReverse);
		}
	}
	
	public void lift(boolean up) {
		if (up){
			lifter.set(SCALER_UP_POS);
		}
		else{
			lifter.set(SCALER_DOWN_POS);
		}
	}
}
