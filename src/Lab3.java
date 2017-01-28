// Lab3.java

import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

public class Lab3 {
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3MediumRegulatedMotor usMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	
	private static final Port usPort = LocalEV3.get().getPort("S2");

	// Constants
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 9.25; 		//changed this value to measured distance between wheel centers

	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor, rightMotor,WHEEL_RADIUS,TRACK);
		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer);
		
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
		
		NavigationManager nm = new NavigationManager(odometer, usPort, usMotor);

		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" Part  | Part   ", 0, 2);
			t.drawString("  1    |  2     ", 0, 3);
			t.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			odometer.start();
			odometryDisplay.start();
			
			nm.runPartOne();
			
		} else {
			odometer.start();
			odometryDisplay.start();
			
			nm.runPartTwo();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}