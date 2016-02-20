package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.Joystick;

/**
 * The {@code ControllerBase} class handles all input coming from the 
 * gamepad, left joystick, and right joystick. This has various methods
 * to get input, buttons, etc.
 */
public class ControllerBase {
	private static Joystick[] joysticks;
	
	private static boolean[][] btn;
	private static boolean[][] btnPrev;

	public static final int MAX_NUMBER_CONTROLLERS = 3;
	public static final int MAX_NUMBER_BUTTONS = 11; 
	
	/**
	 * The {@code GamepadButtons} class contains all the button bindings for the
	 * Gamepad.
	 */
	public static class GamepadButtons {
		public static final int
			A = 1,
			B = 2,
			X = 3,
			Y = 4,
			LB = 5,
			RB = 6,
			BACK = 7,
			START = 8,
			L3 = 9,
			R3 = 10;
		/**
		 * <pre>
		 * private GamepadButtons()
		 * </pre>
		 * 
		 * Unused constructor.
		 */
		private GamepadButtons() {
			
		}
	}
	/**
	 * The {@code JoystickButtons} class contains all the button bindings for the
	 * Joysticks.
	 */
	public static class JoystickButtons {
		public static final int
			BTN1 = 1,
			BTN2 = 2,
			BTN3 = 3,
			BTN4 = 4,
			BTN5 = 5,
			BTN6 = 6,
			BTN7 = 7,
			BTN8 = 8,
			BTN9 = 9,
			BTN10 = 10,
			BTN11 = 11;
		/**
		 * <pre>
		 * private JoystickButtons()
		 * </pre>
		 * 
		 * Unused constructor.
		 */
		private JoystickButtons() {
			
		}
	}
	
	/**
	 * The {@code Joysticks} enum contains 
	 * namespaces for the gamepad, left joystick, and right joystick
	 */
	public enum Joysticks {
		GAMEPAD,	// 0
		LEFT_STICK,	// 1
		RIGHT_STICK	// 2
	}
	
	public ControllerBase(Joystick gamepad, Joystick leftStick, Joystick rightStick) {		
		btn = new boolean[ControllerBase.MAX_NUMBER_CONTROLLERS][ControllerBase.MAX_NUMBER_BUTTONS];
		btnPrev = new boolean[ControllerBase.MAX_NUMBER_CONTROLLERS][ControllerBase.MAX_NUMBER_BUTTONS];
		
		// CAUTION: joysticks are indexed according to order defined in Joysticks enum
		// Therefore changes in Joysticks enum need to be reflected here...
		joysticks = new Joystick[]{gamepad, leftStick, rightStick};
	}
	
	public void update() {
		//Dealing with buttons on the different joysticks
		for (int i = 0; i < ControllerBase.MAX_NUMBER_CONTROLLERS; i++) {
			for (int j = 1; j < ControllerBase.MAX_NUMBER_BUTTONS; j++) {
				btnPrev[i][j] = btn[i][j];
			}
		}

		for (int i = 0; i < ControllerBase.MAX_NUMBER_CONTROLLERS; i++) {
			for (int j = 1; j < ControllerBase.MAX_NUMBER_BUTTONS; j++) {
				btn[i][j] = joysticks[i].getRawButton(j);
			}
		}
	}	

	public boolean getPressedDown(Joysticks contNum, int buttonNum) {
		return btn[contNum.ordinal()][buttonNum] && !btnPrev[contNum.ordinal()][buttonNum]; 
	}	
}
