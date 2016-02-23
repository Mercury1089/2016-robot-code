package org.usfirst.frc.team1089.robot;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * The {@code Logger} class contains code to collect different kinds of data and store them in a document on the roborio
 */
public class Logger {

	private static Formatter formatter; //Formatter object, used to write data onto a text file

	private static String
		shooterTemplate = "%4.1d\t%s", //Time during match when the data was collected
		buttonTemplate = "%4.1d\t%s";

	/**
	 * The {@code LoggerType} is an enum for the possible kinds of data that will be collected.
	 */

	public enum LoggerType {
		SHOOTING_DATA("SHOOT"),
		BUTTON_PRESSED("BUTTON");

		private final String name;
		private LoggerType(String n) {
            name=n;
		}

		public String toString() {
            return name;
		}
	}

	static {
		try {
			formatter = new Formatter("/home/lvuser/log/" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")+".log"); //Location of the file 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		formatter.format("Shooter Information: Horizontal Distance\t Width:Height\t Angle To Turn\t Z-Axis \tButton Pressed: Gamepad\t Joystick");
				//Headers of the columns for the data
	}

	/**
	 * Returns reference to formatter of logger instance
	 *
	 * @return formatter
	 */
	private static Formatter getFormatter() {
		return formatter;
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
        try {
            switch(lE) {
            case SHOOTING_DATA:
                getFormatter().format(shooterTemplate, DriverStation.getInstance().getMatchTime(), lE.toString(), o);
                break;
            case BUTTON_PRESSED:
                getFormatter().format(buttonTemplate, DriverStation.getInstance().getMatchTime(), lE.toString(), o);
                break;
            }
        } catch(MissingFormatArgumentException exception) {
            getFormatter().format("%4.1d\tERROR/%s\tArgument list too short", DriverStation.getInstance().getMatchTime(), lE.toString());
        }
	}
}
