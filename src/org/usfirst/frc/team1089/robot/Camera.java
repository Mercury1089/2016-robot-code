package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * The {@code Camera} class handles targeting and vision using the input from
 * GRIP from the NetworkTable.
 *
 */
public class Camera {

	// TODO Change HFOV for the Camera. In degrees
	

	// Deploy NetworkTable to roboRIO
	private NetworkTable nt;
	private double largestRectArea;
	private int largestRectNum;
	private double perceivedOpeningWidth;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double diagTargetDistance, horizTargetDistance;
	private double diff;

	
	private static final double TARGET_WIDTH_INCHES = 20;
	private static final double TARGET_HEIGHT_INCHES = 12;
	private static final double INCHES_IN_FEET = 12.0;
	private static final double TARGET_ELEVATION_FEET = 6.5;
	private static final double HORIZ_DIST_MIN_FEET = 5.0;
	private static final double HORIZ_DIST_MAX_FEET = 7.0;
	private static final int MAX_NT_RETRY = 5;
	private Config config;

	/**
	 * <pre>
	 * public Camera(String tableLoc)
	 * </pre>
	 * Constructs a new {@code Camera} with the specified NetworkTable location.
	 * @param tableLoc the directory of the NetworkTable containing the input from GRIP
	 */
	public Camera(String tableLoc) {
		nt = NetworkTable.getTable(tableLoc);
		config = Config.getCurrent();
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
		double[] def = { }; // Return an empty array by default.
		boolean is_coherent = false; // Did we get coherent arrays form the NT?
		int retry_count = 0;

		// We cannot get arrays atomically but at least we can make sure they have the same size
		do
		{
			// Get data from NetworkTable
			rectArea = nt.getNumberArray("area", def);
			rectWidth = nt.getNumberArray("width", def);
			rectHeight = nt.getNumberArray("height", def);
			rectCenterX = nt.getNumberArray("centerX", def);
			rectCenterY = nt.getNumberArray("centerY", def);

			is_coherent = (rectArea.length == rectWidth.length && rectArea.length == rectHeight.length
					&& rectArea.length == rectCenterX.length && rectArea.length == rectCenterY.length);
			retry_count++;
		} while (!is_coherent && retry_count < MAX_NT_RETRY);

		if (is_coherent && rectArea.length > 0) { // searches array for largest target
			largestRectArea = rectArea[0];
			largestRectNum = 0;
			for (int i = 1; i < rectArea.length; i++) { // saves an iteration by
														// starting at 1
				if (rectArea[i] >= largestRectArea) {
					largestRectNum = i;
				}
			}
			// Find perceived width of opening in inches
			perceivedOpeningWidth = rectWidth[largestRectNum] * .8
					* (TARGET_HEIGHT_INCHES / rectHeight[largestRectNum]);

			// Calculate distance in feet based off of rectangle width and horizontal
			// FOV of camera
			// NOTE: Between .25 and .5 ft. off of actual distance
			diagTargetDistance = (TARGET_WIDTH_INCHES / INCHES_IN_FEET)
					* (config.HORIZONTAL_CAMERA_RES_PIXELS / rectWidth[largestRectNum]) / 2.0
					/ Math.tan(Math.toRadians(config.HFOV_DEGREES / 2));
		} else {
			largestRectArea = 0;
			
			largestRectNum = -1; // no such thing

			perceivedOpeningWidth = 0;

			diagTargetDistance = Double.POSITIVE_INFINITY;
		}

		horizTargetDistance = Math.sqrt(diagTargetDistance * diagTargetDistance
				- (TARGET_ELEVATION_FEET - config.CAM_ELEVATION_FEET) * (TARGET_ELEVATION_FEET - config.CAM_ELEVATION_FEET));
	}

	/**
	 * <pre>
	 * public double getTurnAngle()
	 * </pre>
	 * Gets the turn angle between the robot and the target.
	 * @return the turn angle in degrees between the robot and the target
	 */
	public double getTurnAngle() {
		if (rectArea.length > 0) {
			diff = ((config.HORIZONTAL_CAMERA_RES_PIXELS / 2) - getCenterX()[getLargestRectNum()])
					/ config.HORIZONTAL_CAMERA_RES_PIXELS;
			return diff * config.HFOV_DEGREES;
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

	/**
	 * <pre>
	 * public boolean isInDistance()
	 * </pre>
	 * Gets if the robot is within a certain distance of the goal.
	 * @return true if the robbot is in range, false if the robot is too close or too far
	 */
	public boolean isInDistance() {
		return getHorizontalDist() > HORIZ_DIST_MIN_FEET && getDiagonalDist() < HORIZ_DIST_MAX_FEET;
	}

	/**
	 * <pre>
	 * public boolean isInTurnAngle()
	 * </pre>
	 * Gets whether or not the robot is within turning distance of the goal.
	 * @return true if the robot can see the target and can turn to it, false if the target is out of the robot's turning range
	 */
	public boolean isInTurnAngle() {
		if (rectArea.length > 0) {
			return getTurnAngle() > config.TURN_ANGLE_MIN_DEGREES && getTurnAngle() < config.TURN_ANGLE_MAX_DEGREES;
		}
		else {
			return false; // if we cannot see the target we are not in turn angle regardless of the angle value
		}
	}

	/**
	 * <pre>
	 * public boolean isInLineWithGoal()
	 * </pre>
	 * Gets whether or not the robot is in line with the goal.
	 * @return true if the robot is in line with the goal, false if the goal is off to a side
	 */
	public boolean isInLineWithGoal() {
		if (rectArea.length > 0) {
			return Math.abs(rectWidth[largestRectNum] / rectHeight[largestRectNum]) > config.IN_LINE_MIN;
		} else {
			return false;
		}
	}
}
