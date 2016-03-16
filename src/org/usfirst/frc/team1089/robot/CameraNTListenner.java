package org.usfirst.frc.team1089.robot;

import java.util.Arrays;
import java.util.Calendar;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class CameraNTListenner implements ITableListener{
	public class Rectangles {
		public double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
		public Rectangles(double[] rectWidth, double[] rectHeight, double[] rectCenterX,  double[] rectCenterY,  double[] rectArea) {
			this.rectWidth = rectWidth;
			this.rectHeight = rectHeight;
			this.rectCenterX = rectCenterX;
			this.rectCenterY = rectCenterY;
			this.rectArea = rectArea;
		}
	}

	private NetworkTable nt;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;
	private Calendar tsRectWidth, tsRectHeight, tsRectCenterX, tsRectCenterY, tsRectArea;

	public CameraNTListenner(NetworkTable nt){
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
	
	public void run(){
		double[] def = {}; // Return an empty array by default.
		
		nt.addTableListener(this);
		Logger.log("Area: " + Arrays.toString(nt.getNumberArray("area", def)), " Width: " + Arrays.toString(nt.getNumberArray("width", def)), 
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
	 * @param string
	 * 		  The string of the type of data
	 * @param o
	 * 		  The object that has to be logged
	 * @param bln
	 * 		  Set to true if there is new data, false otherwise
	 */
	@Override
	public void valueChanged(ITable source, String string , Object o, boolean bln){
		Calendar ts = Calendar.getInstance();
		Logger.log("String: " + string + " Value: " + Arrays.toString((double[])o) + " new: " + bln);
		synchronized(this) {
			switch (string) {
				case "area": {
					rectArea = (double[]) o;
					tsRectArea = ts;
					break;
				}
				case "width": {
					rectWidth = (double[]) o;
					tsRectWidth = ts;
					break;
				}
				case "height": {
					rectHeight = (double[]) o;
					tsRectHeight = ts;
					break;
				}
				case "centerX": {
					rectCenterX = (double[]) o;
					tsRectCenterX = ts;
					break;
				}
				case "centerY": {
					rectCenterY = (double[]) o;
					tsRectCenterY = ts;
					break;
				}
			    default:{
					break;
				}
			}
		}
	}

	public void stop() {
		nt.removeTableListener(this);
	}
	
	public boolean isCoherent() {
		synchronized(this) {
			return (rectArea != null && rectWidth != null && rectHeight != null && rectCenterX != null
					&& rectCenterY != null && rectArea.length == rectWidth.length
					&& rectArea.length == rectHeight.length && rectArea.length == rectCenterX.length
					&& rectArea.length == rectCenterY.length);
		}
	}
	
	public Calendar getTimeStamp() {
		synchronized(this) {
			Calendar ts = tsRectArea.before(tsRectWidth) ? tsRectArea : tsRectWidth;
			ts = ts.before(tsRectHeight) ? ts : tsRectHeight;
			ts = ts.before(tsRectCenterX) ? ts : tsRectCenterX;
			ts = ts.before(tsRectCenterY) ? ts : tsRectCenterY;
			return ts;
		}
	}
	public Rectangles getRectangles(Camera camera) {
		return new Rectangles(rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea);
	}	
}
