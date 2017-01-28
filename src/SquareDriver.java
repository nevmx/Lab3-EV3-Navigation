/*
 * SquareDriver.java
 */

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class SquareDriver {
	private static final int FORWARD_SPEED_L = 400;
	private static final int FORWARD_SPEED_R = 400;
	private static final int ROTATE_SPEED_L = 150;
	private static final int ROTATE_SPEED_R = 150;
	public static int orientation = 0;

	public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double leftRadius, double rightRadius, double width) {
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(2950);
		}

		// wait 8 seconds
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}

		for (int i = 0; i < 4; i++) {
			// drive forward two tiles
			leftMotor.setSpeed(FORWARD_SPEED_L);
			rightMotor.setSpeed(FORWARD_SPEED_R);

			leftMotor.rotate(convertDistance(leftRadius, 91.44), true);
			rightMotor.rotate(convertDistance(rightRadius, 91.44), false);

			// turn 90 degrees clockwise
			leftMotor.setSpeed(ROTATE_SPEED_L);
			rightMotor.setSpeed(ROTATE_SPEED_R);

			leftMotor.rotate(convertAngle(leftRadius, width, 90.0), true);
			rightMotor.rotate(-convertAngle(rightRadius, width, 90.0), false);
			
			orientation += 1;
		}
	}

	static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}