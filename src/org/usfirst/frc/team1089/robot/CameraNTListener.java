package org.usfirst.frc.team1089.robot;

import java.util.Arrays;
import java.util.Calendar;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class CameraNTListener implements ITableListener{

	private NetworkTable nt;
	private boolean isListening = false;
	
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private Calendar tsRectWidth, tsRectHeight, tsRectCenterX, tsRectCenterY, tsRectArea;

	public CameraNTListener(NetworkTable nt){
		this.nt = nt;
		rectWidth = rectHeight = rectCenterX = rectCenterY = rectArea = null;
		tsRectWidth = tsRectHeight = tsRectCenterX = tsRectCenterY = tsRectArea = Calendar.getInstance();
	}
	/**
	 * <pre>
	 * public void run()
	 * </pre>
	 * Has the Listener input information into the logger
	 */
	
	public synchronized void run(){
		if (!isListening) {
			nt.addTableListener(this);
			isListening = true;
		}
		double[] def = {}; // Return an empty array by default.
		Logger.log("Listening started. Area: " + Arrays.toString(nt.getNumberArray("area", def)), " Width: " + Arrays.toString(nt.getNumberArray("width", def)), 
			   " Height: " + Arrays.toString(nt.getNumberArray("height", def)), " CenterX: " + Arrays.toString(nt.getNumberArray("centerX", def)), 
			   "CenterY: " + Arrays.toString(nt.getNumberArray("centerY", def)));
	}
	
	/**
	 * <pre>
	 * public void valueChanged(ITable source, String string , Object o, boolean bln)
	 * </pre>
	 * Runs every time a value changes in the network table and logs the change
	 * @param source 
	 * 		  The table from which to get the data and the table to check for changes
	 * @param key
	 * 		  The key associated with the value that changed
	 * @param value
	 * 		  The new value from the table
	 * @param isNew
	 * 		  true if the key did not previously exist in the table, otherwise it is false
	 */
	@Override
	public synchronized void valueChanged(ITable source, String key , Object value, boolean isNew){
		Calendar ts = Calendar.getInstance(); // Get the time before synchronized so time is accurate as possible
		Logger.log("String: " + key + " Value: " + Arrays.toString((double[])value) + " new: " + isNew);
		synchronized(this) {
			switch (key) {
				case "area": {
					rectArea = (double[]) value;
					tsRectArea = ts;
					break;
				}
				case "width": {
					rectWidth = (double[]) value;
					tsRectWidth = ts;
					break;
				}
				case "height": {
					rectHeight = (double[]) value;
					tsRectHeight = ts;
					break;
				}
				case "centerX": {
					rectCenterX = (double[]) value;
					tsRectCenterX = ts;
					break;
				}
				case "centerY": {
					rectCenterY = (double[]) value;
					tsRectCenterY = ts;
					break;
				}
			    default:{
					break;
				}
			}
		}
	}

	/**
	 * <pre>
	 * public void stop()
	 * </pre>
	 * 
	 * Stop listening for updates.
	 */
	public synchronized void stop() {
		if (isListening) {
			nt.removeTableListener(this);
			isListening = false;
		}
	}

	/**
	 * <pre>
	 * public Calendar getModificationTime()
	 * </pre>
	 * 
	 * Get the last modification time of the rectangle data.
	 * 
	 * @return The modification time of the <em>oldest</em> rectangle.
	 */
	public synchronized Calendar getModificationTime() {
		Calendar ts = tsRectArea.before(tsRectWidth) ? tsRectArea : tsRectWidth;
		ts = ts.before(tsRectHeight) ? ts : tsRectHeight;
		ts = ts.before(tsRectCenterX) ? ts : tsRectCenterX;
		ts = ts.before(tsRectCenterY) ? ts : tsRectCenterY;
		return ts;
	}
	
	/**
	 * <pre>
	 * public void getRectangles(Camera camera)
	 * </pre>
	 * 
	 * Copy the current rectangle data into the supplied Camera instance.
	 * Relies on the Camera.setRectangles() method.
	 * 
	 * @param camera the Camera instance to update the data of.
	 */
	public synchronized void getRectangles(Camera camera) {
		// Copy the current rectangles to the camera instance
		camera.setRectangles(rectArea, rectWidth, rectHeight, rectCenterX, rectCenterY);
	}	
}
