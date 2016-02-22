package org.usfirst.frc.team1089.robot;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * The {@code Logger} class contains code to collect different kinds of data and store them in a document on the roborio
 */
public class Logger {
	
	private static Logger _currentLogger = null; //Initialized by getCurrent() if necessary

	private static Formatter formatter; //Formatter object, used to write data onto a text file
	
	private static String shooterTemplate = DriverStation.getInstance().getMatchTime() + "%d\t"; //Time during match when the data was collected
	private static String buttonTemplate = DriverStation.getInstance().getMatchTime()  + "%d\t";

	/**
	 * The {@code LoggerType} is an enum for the possible kinds of data that will be collected.
	 */

	public enum LoggerType {
		SHOOTING_DATA,
		BUTTON_PRESSED;
	}
	
	
	private Logger() {
		try {
			formatter = new Formatter("/home/lvuser/log/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")+".log"); //Location of the file
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		formatter.format("Shooter Information: Horizontal Distance\t Width:Height\t Angle To Turn\t Z-Axis \tButton Pressed: Gamepad\t Joystick");
				//Headers of the columns for the data
	}
	
	/**
	 * <pre>
	 * public synchronized static Logger getCurrent()
	 * </pre>
	 * Gets the current {@code Logger} being used by the robot.
	 * @return the current {@code Logger} being used by the robot.
	 */
	public synchronized static Logger getCurrent() {
		if (_currentLogger == null) {
			_currentLogger = new Logger();
		}
		return _currentLogger;
	}

	/**
	 * <pre>
	 * public synchronized static void Logger debug()
	 * </pre>
	 * Writes the data onto the file
	 * @param lE
	 * 				the {@code LoggerType} for which type of data is the input
	 * @param o
	 * 				the {@code Object} creates an Object[] for the data that comes in
	 */
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
