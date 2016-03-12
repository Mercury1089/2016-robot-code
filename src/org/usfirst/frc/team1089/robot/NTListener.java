package org.usfirst.frc.team1089.robot;

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
		
		Logger.log("Area: " + t.getNumberArray("area", def), " Width: " + t.getNumberArray("width", def), 
				   " Height: " + t.getNumberArray("height", def), " CenterX: " + t.getNumberArray("centerX", def), 
				   "CenterY: " + t.getNumberArray("centerY", def));
		
	}
	
	@Override
	public void valueChanged(ITable source, String string , Object o, boolean bln){
		Logger.log("String: " + string + " Value: " + o + " new: " + bln);
	}

	public void stop() {
		t.removeTableListener(this);
	}
}
