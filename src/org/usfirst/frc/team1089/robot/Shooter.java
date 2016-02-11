package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

public class Shooter {
	private Solenoid shooter;
	private DoubleSolenoid elevator;
	private static final long SHOOTER_RELEASE_DELAY_MS = 500;

	public Shooter() {
		shooter = new Solenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER);
		elevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.ELEVATOR_FORWARD, Ports.PCM.ELEVATOR_REVERSE);
	}

	public void raise(int level) {
		if (level == 1) {
			elevator.set(DoubleSolenoid.Value.kForward);
		}

		if (level == -1) {
			elevator.set(DoubleSolenoid.Value.kReverse);
		}
	}

	public void shoot() {
		if (shooter.get()) {
			return;
		}
		shooter.set(true);
		Timer timer = new Timer();
		timer.schedule(new ShooterReleaseTask(shooter), SHOOTER_RELEASE_DELAY_MS);
	}

	class ShooterReleaseTask extends TimerTask
	{
		Solenoid _shooter;
		public ShooterReleaseTask(Solenoid shooter) {
			this._shooter = shooter;
		}

		@Override
		public void run() {
			_shooter.set(false);
		}
		
	}
}
