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
	private static boolean is_logging = false;
	
	private static final SimpleDateFormat ISO8601;
	
	static {
		ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSXXX");
	}
	
	/**
	 * <pre>
	 * public synchronized static void init(String location)
	 * </pre>
	 * Initializes the current {@code Logger} with the file at the specified location.
	 */
	public static synchronized void init() {
		if (log == null) {
			try {
				File path;
				log = new File("home/lvuser/log/log_" + ISO8601.format(Calendar.getInstance().getTime()) + ".txt");
				path = log.getParentFile();
				
				if (!path.exists())
					path.mkdirs();
				
				if (!log.exists())
					log.createNewFile();
				
				out = new FileWriter(log.getAbsolutePath());
				writer = new PrintWriter(out);
				ds = DriverStation.getInstance();
				is_logging = true;
			} catch (Exception e) { 
				e.printStackTrace(System.out);
			}
		}
	}
	
	/**
	 * <pre>
	 * public static synchronized void log(Object... input)
	 * </pre>
	 * Logs the specified input into the log file, separating elements by tabs.
	 * Log entries are prefixed with the current match time and current system time.
	 * 
	 * @param input the text to put into the log.
	 */
	public static void log(Object... input){
		if (is_logging) {
			// Get the time outside of synchronized code so it captures most accurately the time when log() is invoked
			String time_prefix = "[" + ds.getMatchTime() + "]:" + "[" + ISO8601.format(Calendar.getInstance().getTime()) + "]: ";
			
			synchronized(Logger.class) {
				// Check is_logging again in case it changed since the lock was acquired
				if (is_logging) {
					try {
						String out = "";
						for (Object o : input)
							out += o.toString() + '\t';
						writer.println(time_prefix + out);
					} catch (Exception e) { 
						e.printStackTrace(System.out);
					}
				}
			}
		}
	}
	
	/**
	 * <pre>
	 * public static synchronized void logWarning(String warn)
	 * </pre>
	 * Logs the specified warning into the log file, and timestamps it with the current time of the match.
	 * @param warning the warning to put into the log.
	 */
	public static void logWarning(String warning) {
		log("WARNING: " + warning);
	}
	
	/**
	 * <pre>
	 * public static synchronized void logError(String error)
	 * </pre>
	 * Logs the specified error into the log file, and timestamps it with the current time of the match.
	 * @param error the error to put into the log.
	 */
	public static void logError(String error) {
		log("ERROR: " + error);
	}
	
	/**
	 * <pre>
	 * public static synchronized void logTrace(Exception e)
	 * </pre>
	 * Logs the specified exception and trace into the log file, and timestamps it with the current time of the match.
	 * @param e the exception to trace into the log.
	 */
	public static void logTrace(Exception e) {
		StackTraceElement[] stack = e.getStackTrace();
		String msg = e.toString();
		for (StackTraceElement s : stack)
			msg += "			at " + s.toString();
		logError(msg);
	}
	
	/** 
	 * <pre>
	 * public static synchronized void close()
	 * </pre>
	 * Closes all the writers and releases the resources related to them.
	 */
	public static synchronized void close() {
		try {
			if(writer != null && out != null){			
				is_logging = false;
				writer.close();
				out.close();
				log = null;
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
