// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	// Deploy NetworkTable to roboRIO
	NetworkTable nt;

	double[] rectWidth, rectHeight, rectCenterX, rectCenterY, area;

	String testStr;
	int count = 0;

	public void robotInit() {
		nt = NetworkTable.getTable("GRIP/myContoursReport");
	}

	public void autonomousPeriodic() {

	}

	public void teleopPeriodic() {
		// Get values from NetworkTable and put into the SmartDashboard
		getNTInfo();
		debug();
	}

	public void testPeriodic() {

	}

	private void getNTInfo() {
		double[] def = { -1 };
		area = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
	}

	private void debug() {
		SmartDashboard.putString("Area", Arrays.toString(area));
		SmartDashboard.putString("Width", Arrays.toString(rectWidth));
		SmartDashboard.putString("NT", nt.toString());
		SmartDashboard.putString("Height", Arrays.toString(rectHeight));
	}
}
