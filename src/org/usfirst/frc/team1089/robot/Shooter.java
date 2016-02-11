package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

public class Shooter {
	Solenoid shooter;
	DoubleSolenoid elevator;

	public Shooter() {
		shooter = new Solenoid(6, 1);
		elevator = new DoubleSolenoid(7, 2, 3);
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
		timer.schedule(new ShooterReleaseTask(shooter), 500);
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
