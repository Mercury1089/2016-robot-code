package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The {@code Camera} class handles targeting and vision using the input from
 * GRIP from the NetworkTable.
 *
 */
public class Camera {

	// Deploy NetworkTable to roboRIO
	private NetworkTable nt;
	private double largestRectArea;
	private int largestRectNum;
	private double targetWidthInches;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double diagTargetDistance, horizTargetDistance;

	public Camera(String tableLoc) {
		nt = NetworkTable.getTable(tableLoc);
	}

	/**
	 * <pre>
	 * private void getNTInfo()
	 * </pre>
	 * 
	 * Gets data from the NetworkTable, then calculates distance based on the
	 * rectangle and camera's horizontal FOV.
	 */
	public void getNTInfo() {
		double[] def = {};

		// Get data from NetworkTable
		rectArea = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
		rectCenterX = nt.getNumberArray("centerX", def);
		rectCenterY = nt.getNumberArray("centerY", def);
		
		try {
			largestRectArea = rectArea[0];
			largestRectNum = 0;
			for (int i = 0; i < rectArea.length; i++) {
				if (rectArea[i] >= largestRectArea) {
					largestRectNum = i;
				}
			}

			// Find width of target in inches
			targetWidthInches = rectWidth[largestRectNum] * .8 * (14.0 / rectHeight[largestRectNum]);

			// Calculate distance based off of rectangle width and horizontal
			// FOV of camera in feet.
			//
			// NOTE: Between .25 and .5 ft. off of actual distance
			diagTargetDistance = 
					(20.0 / 12.0) * (480.0 / rectWidth[largestRectNum]) / 2.0 / Math.tan(Math.toRadians(Ports.HFOV / 2));

			horizTargetDistance = Math.sqrt(diagTargetDistance * diagTargetDistance - 6.5 * 6.5);
		} catch (Exception e) {
			diagTargetDistance = Double.NEGATIVE_INFINITY;
		}
	}
	
	public double[] getRectArea(){
		return rectArea;
	}
	
	public double[] getRectWidth(){
		return rectWidth;
	}
	
	public double[] getRectHeight(){
		return rectHeight;
	}
	
	public double[] getCenterX(){
		return rectCenterX;
	}
	
	public double[] getCenterY(){
		return rectCenterY;
	}

	/**
	 * <pre>
	 * public void debug()
	 * </pre>
	 * 
	 * Puts information onto the SmartDashboard.
	 */
	public void debug() {
		SmartDashboard.putString("Area:", Arrays.toString(rectArea));
		SmartDashboard.putString("Width:", Arrays.toString(rectWidth));
		SmartDashboard.putString("Height:", Arrays.toString(rectHeight));
		SmartDashboard.putString("Center X:", Arrays.toString(rectCenterX));
		SmartDashboard.putString("Center Y:", Arrays.toString(rectCenterY));
		SmartDashboard.putString("Diagonal Distance: ", "" + round(diagTargetDistance, 2) + " ft.");
		SmartDashboard.putString("Horizontal Distance: ", "" + round(horizTargetDistance, 2) + " ft.");
		SmartDashboard.putString("Target Width Inches", "" + round(targetWidthInches, 2));
	}

	/**
	 * <pre>
	 * public static double round(double num,
	 *                            int places)
	 * </pre>
	 * Rounds the specified decimal to the specified amount of places.
	 * 
	 * @param num    the number to round
	 * @param places the amount of places to round to
	 * @return the specified decimal rounded to the specified amount of places 
	 */
	public static double round(double num, int places) {
		// Get decimal place to round to
		int dec = (int) Math.pow(10, places);

		// Round number by moving the decimal place, then
		// truncating and rounding
		long v = (long) (num * dec + .5);
		
		return ((double) (v)) / dec;
	}

}
