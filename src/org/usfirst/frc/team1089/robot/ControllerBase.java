package org.usfirst.frc.team1089.robot;

//import edu.wpi.first.wpilibj.Joystick;

/**
 * The {@code ControllerBase} class handles all input coming from the 
 * gamepad, left joystick, and right joystick. This has various methods
 * to get input, buttons, etc.
 */
public class ControllerBase {
	/*private final Joystick GAMEPAD, LEFT_STICK, RIGHT_STICK;
	private boolean[] pressed;*/
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
		GAMEPAD,
		LEFT_STICK,
		RIGHT_STICK
	}
	
	/*public ControllerBase(int gp, int l, int r) {
		GAMEPAD = new Joystick(gp);
		LEFT_STICK = new Joystick(l);
		RIGHT_STICK = new Joystick(r);
		pressed = new boolean[MAX_NUMBER_BUTTONS];
	}
	
	public void update() {
		for (int i = 0; i < pressed.length; i++) {
			pressed[i] = GAMEPAD.getRawButton(i);
		}
	}
	
	public boolean getPressedDown(int b) {
		return GAMEPAD.getRawButton(b) && !pressed[b];
	}*/
	
}
