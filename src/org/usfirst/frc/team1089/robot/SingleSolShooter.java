package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.Solenoid;

public class SingleSolShooter extends Shooter {
	private Solenoid shooter;
	
	public SingleSolShooter(){
		super();
		shooter = new Solenoid(Ports.CAN.PCM_ID, Ports.PCM.SINGLE_SHOOTER);
	}
	/**
	 * <pre>
	 * public void shoot()
	 * </pre>
	 * Schedules a {@code ShooterReleaseTask} if the shooter is ready to fire.
	 */
	@Override
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
