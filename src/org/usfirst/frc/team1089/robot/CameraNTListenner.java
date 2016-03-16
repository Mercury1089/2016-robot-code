package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class CameraNTListenner implements ITableListener{

	private NetworkTable nt;
	private double[] rectWidth, rectHeight, rectCenterX, rectCenterY, rectArea;


	public CameraNTListenner(NetworkTable nt){
		this.nt = nt;
	}
	/**
	 * <pre>
	 * public void run()
	 * </pre>
	 * Has the Listener input information into the logger
	 */
	
	public void run(){
		double[] def = {}; // Return an empty array by default.
		
		/*NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1089-frc.local");*/
		
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
		Logger.log("String: " + string + " Value: " + Arrays.toString((double[])o) + " new: " + bln);
		switch (string) {
		case "area": {
			rectArea = (double[]) o;
			break;
		}
		case "width": {
			rectWidth = (double[]) o;
			break;
		}
		case "height": {
			rectHeight = (double[]) o;
			break;
		}
		case "centerX": {
			rectCenterX = (double[]) o;
			break;
		}
		case "centerY": {
			rectCenterY = (double[]) o;
			break;
		}
	    default:{
			break;
		}
		}
	}

	public void stop() {
		nt.removeTableListener(this);
	}
	
	public boolean isCoherent() {
		return (rectArea != null && rectWidth != null && rectHeight != null && rectCenterX != null
				&& rectCenterY != null && rectArea.length == rectWidth.length
				&& rectArea.length == rectHeight.length && rectArea.length == rectCenterX.length
				&& rectArea.length == rectCenterY.length);
	}
}
