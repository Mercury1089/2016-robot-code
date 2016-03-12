package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

public class NTListener implements ITableListener{

	
	public static void main(String[] args){
		new NTListener().run();
	}
	
	public void run(){
		double[] def = {}; // Return an empty array by default.
		/*NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1089-frc.local");*/
		NetworkTable t = NetworkTable.getTable("GRIP/myContoursReport");
		
		t.addTableListener(this);
		
		try {
			Thread.sleep(100000);
		} catch(InterruptedException ex) {
			Logger.log(t.getNumberArray("area", def), t.getNumberArray("width", def), t.getNumberArray("height", def),
					   t.getNumberArray("centerX", def), t.getNumberArray("centerY", def));
		}
	}
	
	@Override
	public void valueChanged(ITable source, String string , Object o, boolean bln){
		Logger.log("String: " + string + " Value: " + o + " new: " + bln);
	}

}
