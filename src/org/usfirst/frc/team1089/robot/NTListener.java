package org.usfirst.frc.team1089.robot;

import java.util.Arrays;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class NTListener implements ITableListener{

	private NetworkTable t = NetworkTable.getTable("GRIP/myContoursReport");
	
	public static void main(String[] args){
		new NTListener().run();
	}
	
	public void run(){
		double[] def = {}; // Return an empty array by default.
		/*NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1089-frc.local");*/
		
		t.addTableListener(this);
		
		Logger.log("Area: " + Arrays.toString(t.getNumberArray("area", def)), " Width: " + Arrays.toString(t.getNumberArray("width", def)), 
				   " Height: " + Arrays.toString(t.getNumberArray("height", def)), " CenterX: " + Arrays.toString(t.getNumberArray("centerX", def)), 
				   "CenterY: " + Arrays.toString(t.getNumberArray("centerY", def)));
		
	}
	
	@Override
	public void valueChanged(ITable source, String string , Object o, boolean bln){
		Logger.log("String: " + string + " Value: " + Arrays.toString((double[])o) + " new: " + bln);
	}

	public void stop() {
		t.removeTableListener(this);
	}
}
