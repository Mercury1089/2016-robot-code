package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * The {@code Shooter} class contains fields and methods for operating the
 * shooter on the robot.
 */
public class Shooter {
	private Solenoid shooter;
	private DoubleSolenoid elevator;
	private static final long SHOOTER_RELEASE_DELAY_MS = 250;

	/**
	 * <pre>
	 * public Shooter()
	 * </pre>
	 * Constructs a new {@code Shooter} with a {@code Solenoid} controlling the
	 * kicker piston and a {@code DoubleSolenoid} for the elevator.
	 */
	public Shooter() {
		shooter = new Solenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER);
		elevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_FORWARD,
				Ports.PCM.SHOOTER_ELEVATOR_REVERSE);
	}

	/**
	 * <pre>
	 * public void raise(boolean level)
	 * </pre>
	 * 
	 * Raises the elevator depending on whether or not the elevator is
	 * considered level.
	 * 
	 * @param level whether or not the elevator is considered level.
	 */
	public void raise(boolean level) {
		if (level) {
			elevator.set(DoubleSolenoid.Value.kForward);
		} else { 
			elevator.set(DoubleSolenoid.Value.kReverse);
		}
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
	}

	/**
	 * <pre>
	 * public boolean isElevatorUp()
	 * </pre>
	 * Gets whether or not the elevator is going up
	 * @return true if the {@code DoubleSolenoid's} value is at kForward, false otherwise 
	 */
	public boolean isElevatorUp() {
		return elevator.get() == DoubleSolenoid.Value.kForward;
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
