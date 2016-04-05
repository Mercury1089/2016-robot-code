package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;

public class Scaler {
	private CANTalon lifter;
	private CANTalon deployer;
	private AnalogPotentiometer scaler;
	private static final double SCALER_DEPLOY_TIME = .25, LIFT_TIME = 3.0;
	public Scaler(CANTalon lifterMotor, CANTalon deployerMotor) {
		lifter = lifterMotor;
		deployer = deployerMotor;
		lifter.changeControlMode(TalonControlMode.PercentVbus);
		lifter.enableBrakeMode(true);
		deployer.changeControlMode(TalonControlMode.PercentVbus);
		deployer.enableBrakeMode(true);
	}
	
	public void deploy() {
		deployer.set(0.5);
		Timer.delay(SCALER_DEPLOY_TIME);
		deployer.set(0);
	}
	
	public void lift() {
		lifter.set(1.0);
		Timer.delay(LIFT_TIME);
		deployer.set(-0.5);
		Timer.delay(SCALER_DEPLOY_TIME);
		deployer.set(0);
		lifter.set(0);
	}
}
