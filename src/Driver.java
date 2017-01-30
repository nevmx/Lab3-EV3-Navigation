/*
 * SquareDriver.java
 */

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Driver {
	static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}