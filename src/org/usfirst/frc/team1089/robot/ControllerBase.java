package org.usfirst.frc.team1089.robot;

import edu.wpi.first.wpilibj.Joystick;

/**
 * The {@code ControllerBase} class handles all input coming from the 
 * gamepad, left joystick, and right joystick. This has various methods
 * to get input, buttons, etc.
 */
public class ControllerBase {
	private final Joystick GAMEPAD, LEFT_STICK, RIGHT_STICK;
	private boolean[] pressed;
	private final double DEAD_ZONE = 0.1;
	
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
			L2 = 5,
			R2 = 6,
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
	 * The {@code Joysticks} enum contains 
	 * namespaces for the gamepad, left joystick, and right joystick
	 */
	public enum Joysticks {
		GAMEPAD,
		LEFT_STICK,
		RIGHT_STICK
	}
	
	public ControllerBase(int gp, int l, int r) {
		GAMEPAD = new Joystick(gp);
		LEFT_STICK = new Joystick(l);
		RIGHT_STICK = new Joystick(r);
		pressed = new boolean[11];
	}
	
	public void update() {
		for (int i = 0; i < pressed.length; i++) {
			pressed[i] = GAMEPAD.getRawButton(i);
		}
	}
	
	public boolean getPressedDown(int b) {
		return GAMEPAD.getRawButton(b) && !pressed[b];
	}
	
	/**
	 * <pre>
	 * public double getAxis(Joysticks js, 
	 *                       int x)
	 * </pre>
	 * Gets a value between -1 and 1 of the axis from the specified joystick, 
	 * taking into consideration the deadzone.
	 * 
	 * @param js the joystick to get the axis value from
	 * @param x  the axis to get a value from
	 * @return the current value of the axis;
	 *         0 if axis <= deadzone,
	 *         the polar end if |a| >= 1 - deadzone
	 */
	public double getAxis(Joysticks js, int x) {
		double axis = 0;
		
		// Figure out what joystick to get the value from
		switch(js) {
			case LEFT_STICK:
				axis = LEFT_STICK.getRawAxis(x);
				break;
			case RIGHT_STICK:
				axis = RIGHT_STICK.getRawAxis(x);
				break;
			default:
				axis = GAMEPAD.getRawAxis(x);
				break;
		}
		
		// Check deadzones at both polar ends and at 0
		if (Math.abs(axis) <= DEAD_ZONE)
			axis = 0;
		
		if (Math.abs(axis) >= 1 - DEAD_ZONE)
			axis = Math.signum(axis);
			
		return axis;
	}
}