package org.usfirst.frc.team1089.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;

public class Scaler {
	private CANTalon raise, lift;
	private DoubleSolenoid deployer ;
	private static final double SCALER_UP_POS = 4.375, SCALER_DOWN_POS = 1.0;
	
	public Scaler(CANTalon lifterMotor, CANTalon raiseMotor) {
		lift = lifterMotor;
		lift.changeControlMode(TalonControlMode.Position);
		lift.enableBrakeMode(true);
		raise = raiseMotor;
		deployer = new DoubleSolenoid(Ports.PCM.LIFT_DEPLOYER_FORWARD, Ports.PCM.LIFT_DEPLOYER_REVERSE);
		
	}
	
	public void raise(boolean up) {
		if (up){
			deployer.set(DoubleSolenoid.Value.kForward);
			raise.set(1.0);
		}
		else{
			deployer.set(DoubleSolenoid.Value.kReverse);
			raise.set(0);
		}
	}
	
	public void lift(boolean up) {
		if (up){
			lift.set(SCALER_UP_POS);
		}
		else{
			lift.set(SCALER_DOWN_POS);
		}
	}
}