// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	// Deploy NetworkTable to roboRIO
	NetworkTable nt;

	double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	double targetDistanceW;

	public void robotInit() {
		nt = NetworkTable.getTable("GRIP/myContoursReport");
	}

	public void autonomousPeriodic() {

	}

	public void teleopPeriodic() {
		// Get values from NetworkTable and put into SmartDash
		getNTInfo();
		debug();
	}

	public void testPeriodic() {

	}

	private void getNTInfo() {
		double[] def = {};
		rectArea = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
		rectCenterX = nt.getNumberArray("centerX", def);
		rectCenterY = nt.getNumberArray("centerY", def);
		//calculate distance using width
		try {
			targetDistanceW = (20.0 / 12.0) * (480.0 / rectWidth[0]) / 2.0 / Math.tan(Math.toRadians(29.5)); 
		} catch (Exception e) {
			targetDistanceW = Double.NEGATIVE_INFINITY;
		}
	}

	private void debug() {
		SmartDashboard.putString("Area:", Arrays.toString(rectArea));
		SmartDashboard.putString("Width:", Arrays.toString(rectWidth));
		SmartDashboard.putString("Height:", Arrays.toString(rectHeight));
		SmartDashboard.putString("Center X:", Arrays.toString(rectCenterX));
		SmartDashboard.putString("Center Y:", Arrays.toString(rectCenterY));
		SmartDashboard.putNumber("DistanceW:", targetDistanceW);
	}

}
