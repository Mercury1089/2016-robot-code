package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * The {@code Shooter} class contains fields and methods for operating the
 * shooter on the robot.
 */
public abstract class Shooter {
	private DoubleSolenoid highElevator, lowElevator;
	
	public static final int DOWN = 0, LOW = 1, MEDIUM = 2, HIGH = 3;
	protected static final long SHOOTER_RELEASE_DELAY_MS = 500;
	public static final double RAISE_SHOOTER_CATCHUP_DELAY_SECS = .500;
	
	private int position = HIGH;
	
	/**
	 * <pre>
	 * public Shooter()
	 * </pre>
	 * Constructs a new {@code Shooter} with a {@code Solenoid} controlling the
	 * kicker piston and a {@code DoubleSolenoid} for the elevator.
	 */
	public Shooter() {
		highElevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_HIGH_FORWARD,
				Ports.PCM.SHOOTER_ELEVATOR_HIGH_REVERSE);
		lowElevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_LOW_FORWARD,
				Ports.PCM.SHOOTER_ELEVATOR_LOW_REVERSE);
	}

	/**
	 * <pre>
	 * public void raise(int pos)
	 * </pre>
	 * Raises the elevator to specified position.
	 * @param pos the position to set the elevator
	 */
	public void raise(int pos) {
		this.position = pos;
		switch(pos) {
			case DOWN: {			
				lowElevator.set(DoubleSolenoid.Value.kReverse);
				highElevator.set(DoubleSolenoid.Value.kReverse);
				break;
			}
			case LOW: {				//pancake
				lowElevator.set(DoubleSolenoid.Value.kForward);
				highElevator.set(DoubleSolenoid.Value.kReverse);
				break;
			}
			case MEDIUM: {			//shooting
				lowElevator.set(DoubleSolenoid.Value.kReverse);
				highElevator.set(DoubleSolenoid.Value.kForward);
				break;
			}
			case HIGH: {
				highElevator.set(DoubleSolenoid.Value.kForward);
				lowElevator.set(DoubleSolenoid.Value.kForward);
				break;
			}
		}
	}
	
	/**
	 * <pre>
	 * public int getPosition()
	 * </pre>
	 * Gets the current position of the elevator
	 * @return the index of the current position
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * <pre>
	 * public void raiseShootingHeight(Camera cam) 
	 * </pre>
	 * Raises the shooting height depending on distance from the target.
	 * @param cam the {@code Camera} to use for targeting
	 */
	public void raiseShootingHeight(Camera cam) {
		if (cam.isInFarDistance()) {
			raise(MEDIUM);
		} else if (cam.isInCloseDistance()){
			raise(HIGH);
		}
		// else we don't do anything
	}
	/**
	 * <pre>
	 * public void shoot()
	 * </pre>
	 * Schedules a {@code ShooterReleaseTask} if the shooter is ready to fire.
	 */
	public abstract void shoot(); 

	/**
	 * <pre>
	 * public boolean isElevatorUp()
	 * </pre>
	 * Gets whether or not the elevator is going up
	 * @return true if the {@code DoubleSolenoid's} value is at kForward, false otherwise 
	 */
	public boolean isElevatorUp() {
		return highElevator.get() == DoubleSolenoid.Value.kForward;
	}

	
}
