package org.usfirst.frc.team1089.robot;

//import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.CANTalon;

public class PortcullisLifter {
	private CANTalon lift;
	public double POTENTIOMETER_MAX_TICKS = 500.0;
	public double POTENTIOMETER_MIN_TICKS = 0.0;
	
	public PortcullisLifter(CANTalon c) {
		lift = c;
		lift.enableBrakeMode(false);
		lift.setFeedbackDevice(CANTalon.FeedbackDevice.AnalogPot);
		lift.changeControlMode(CANTalon.TalonControlMode.Position);
	}
	
	public double getPosition(){
		return lift.get();
	}
	
	public void raise(){
		lift.set(POTENTIOMETER_MAX_TICKS);
	}
	
	public void lower(){
		lift.set(POTENTIOMETER_MIN_TICKS);
	}
	
}
