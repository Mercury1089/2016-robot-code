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
	public static final double HFOV = 47; //Have to change - Horizontal Field of View for the Camera. In degrees
	private NetworkTable nt;
	private double largestRectArea;
	private int largestRectNum;
	private double targetWidthInches;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double diagTargetDistance, horizTargetDistance;
	private static final double HORIZONTAL_CAMERA_RES = 320; //480
	private static final double TARGET_HEIGHT_INCHES = 20;
	private static final double INCHES_IN_FEET = 12.0;
	private static final double TARGET_ELEVATION_FEET = 6.5;
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
		double[] def = {Double.NEGATIVE_INFINITY};

		// Get data from NetworkTable
		rectArea = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
		rectCenterX = nt.getNumberArray("centerX", def);
		rectCenterY = nt.getNumberArray("centerY", def);
		
		if(rectArea.length != 0){ //searches array for largest target
			largestRectArea = rectArea[0];
			largestRectNum = 0;
			for (int i = 1; i < rectArea.length; i++) { //saves an iteration by starting at 1
				if (rectArea[i] >= largestRectArea) {
					largestRectNum = i;
				}
			}
			// Find width of target in inches
			//targetWidthInches = rectWidth[largestRectNum] * .8 * (14.0 / rectHeight[largestRectNum]);

			// Calculate distance based off of rectangle width and horizontal
			// FOV of camera in feet.
			//
			// NOTE: Between .25 and .5 ft. off of actual distance
			diagTargetDistance = 
					(TARGET_HEIGHT_INCHES / INCHES_IN_FEET) * (HORIZONTAL_CAMERA_RES / rectWidth[largestRectNum]) / 2.0 / Math.tan(Math.toRadians(Camera.HFOV / 2));
			
			horizTargetDistance = Math.sqrt(diagTargetDistance * diagTargetDistance - TARGET_ELEVATION_FEET * TARGET_ELEVATION_FEET);
		}
		else{
			diagTargetDistance = Double.POSITIVE_INFINITY;
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
	
	public double getDiagonalDist() {
		return diagTargetDistance;
	}
	
	public int getLargestRectNum(){
		return largestRectNum;
	}
	
	public double getHorizontalDist() {
		return horizTargetDistance;
	}
}
