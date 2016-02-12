package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

public class Shooter {
	private Solenoid shooter;
	private DoubleSolenoid elevator;
	private static final long SHOOTER_RELEASE_DELAY_MS = 250;

	public Shooter() {
		shooter = new Solenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER);
		elevator = new DoubleSolenoid(Ports.CAN.PCM_ID, Ports.PCM.SHOOTER_ELEVATOR_FORWARD, Ports.PCM.SHOOTER_ELEVATOR_REVERSE);
	}

	public void raise(boolean level) {
		if (level) {
			elevator.set(DoubleSolenoid.Value.kForward);
		}
		else{
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
	
	public boolean isElevatorUp(){
		return elevator.get() == DoubleSolenoid.Value.kForward;
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
