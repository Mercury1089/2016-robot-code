package org.usfirst.frc.team1089.robot;

/**
 * The {@code Ports} class contains constants for all the components' connected ports.
 * There are subclasses separating the types of ports for each subset of the robot.
 */
public class Ports {

	/**
	 * The {@code Analog} subclass contains all the ports for anything giving an analog output.
	 * The only thing in this class is the gyro's port.
	 */
	public static class Analog {
		public static final int GYRO = 0;
		public static final int CHECK_PRESSURE = 1;
		public static final int SCALER_POT = 2;
	}

	/**
	 * The {@code Digital} subclass contains all the ports for anything giving an digital output.
	 */
	public static class Digital {
		public static final int CHECK_PRESSURE = 0;
	}
	/**
	 * The {@code Relay} subclass contains all the ports for anything in the relay section.
	 */
	public static class Relay {
		public static final int COMPRESSOR_RELAY = 0;
	}
	/**
	 * The {@code CAN} subclass contains all the ports for anything using the CAN interface.
	 */
	public static class CAN {
		public static final int LEFT_FRONT_TALON_ID = 4;
		public static final int RIGHT_FRONT_TALON_ID = 2;
		public static final int LEFT_BACK_TALON_ID = 3;
		public static final int RIGHT_BACK_TALON_ID = 1;
		public static final int LIFTER_TALON_ID = 8;
		public static final int INTAKE_TALON_ID = 7;
		public static final int ENGAGER_TALON_ID = 9;
		public static final int PCM_ID = 6;
		public static final int PORTCULLIS_LIFT_TALON_ID = 10;
	}

	/**
	 * The {@code USB} subclass contains all the ports for anything using the USB interface.
	 * This only contains the joysticks.
	 */
	public static class USB {
		public static final int LEFT_STICK = 1;
		public static final int RIGHT_STICK = 0;
		public static final int GAMEPAD = 2;
	}

	/**
	 * The {@code PCM} subclass contains all the ports for anything connected to the pneumatics control module (PCM).
	 */
	public static class PCM {
		public static final int SINGLE_SHOOTER = 7;
		public static final int DOUBLE_SHOOTER_FORWARD = 7;
		public static final int DOUBLE_SHOOTER_REVERSE = 0;
		public static final int SHOOTER_ELEVATOR_LOW_FORWARD = 4;
		public static final int SHOOTER_ELEVATOR_LOW_REVERSE = 3;
		public static final int SHOOTER_ELEVATOR_HIGH_FORWARD = 1;
		public static final int SHOOTER_ELEVATOR_HIGH_REVERSE = 2;
		public static final int INTAKE_ELEVATOR_FORWARD = 5;
		public static final int INTAKE_ELEVATOR_REVERSE = 6;
		public static final int LIFT_DEPLOYER_FORWARD = 7;
		public static final int LIFT_DEPLOYER_REVERSE = 8;
	}
}
