package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;

public class DriveTrain {

	private CANTalon lft, rft, lbt, rbt;
	
	public DriveTrain(CANTalon leftFront, CANTalon rightFront, CANTalon leftBack, CANTalon rightBack) {
		lft = leftFront;
		rft = rightFront;
		lbt = leftBack;
		rbt = rightBack;
		lft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		rft.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		lbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		rbt.changeControlMode(CANTalon.TalonControlMode.Follower);
		lbt.set(lft.getDeviceID());
		rbt.set(rft.getDeviceID());
	}
	
	public void tankDrive(double leftValue, double rightValue) {
		lft.set(leftValue);
		rft.set(rightValue);
	}
	
}
