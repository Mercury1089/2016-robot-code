package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class DoubleSolShooter extends Shooter {

private DoubleSolenoid shooter;
	
	public DoubleSolShooter(){
		super();
		shooter = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.DOUBLE_SHOOTER_FORWARD, Ports.PCM.DOUBLE_SHOOTER_REVERSE);
	}
	/**
	 * <pre>
	 * public void shoot()
	 * </pre>
	 * Schedules a {@code ShooterReleaseTask} if the shooter is ready to fire.
	 */
	@Override
	public void shoot() {
		if (shooter.get() == DoubleSolenoid.Value.kForward) {
			return;
		}
		shooter.set(DoubleSolenoid.Value.kForward);
		Timer timer = new Timer();
		timer.schedule(new ShooterReleaseTask(shooter), SHOOTER_RELEASE_DELAY_MS);
		//Logger.debug(LoggerType.SHOOTING_DATA, "SHOT!");
	}
	
	/**
	 * The {@code ShooterReleaseTask} is a {@code TimerTask} used to trigger the release of the shooter piston.
	 */
	private class ShooterReleaseTask extends TimerTask {
		DoubleSolenoid _shooter;

		/**
		 * <pre>
		 * public ShooterReleaseTask(Solenoid shooter)
		 * </pre>
		 * Constructs a new {@code ShooterReleaseTask} with the specified {@code Solenoid} to be used
		 * to control the shooter piston.
		 * @param shooter {@code Solenoid} used to control the shooter piston
		 */
		public ShooterReleaseTask(DoubleSolenoid shooter) {
			this._shooter = shooter;
		}

		@Override
		public void run() {
			_shooter.set(DoubleSolenoid.Value.kReverse);
		}
	}	

}


