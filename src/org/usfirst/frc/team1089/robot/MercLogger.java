package org.usfirst.frc.team1089.robot;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.wpi.first.wpilibj.DriverStation;


public class MercLogger {
	
	private static MercLogger _currentLogger = null;

	private static Formatter formatter;
	
	private static String shooterTemplate = DriverStation.getInstance().getMatchTime() + "%d\t";
	private static String buttonTemplate = DriverStation.getInstance().getMatchTime()  + "%d\t";

	
	public enum LoggerType {
		SHOOTING_DATA,
		BUTTON_PRESSED;
	}
	
	
	private MercLogger() {
		try {
			formatter = new Formatter("/home/lvuser/log/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")+".log");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		formatter.format("Shooter Information: Horizontal Distance\t Width:Height\t Angle To Turn\t Z-Axis \tButton Pressed: Gamepad\t Joystick");
	}
	

	public synchronized static MercLogger getCurrent() {
		if (_currentLogger == null) {
			_currentLogger = new MercLogger();
		}
		return _currentLogger;
	}

	public synchronized static void debug(LoggerType lE, Object... o) {		
		switch(lE) {
		case SHOOTING_DATA:
			formatter.format(shooterTemplate, o);
			break;
		case BUTTON_PRESSED:
			formatter.format(buttonTemplate,  o);
			break;
		}
	}
}
