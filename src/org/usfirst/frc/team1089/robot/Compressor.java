package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Compressor {
	private DigitalInput checkPressure;
	private Relay relay;
	private static final long CHECK_TIME_MS = 20;
	private Timer timer = new Timer();

	public Compressor() {
		checkPressure = new DigitalInput(Ports.Digital.CHECK_PRESSURE);
		relay = new Relay(Ports.Digital.COMPRESSOR_RELAY);
	}

	public void checkCompressor() {
		timer.schedule(new CheckCompressorTask(checkPressure, relay), CHECK_TIME_MS, CHECK_TIME_MS);
	}

	public class CheckCompressorTask extends TimerTask {
		private DigitalInput _checkPressure;
		private Relay _relay;

		public CheckCompressorTask(DigitalInput dI, Relay r) {
			_checkPressure = dI;
			_relay = r;
		}

		@Override
		public void run() {
			if (_checkPressure.get()) {
				_relay.set(Relay.Value.kOff);
			} else {
				_relay.set(Relay.Value.kForward);
			}
		}

	}
}
