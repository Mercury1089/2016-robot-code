package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import org.usfirst.frc.team1089.robot.LoggerLegacy.LoggerType;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * The {@code Shooter} class contains fields and methods for operating the
 * shooter on the robot.
 */
public class Shooter {
	private Solenoid shooter;
	private DoubleSolenoid highElevator, lowElevator;
	
	public static final int DOWN = 0, LOW = 1, MEDIUM = 2, HIGH = 3;
	private static final long SHOOTER_RELEASE_DELAY_MS = 500;
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
		shooter = new Solenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER);
		highElevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_HIGH_FORWARD,
				Ports.PCM.SHOOTER_ELEVATOR_HIGH_REVERSE);
		lowElevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_LOW_FORWARD,
				Ports.PCM.SHOOTER_ELEVATOR_LOW_REVERSE);
	}

	/**
	 * <pre>
	 * public void raise(int pos)
	 * </pre>
	 * 
	 * Raises the elevator to specified position.
	 * 
	 * @param pos position
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
	
	public int getPosition() {
		return position;
	}
	
	public void raiseShootingHeight(Camera cam) {
		if (cam.isInFarDistance()) {
			raise(MEDIUM);
		}
		else if (cam.isInCloseDistance()){
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
	public void shoot() {
		if (shooter.get()) {
			return;
		}
		shooter.set(true);
		Timer timer = new Timer();
		timer.schedule(new ShooterReleaseTask(shooter), SHOOTER_RELEASE_DELAY_MS);
		//Logger.debug(LoggerType.SHOOTING_DATA, "SHOT!");
	}

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

	/**
	 * The {@code ShooterReleaseTask} is a {@code TimerTask} used to trigger the release of the shooter piston.
	 */
	private class ShooterReleaseTask extends TimerTask {
		Solenoid _shooter;

		/**
		 * <pre>
		 * public ShooterReleaseTask(Solenoid shooter)
		 * </pre>
		 * Constructs a new {@code ShooterReleaseTask} with the specified {@code Solenoid} to be used
		 * to control the shooter piston.
		 * @param shooter {@code Solenoid} used to control the shooter piston
		 */
		public ShooterReleaseTask(Solenoid shooter) {
			this._shooter = shooter;
		}

		@Override
		public void run() {
			_shooter.set(false);
		}
	}	
}
