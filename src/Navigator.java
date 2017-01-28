import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;

public class Navigator extends Thread {
	
	private boolean isNavigating;
	private Odometer odometer;
	
	private Queue<Waypoint> waypoints;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3MediumRegulatedMotor usMotor;
	private double wheelRadius;
	private double wheelBase;
	
	private boolean interrupted;
	
	//Temp
	public double tempCurrentHeading;
	public double tempNextHeading;
	public double tempRotAngle;
		
	public Navigator(Odometer o, Queue<Waypoint> w, EV3MediumRegulatedMotor usMotor) {
		odometer = o;
		waypoints = w;
		
		leftMotor = odometer.getLeftMotor();
		rightMotor = odometer.getRightMotor();
		this.usMotor = usMotor;
		wheelRadius = odometer.WR;
		wheelBase = odometer.WB;
		
		usMotor.setSpeed(100);
		usMotor.setAcceleration(2000);
		usMotor.rotateTo(0);
		
		interrupted = false;
	}
	
	private void travelTo(double nextX, double nextY) {
		// Compute the heading
		double currentX = odometer.getX();
		double currentY = odometer.getY();
		
		// Source: http://math.stackexchange.com/questions/1596513/find-the-bearing-angle-between-two-points-in-a-2d-space
		double nextHeading = Math.toDegrees(Math.atan2(nextX - currentX, nextY - currentY));
		if (nextHeading < 0) {
			nextHeading += 360;
		}
		
		// Turn to next heading
		turnTo(nextHeading);
		
		// Calculate the distance
		double distance = Math.sqrt(Math.pow(nextY-currentY, 2) + Math.pow(nextX - currentX,2));
		
		// Set speed for straight line travel
		leftMotor.setSpeed(300);
		rightMotor.setSpeed(300);
		
		// Travel to next waypoint
		leftMotor.rotate(SquareDriver.convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(SquareDriver.convertDistance(wheelRadius, distance), false);
	}
	
	private void turnTo(double theta) {
		// Set speed for rotation
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		
		double currentHeading = odometer.getTheta();
		this.tempCurrentHeading = currentHeading;
		
		// Determine rotation angle and direction
		// Source: http://stackoverflow.com/questions/1878907/the-smallest-difference-between-2-angles
		double rotationAngle = theta - currentHeading;
		if (rotationAngle > 180)
			rotationAngle -= 360;
		else if (rotationAngle < -180)
			rotationAngle += 360;
		
		this.tempRotAngle = rotationAngle;

		if (rotationAngle > 0) {
			leftMotor.rotate(SquareDriver.convertAngle(wheelRadius, wheelBase, rotationAngle), true);
			rightMotor.rotate(-SquareDriver.convertAngle(wheelRadius, wheelBase, rotationAngle), false);
		} else {
			leftMotor.rotate(-SquareDriver.convertAngle(wheelRadius, wheelBase, -rotationAngle), true);
			rightMotor.rotate(SquareDriver.convertAngle(wheelRadius, wheelBase, -rotationAngle), false);
		}
	}

	boolean isNavigating() {
		return this.isNavigating;
	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(750);
		}
		
		this.isNavigating = true;
		
		while (!waypoints.isEmpty()) {
						
			// Dequeue the next waypoint
			Waypoint nextWaypoint = waypoints.poll();
			
			travelTo(nextWaypoint.getX(), nextWaypoint.getY());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (interrupted) {
				interrupted = false;
				
				followWall(nextWaypoint);
				
				break;
			}
		}
		
		this.isNavigating = false;
	}
	
	private void followWall(Waypoint nextWaypoint) {
		// Follow the wall now.
		// nextWaypoint was the waypoint when navigation was interrupted
		// The remaining waypoints are still in the waypoints Queue
		Sound.twoBeeps();
		usMotor.rotateTo(75);
		usMotor.stop();
		
		// Turn right to avoid obstacle
		turnTo(odometer.getTheta() + 90);
		
		// Start following the wall
		BangBangController bbc = new BangBangController(leftMotor, rightMotor, 20, 3, 100, 200);
	}
	
	public void interrupt() {
		leftMotor.stop(true);
		rightMotor.stop();
		this.interrupted = true;
	}
}
