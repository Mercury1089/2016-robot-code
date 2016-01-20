// Robot for 2016 FIRST Stronghold competition

package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {

	NetworkTable nt;
	double[] rectWidth, rectHeight, rectCenterX, rectCenterY;
	
	public void robotInit() {
	
	}

	public void autonomousPeriodic() {

    }

    public void teleopPeriodic() {
    	nt = NetworkTable.getTable("GRIP/myContoursReport");
    	   
    	getNTInfo();
        debug();
    }
    
    public void testPeriodic() {
    
    }

    private void getNTInfo() {
    	double[] iv = {-1};
    	
    	rectWidth = nt.getNumberArray("width", iv);
    	rectHeight = nt.getNumberArray("height", iv);
    	rectCenterX = nt.getNumberArray("centerX", iv);
    	rectCenterY = nt.getNumberArray("centerY", iv);
    	
    }
    
    private void debug() {
    	SmartDashboard.putString("Rect Center", "(" + rectCenterX[0] + ", " + rectCenterY[0] + ")");
    	SmartDashboard.putString("Rect Size", "(" + rectWidth[0] + ", " + rectHeight[0] + ")");
    }
}
