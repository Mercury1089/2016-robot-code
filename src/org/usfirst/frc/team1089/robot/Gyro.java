package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.command.PIDSubsystem;

public class Gyro extends PIDSubsystem{

/*
	RobotDrive drive = new RobotDrive(Ports.PWM.LEFT_TALON, Ports.PWM.RIGHT_TALON);;
	AnalogGyro gyro = new AnalogGyro(Ports.Analog.GYRO);
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
