import lejos.hardware.motor.*;

public class BangBangController {
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.backward();
		rightMotor.backward();
	}
	
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
		
		// Right turn
		if (this.distance < (bandCenter - bandwidth)) {
			leftMotor.setSpeed(motorLow);
			rightMotor.setSpeed(0);
		}
		
		// Left turn
		else if (this.distance > (bandCenter + bandwidth)) {
			leftMotor.setSpeed((int)(motorLow*1.35));
			rightMotor.setSpeed(motorHigh);
		}
		
		// Straight
		else {
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorHigh);
		}
		
		leftMotor.forward();
		rightMotor.forward();
	}

	public int readUSDistance() {
		return this.distance;
	}
}
