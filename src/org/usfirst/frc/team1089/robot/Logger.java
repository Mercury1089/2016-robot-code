package org.usfirst.frc.team1089.robot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * The {@code Logger} class is a class that contains methods to writing a log text file for the robot.
 *
 */
public class Logger {
	private static File log;
	private static FileWriter out;
	private static PrintWriter writer;
	private static DriverStation ds;
	
	private static final Calendar CALENDAR;
	private static final SimpleDateFormat DAY;
	
	static {
		CALENDAR = Calendar.getInstance();
		DAY = new SimpleDateFormat("yyyy-MM-dd_HH,mm,ss");
	}
	
	/**
	 * <pre>
	 * public synchronized static void init(String location)
	 * </pre>
	 * Initializes the current {@code Logger} with the file at the specified location.
	 * @param location the location to store the log file. Note that it is only the path;
	 *        the filename itself is handled inside the method.
	 */
	public synchronized static void init() {
		if (log == null) {
			try {
				File path;
				log = new File("home/lvuser/log/log_" + DAY.format(CALENDAR.getTime()) + ".txt");
				path = log.getParentFile();
				
				if (!path.exists())
					path.mkdirs();
				
				if (!log.exists())
					log.createNewFile();
				
				out = new FileWriter(log.getAbsolutePath());
				writer = new PrintWriter(out);
				ds = DriverStation.getInstance();
			} catch (Exception e) { 
				e.printStackTrace(System.out);
			}
		}
	}
	
	/**
	 * <pre>
	 * public static synchronized void log(Object... input)
	 * </pre>
	 * Logs the specified input into the log file, separating elements by tabs, and timestamps it with the current time of the match.
	 * @param input the text to put into the log.
	 */
	public static synchronized void log(Object... input){
		try {
			String out = "";
			for (Object o : input)
				out += o.toString() + '\t';
			writer.println("[" + ds.getMatchTime() + "]: " + out);
		} catch (Exception e) { 
			e.printStackTrace(System.out);
		}
	}
	
	/**
	 * <pre>
	 * public static synchronized void logWarning(String warn)
	 * </pre>
	 * Logs the specified warning into the log file, and timestamps it with the current time of the match.
	 * @param warning the warning to put into the log.
	 */
	public static synchronized void logWarning(String warn) {
		log("WARN: " + warn);
	}
	
	/**
	 * <pre>
	 * public static synchronized void logError(String error)
	 * </pre>
	 * Logs the specified error into the log file, and timestamps it with the current time of the match.
	 * @param error the error to put into the log.
	 */
	public static synchronized void logError(String error) {
		log("ERROR: " + error);
	}
	
	/**
	 * <pre>
	 * public static synchronized void logTrace(Exception e)
	 * </pre>
	 * Logs the specified exception and trace into the log file, and timestamps it with the current time of the match.
	 * @param e the exception to trace into the log.
	 */
	public static synchronized void logTrace(Exception e) {
		StackTraceElement[] stack = e.getStackTrace();
		
		logError(e.toString());
		for (StackTraceElement s : stack)
			log("			at " + s.toString());
	}
	
	/**
	 * <pre>
	 * public static synchronized void close()
	 * </pre>
	 * Closes all the writers and releases the resources related to them.
	 */
	public static synchronized void close() {
		try {
			writer.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
}
