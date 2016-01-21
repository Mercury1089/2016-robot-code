// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	// Deploy NetworkTable to roboRIO
	NetworkTable nt;

	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double targetDistance;
	
	private boolean[] btn, btnPrev;

	private final double HFOV = 29.5;
	
	//private Joystick gamepad;
	
	public void robotInit() {
		nt = NetworkTable.getTable("GRIP/myContoursReport");
		//gamepad = new Joystick(Ports.USB.GAMEPAD);
	}

	public void autonomousPeriodic() {

	}

	public void teleopPeriodic() {
		// Get values from NetworkTable and put into SmartDash
		getNTInfo();
		debug();
		
		/*btn = new boolean[11];
		for (int i = 1; i <= 10; i++) {
			//btn[i] = gamepad.getRawButton(i);
		}*/
		
		//btnPrev = Arrays.copyOf(btn, 11);
	}

	public boolean button(int i) {
		return btn[i] && !btnPrev[i];
	}
	
	public void testPeriodic() {

	}

	/**
	 * <pre>
	 * private void getNTInfo()
	 * </pre>
	 * Gets data from the NetworkTable, then
	 * calculates distance based on the rectangle and camera's
	 * horizontal FOV.
	 */
	private void getNTInfo() {
		double[] def = {};
		
		// Get data from NetworkTable
		rectArea = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
		rectCenterX = nt.getNumberArray("centerX", def);
		rectCenterY = nt.getNumberArray("centerY", def);
		
		// Calculate distance based off of rectangle width and horizontal FOV of camera in feet.
		// NOTES:
		//     > Between .25 and .5 ft. off of actual distance
		try {
			targetDistance = (20.0 / 12.0) * (480.0 / rectWidth[0]) / 2.0 / Math.tan(Math.toRadians(HFOV)); 
		} catch (Exception e) {
			targetDistance = Double.NEGATIVE_INFINITY;				
		}															
		
	}
	
	/**
	 * <pre>
	 * private void debug()
	 * </pre>
	 * Puts data onto the SmartDashboard.
	 */
	private void debug() {
		SmartDashboard.putString("Area:", Arrays.toString(rectArea));
		SmartDashboard.putString("Width:", Arrays.toString(rectWidth));
		SmartDashboard.putString("Height:", Arrays.toString(rectHeight));
		SmartDashboard.putString("Center X:", Arrays.toString(rectCenterX));
		SmartDashboard.putString("Center Y:", Arrays.toString(rectCenterY));
		SmartDashboard.putString("Distance: ", "" + targetDistance + " ft.");
	}

}
