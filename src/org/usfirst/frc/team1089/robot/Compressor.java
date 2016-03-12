package org.usfirst.frc.team1089.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.AnalogInput;
//import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Relay;

/**
 * The {@code Compressor} class contains fields and methods pertaining to the function of the compressor.
 */
public class Compressor {
	//private DigitalInput checkPressure;
	private AnalogInput pressureSwitch;
	private Relay relay;
	private static final long CHECK_TIME_MS = 20;
	private Timer timer = new Timer();
	private static final double MAX_PRESSURE_VOLTS = 3.0; //120 PSI
	private static final double MIN_PRESSURE_VOLTS = 1.25; //50 PSI

	/**
	 * <pre>
	 * public Compressor()
	 * </pre>
	 * Constructs a new {@code Compressor} with a set {@code DigitalInput} and {@code Relay}.
	 */
	public Compressor() {
		//checkPressure = new DigitalInput(Ports.Digital.CHECK_PRESSURE);
		pressureSwitch = new AnalogInput(Ports.Analog.CHECK_PRESSURE); // TODO: Change port for this
		relay = new Relay(Ports.Relay.COMPRESSOR_RELAY);
	}

	/**
	 * <pre>
	 * public void checkCompressor()
	 * </pre>
	 * Schedules a task to check the compressor.
	 */
	public void checkCompressor() {
		//timer.schedule(new CheckCompressorTask(checkPressure, relay), CHECK_TIME_MS, CHECK_TIME_MS);
		timer.schedule(new CheckCompressorTask(pressureSwitch, relay), CHECK_TIME_MS, CHECK_TIME_MS);
	}

	/**
	 * The {@code CheckCompressorTask} is a {@code TimerTask} used to check if the compressor can shoot, and act accordingly.
	 */
	private class CheckCompressorTask extends TimerTask {
		//private DigitalInput _checkPressure;
		private AnalogInput inputSwitch;
		private Relay _relay;

		/**
		 * <pre>
		 * public CheckCompressorTask(DigitalInput dI,
		 *                            Relay r)
		 * </pre>
		 * Constructs a new {@code CheckCompressorTask} with a specified {@code DigitalInput} and {@code Relay} to use
		 * for checking the compressor.
		 * @param dI the {@code DigitalInput} to use to check the compressor
		 * @param r the {@code Relay} to use to manipulate the compressor
		 */
		//public CheckCompressorTask(DigitalInput dI, Relay r) {
		public CheckCompressorTask(AnalogInput iS, Relay r) {
			inputSwitch = iS;
			//_checkPressure = dI;
			_relay = r;
		}

		@Override
		public void run() {
			if (inputSwitch.getVoltage() > MAX_PRESSURE_VOLTS) {
				_relay.set(Relay.Value.kOff);
			} else if (inputSwitch.getVoltage() < MIN_PRESSURE_VOLTS){
				_relay.set(Relay.Value.kForward);
			}
		}
		
		/*public void run() {
			if (_checkPressure.get()) {
				_relay.set(Relay.Value.kOff);
			} else {
				_relay.set(Relay.Value.kForward);
			}
		}
*/
	}
}
