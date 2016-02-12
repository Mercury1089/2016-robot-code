package org.usfirst.frc.team1089.robot;

/**
 * The {@code Utilities} class is a static class containing various utility methods to use throughout the code.
 */
public class Utilities {
	
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
