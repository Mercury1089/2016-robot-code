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

	// Have to change - Horizontal Field of View for the Camera. In degrees
	public static final double HFOV_DEGREES = 41;
	public static final double CAM_ELEVATION_FEET = 9.5 / 12;

	// Deploy NetworkTable to roboRIO
	private NetworkTable nt;
	private double largestRectArea;
	private int largestRectNum;
	private double perceivedOpeningWidth;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double diagTargetDistance, horizTargetDistance;
	private double diff;

	public static final double HORIZONTAL_CAMERA_RES = 320;
	private static final double TARGET_WIDTH_INCHES = 20;
	private static final double TARGET_HEIGHT_INCHES = 12;
	private static final double INCHES_IN_FEET = 12.0;
	private static final double TARGET_ELEVATION_FEET = 6.5;
	private static final double HORIZ_DIST_MIN = 5.0;
	private static final double HORIZ_DIST_MAX = 7.0;
	private static final double TURN_ANGLE_MIN = -1.0;
	private static final double TURN_ANGLE_MAX = 1.0;
	private static final double IN_LINE_MIN = .4; // TODO FIX

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
		double[] def = { -1 };

		// Get data from NetworkTable
		rectArea = nt.getNumberArray("area", def);
		rectWidth = nt.getNumberArray("width", def);
		rectHeight = nt.getNumberArray("height", def);
		rectCenterX = nt.getNumberArray("centerX", def);
		rectCenterY = nt.getNumberArray("centerY", def);

		if (rectArea.length > 0) { // searches array for largest target
			largestRectArea = rectArea[0];
			largestRectNum = 0;
			for (int i = 1; i < rectArea.length; i++) { // saves an iteration by
														// starting at 1
				if (rectArea[i] >= largestRectArea) {
					largestRectNum = i;
				}
			}
			// Find width of target in inches
			perceivedOpeningWidth = rectWidth[largestRectNum] * .8
					* (TARGET_HEIGHT_INCHES / rectHeight[largestRectNum]);

			// Calculate distance based off of rectangle width and horizontal
			// FOV of camera in feet.
			// NOTE: Between .25 and .5 ft. off of actual distance
			diagTargetDistance = (TARGET_WIDTH_INCHES / INCHES_IN_FEET)
					* (HORIZONTAL_CAMERA_RES / rectWidth[largestRectNum]) / 2.0
					/ Math.tan(Math.toRadians(Camera.HFOV_DEGREES / 2));
		} else {
			largestRectNum = 0;

			perceivedOpeningWidth = 0;

			diagTargetDistance = Double.POSITIVE_INFINITY;
		}

		horizTargetDistance = Math.sqrt(diagTargetDistance * diagTargetDistance
				- (TARGET_ELEVATION_FEET - CAM_ELEVATION_FEET) * (TARGET_ELEVATION_FEET - CAM_ELEVATION_FEET));
	}

	public double getTurnAngle() {
		if (rectArea.length > 0) {
			diff = ((Camera.HORIZONTAL_CAMERA_RES / 2) - getCenterX()[getLargestRectNum()])
					/ Camera.HORIZONTAL_CAMERA_RES;
			return diff * Camera.HFOV_DEGREES;
		}

		return 0;

	}

	public double[] getRectArea() {
		return rectArea;
	}

	public double[] getRectWidth() {
		return rectWidth;
	}

	public double[] getRectHeight() {
		return rectHeight;
	}

	public double[] getCenterX() {
		return rectCenterX;
	}

	public double[] getCenterY() {
		return rectCenterY;
	}

	public double getDiagonalDist() {
		return diagTargetDistance;
	}

	public int getLargestRectNum() {
		return largestRectNum;
	}

	public double getHorizontalDist() {
		return horizTargetDistance;
	}

	public double getOpeningWidth() {
		return perceivedOpeningWidth;
	}

	public boolean isInDistance() {
		return getHorizontalDist() > HORIZ_DIST_MIN && getDiagonalDist() < HORIZ_DIST_MAX;
	}

	public boolean isInTurnAngle() {
		return getTurnAngle() > TURN_ANGLE_MIN && getTurnAngle() < TURN_ANGLE_MAX;
	}

	public boolean isInLineWithGoal() {
		if (rectArea.length > 0) {
			return Math.abs(rectWidth[largestRectNum] / rectHeight[largestRectNum]) > IN_LINE_MIN;
		} else
			return false;
	}
}
