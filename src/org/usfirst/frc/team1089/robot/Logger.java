package org.usfirst.frc.team1089.robot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.wpi.first.wpilibj.DriverStation;

public class Logger {
	private File log;
	private FileWriter out;
	private PrintWriter writer;
	private DriverStation ds;
	
	public Logger(String location) {
		if (log == null) {
			try {
				int iteration = 1;
				location += "logger";
				
				log = new File(location + ".txt");
				
				while(log.exists()) {
					log = new File(location + iteration + ".txt");
					iteration++;
				}
				
				log.createNewFile();
				
				out = new FileWriter(log.getAbsolutePath());
				writer = new PrintWriter(out);
			} catch (Exception e) { }
			
			ds = DriverStation.getInstance();
		}
	}
	
	public void log(String input){
		try {
			writer.println("[" + ds.getMatchTime() + "]: " + input);
		} catch (Exception e) { }
	}
	
	public void logWarning(String warn) {
		log("WARN: " + warn);
	}
	
	public void logError(String error) {
		log("ERROR: " + error);
	}
	
	public void logTrace(Exception e) {
		StackTraceElement[] stack = e.getStackTrace();
		
		logError(e.toString());
		for (StackTraceElement s : stack)
			log("			at " + s.toString());
	}
	
	public void close() {
		try {
			writer.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
