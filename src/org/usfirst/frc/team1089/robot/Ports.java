package org.usfirst.frc.team1089.robot;

public class Ports {

	public static class Analog {
		public static final int GYRO = 0;
	}

	public static class Digital {
		public static final int CHECK_PRESSURE = 0;
		public static final int COMPRESSOR_RELAY = 0;
	}

	public static class CAN {
		public static final int LEFT_FRONT_TALON_ID = 4;
		public static final int RIGHT_FRONT_TALON_ID = 2;
		public static final int LEFT_BACK_TALON_ID = 3;
		public static final int RIGHT_BACK_TALON_ID = 1;
		public static final int INTAKE_TALON_ID = 7;
		public static final int PCM_ID = 6;
	}

	public static class PWM {

	}

	public static class USB {
		public static final int LEFT_STICK = 1;
		public static final int RIGHT_STICK = 0;
		public static final int GAMEPAD = 2;
	}

	public static class PCM {
		public static final int SHOOTER = 1;
		public static final int SHOOTER_ELEVATOR_FORWARD = 2;
		public static final int SHOOTER_ELEVATOR_REVERSE = 3;
		public static final int INTAKE_ELEVATOR_FORWARD = 4;
		public static final int INTAKE_ELEVATOR_REVERSE = 5;
	}
}
