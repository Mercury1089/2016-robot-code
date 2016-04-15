package org.usfirst.frc.team1089.robot;

import java.util.TimerTask;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import java.util.Timer;

public class PowerLogger extends TimerTask{
	private PowerDistributionPanel pdp;
	
	public PowerLogger() {
		pdp = new PowerDistributionPanel();
	}
	
	public void start() {
		 Timer timer = new Timer();
		 timer.schedule(this, 1000, 1000);
	}
	
	@Override
	public void run() {
		try {
			String msg = "";
			for (int i = 0; i <= 3; i++) {
				msg += "PDP(" + i + "):" + pdp.getCurrent(i) + " | ";
			}
			msg += "PDP(12):" + pdp.getCurrent(12) + " | ";
			msg += "PDP(13):" + pdp.getCurrent(13) + " | ";
			Logger.log(msg);
		}
		catch (Exception e) {
			
		}
	}

}
