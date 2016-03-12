package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.DoubleSolenoid;

/**
 * The {@code Intake} class contains fields and methods for operating the ball intake on the robot.
 */
public class Intake {
	private CANTalon intake;				//controls wheels
	private DoubleSolenoid elevator;		//wheel elevator - to hold ball in place?
	
	/**
	 * <pre>
	 * public Intake()
	 * </pre>
	 * Constructs a new {@code Intake} with a specified {@code CANTalon} for operating the wheels 
	 * and a {@code DoubleSolenoid} that operates the elevator. 
	 */
	public Intake(CANTalon intakeMotor) {
		intake = intakeMotor;
		elevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.INTAKE_ELEVATOR_FORWARD, Ports.PCM.INTAKE_ELEVATOR_REVERSE);
		intake.changeControlMode(TalonControlMode.PercentVbus);
		intake.enableBrakeMode(true);
	}

	/**
	 * <pre>
	 * public void moveBall(int speed)
	 * </pre>
	 * Sets the speed of the intake to move the ball.
	 * @param speed speed to set the intake wheels
	 */
	public void moveBall(double speed) {	
		intake.set(speed);	
	}
	
	/**
	 * <pre>
	 * public boolean isOn()
	 * </pre>
	 * Gets whether or not the {@code CANTalon} for the wheels of the intake are enabled.
	 * @return true if the {@code CANTalon} is enabled, false if the {@code CANTalon} is disabled 
	 */
	public boolean isOn() {						
		return intake.isEnabled();
	}
	
	/**
	 * <pre>
	 * public void lower(boolean readyToShoot)
	 * </pre>
	 * Raises or lowers the elevator depending on whether or not the driver is ready to shoot.
	 * @param readyToShoot whether or not the driver is ready to shoot
	 */
	public void lower(boolean readyToShoot) {	//have to check values
		if (readyToShoot)
			elevator.set(DoubleSolenoid.Value.kForward);
		else
			elevator.set(DoubleSolenoid.Value.kReverse);
	}
}
