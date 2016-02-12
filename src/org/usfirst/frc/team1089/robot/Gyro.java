package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.command.PIDSubsystem;

/**
 * The {@code Gyro} class is a {@code PIDSubsystem} containing fields and methods for reading the gyro on the robot.
 */
public class Gyro extends PIDSubsystem {

	/*
	 * If we don't need this, why is it even here? 
	 * 
	 * RobotDrive drive = new RobotDrive(Ports.PWM.LEFT_TALON,
	 * Ports.PWM.RIGHT_TALON);; AnalogGyro gyro = new
	 * AnalogGyro(Ports.Analog.GYRO);
	 */
	
	public Gyro() {
		super(0.1, 0.0, 0.0);
		setAbsoluteTolerance(0.05);
		getPIDController().setContinuous(false);
	}

	protected double returnPIDInput() {
		return 0;
	}

	protected void usePIDOutput(double output) {

	}

	protected void initDefaultCommand() {

	}

}
