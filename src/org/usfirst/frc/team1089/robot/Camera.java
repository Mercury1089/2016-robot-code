package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * The {@code Camera} class handles targeting and vision using the input from
 * GRIP from the NetworkTable.
 *
 */
public class Camera {
	private NetworkTable nt;
	private double largestRectArea;
	private int largestRectNum;
	private double perceivedOpeningWidth;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private double diagTargetDistance, horizTargetDistance;
	private double diff;
	private CameraNTListenner ntListener;

	private static final double TARGET_WIDTH_INCHES = 20;
	private static final double TARGET_HEIGHT_INCHES = 12;
	private static final double INCHES_IN_FEET = 12.0;
	private static final double TARGET_ELEVATION_FEET = 6.5;

	private static final int MAX_NT_RETRY = 5;

	private Config config;

	/**
	 * <pre>
	 * public Camera(String tableLoc)
	 * </pre>
	 * 
	 * Constructs a new {@code Camera} with the specified NetworkTable location.
	 * 
	 * @param tableLoc
	 *            the directory of the NetworkTable containing the input from
	 *            GRIP
	 */
	public Camera(String tableLoc) {
		nt = NetworkTable.getTable(tableLoc);
		ntListener = new CameraNTListenner(nt);
		config = Config.getInstance();
	}

	/**
	 * <pre>
	 * private void getNTInfo()
	 * </pre>
	 * 
	 * Gets data from the NetworkTable, then calculates distance based on the
	 * rectangle and camera's horizontal FOV.
	 * 
	 * @param waitForNewInfo
	 * 		Wait for a new copy of the NT info
	 */
	public void getNTInfo(boolean waitForNewInfo) {
		double[] def = {}; // Return an empty array by default.
		boolean is_coherent = false; // Did we get coherent arrays from the NT?
		int retry_count = 0;

		rectArea = null;
		rectWidth = null;
		rectHeight = null;
		rectCenterX = null;
		rectCenterY = null;
		
		if (waitForNewInfo) {
			Timer.delay(DriveTrain.AUTOROTATE_CAMERA_CATCHUP_DELAY_SECS);
		}

		// We cannot get arrays atomically but at least we can make sure they
		// have the same size
		do {
			// Get data from NetworkTable
			rectArea = nt.getNumberArray("area", def);
			rectWidth = nt.getNumberArray("width", def);
			rectHeight = nt.getNumberArray("height", def);
			rectCenterX = nt.getNumberArray("centerX", def);
			rectCenterY = nt.getNumberArray("centerY", def);

			is_coherent = (rectArea != null && rectWidth != null && rectHeight != null && rectCenterX != null
					&& rectCenterY != null && rectArea.length == rectWidth.length
					&& rectArea.length == rectHeight.length && rectArea.length == rectCenterX.length
					&& rectArea.length == rectCenterY.length);
			retry_count++;
		} while (!is_coherent && retry_count < MAX_NT_RETRY);

		if (is_coherent && rectArea.length > 0) { // searches array for largest
													// target
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

			// Calculate distance in feet based off of rectangle width and
			// horizontal
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

		horizTargetDistance = Math
				.sqrt(diagTargetDistance * diagTargetDistance - (TARGET_ELEVATION_FEET - config.CAM_ELEVATION_FEET)
						* (TARGET_ELEVATION_FEET - config.CAM_ELEVATION_FEET));
	}
	
	/**
	 * <pre>
	 * public double getTurnAngle()
	 * </pre>
	 * 
	 * Gets the turn angle between the robot and the target.
	 * 
	 * @return the turn angle in degrees between the robot and the target
	 */
	public double getTurnAngle() {
		if (isTargetFound()) {
			diff = (getCenterX()[getLargestRectNum()] - (config.HORIZONTAL_CAMERA_RES_PIXELS / 2))
					/ config.HORIZONTAL_CAMERA_RES_PIXELS;
			return diff * config.HFOV_DEGREES;
		} else {
			return 0; // we don't know where to turn if target is not found, so
						// we don't turn
		}
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

	public CameraNTListenner getNTListenner() {
		return this.ntListener;
	}
	/**
	 * <pre>
	 * public boolean isTargetFound()
	 * </pre>
	 * 
	 * Indicates if we have at least one target on screen
	 * <p>
	 * This method assumes that the network tables have already been fetched
	 * </p>
	 * 
	 * @return true if at least one target was found, false otherwise
	 */
	public boolean isTargetFound() {
		if ((rectArea != null && rectWidth != null && rectHeight != null && rectCenterX != null && rectCenterY != null
				&& rectArea.length == rectWidth.length && rectArea.length == rectHeight.length
				&& rectArea.length == rectCenterX.length && rectArea.length == rectCenterY.length)
				&& rectArea.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <pre>
	 * public boolean isInDistance()
	 * </pre>
	 * 
	 * Gets if the robot is within shooting distance of the goal.
	 * 
	 * @return true if the robot is in range, false if the robot is too close or
	 *         too far
	 */
	public boolean isInDistance(){
		return isInFarDistance() || isInCloseDistance();
	}
	
	/**
	 * Returns if the robot is within long-range shooting distance of the goal
	 * 
	 * @return true if within long-range shooting distance
	 */
	public boolean isInFarDistance() {
		return (getHorizontalDist() > config.HORIZ_DIST_MIN_FEET && getHorizontalDist() < config.HORIZ_DIST_MAX_FEET);
	}
	
	/**
	 * Returns if the robot is within short-range shooting distance of the goal
	 * 
	 * @return true if within short-range shooting distance
	 */
	public boolean isInCloseDistance() {
		return (getHorizontalDist() > config.HORIZ_CLOSE_DIST_MIN_FEET && getHorizontalDist() < config.HORIZ_CLOSE_DIST_MAX_FEET);
	}

	/**
	 * <pre>
	 * public boolean isInTurnAngle()
	 * </pre>
	 * 
	 * Gets whether or not the robot is properly aiming at the center of the goal.
	 * 
	 * @return true if the robot is aiming at the center of the goal, false if
	 *         otherwise
	 */
	public boolean isInTurnAngle() {
		if (isTargetFound()) {
			return getTurnAngle() > config.TURN_ANGLE_MIN_DEGREES && getTurnAngle() < config.TURN_ANGLE_MAX_DEGREES;
		} else {
			return false; // if we cannot see the target we are not in turn
							// angle regardless of the angle value
		}
	}

	/**
	 * <pre>
	 * public boolean isInLineWithGoal()
	 * </pre>
	 * 
	 * Gets whether or not the robot is in line with the goal.
	 * 
	 * @return true if the robot is in line with the goal, false if the goal is
	 *         off to a side
	 */
	public boolean isInLineWithGoal() {
		if (isTargetFound()) {
			return Math.abs(rectWidth[largestRectNum] / rectHeight[largestRectNum]) > config.IN_LINE_MIN;
		} else {
			return false; // if we cannot see the target then we are not in line
							// for sure
		}
	}
}
