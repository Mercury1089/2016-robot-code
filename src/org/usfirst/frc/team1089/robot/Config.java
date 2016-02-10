package org.usfirst.frc.team1089.robot;

public class Config {

	int configType = -1; // Make this an enum
	
	private static Config current = null;
	
	private Config(int confType) {
		switch (confType) {
		case 1:
				break;
		case 2:
			break;
		}
	}
	
	public static void setCurrent(int confType) {
		current = new Config (confType);
	}
	
	public static Config getCurrent() {
		return current;
	}
}
