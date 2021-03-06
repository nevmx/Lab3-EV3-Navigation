import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;

public class Navigator extends Thread {
	
	final static int STRAIGHT_LINE_TRAVEL_SPEED = 300;
	final static int TURN_SPEED = 75;
	final static int MOTOR_ACCELERATION = 300;
	
	final static int US_MOTOR_SPEED = 100;
	final static int US_MOTOR_ACCELERATION = 2000;
	
	final static int WF_BANDCENTER = 10;			// Offset from the wall (cm)
	final static int WF_BANDWIDTH = 3;				// Width of dead band (cm)
	final static int WF_MOTOR_LOW = 100;			// Speed of slower rotating wheel (deg/sec)
	final static int WF_MOTOR_HIGH = 200;
	final static int WF_FINAL_ANGLE_THRESHOLD = 70;
	
	private boolean isNavigating;
	private Odometer odometer;
	
	private Queue<Waypoint> waypoints;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3MediumRegulatedMotor usMotor;
	private double wheelRadius;
	private double wheelBase;
	
	private ObstacleWatcher ow;
	
	private boolean interrupted;
		
	public Navigator(Odometer o, Queue<Waypoint> w, EV3MediumRegulatedMotor usMotor) {
		odometer = o;
		waypoints = w;
		
		leftMotor = odometer.getLeftMotor();
		rightMotor = odometer.getRightMotor();
		this.usMotor = usMotor;
		wheelRadius = odometer.WR;
		wheelBase = odometer.WB;
		
		usMotor.setSpeed(US_MOTOR_SPEED);
		usMotor.setAcceleration(US_MOTOR_ACCELERATION);
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
		leftMotor.setSpeed(STRAIGHT_LINE_TRAVEL_SPEED);
		rightMotor.setSpeed(STRAIGHT_LINE_TRAVEL_SPEED);
		
		// Travel to next waypoint
		leftMotor.rotate(Driver.convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(Driver.convertDistance(wheelRadius, distance), false);
	}
	
	private void turnTo(double theta) {
		// Set speed for rotation
		leftMotor.setSpeed(TURN_SPEED);
		rightMotor.setSpeed(TURN_SPEED);
		
		double currentHeading = odometer.getTheta();
		
		// Determine rotation angle and direction
		// Source: http://stackoverflow.com/questions/1878907/the-smallest-difference-between-2-angles
		double rotationAngle = theta - currentHeading;
		if (rotationAngle > 180)
			rotationAngle -= 360;
		else if (rotationAngle < -180)
			rotationAngle += 360;
		
		if (rotationAngle > 0) {
			leftMotor.rotate(Driver.convertAngle(wheelRadius, wheelBase, rotationAngle), true);
			rightMotor.rotate(-Driver.convertAngle(wheelRadius, wheelBase, rotationAngle), false);
		} else {
			leftMotor.rotate(-Driver.convertAngle(wheelRadius, wheelBase, -rotationAngle), true);
			rightMotor.rotate(Driver.convertAngle(wheelRadius, wheelBase, -rotationAngle), false);
		}
	}

	boolean isNavigating() {
		return this.isNavigating;
	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(MOTOR_ACCELERATION);
		}
		
		this.isNavigating = true;
		
		while (!waypoints.isEmpty()) {
						
			// Dequeue the next waypoint
			Waypoint nextWaypoint = waypoints.poll();
			
			travelTo(nextWaypoint.getX(), nextWaypoint.getY());
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Navigation was asked to be interrupted by another thread.
			// The thread executing this should now be wall following.
			if (interrupted) {
				interrupted = false;
				
				followWall();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// Continue traveling
				travelTo(nextWaypoint.getX(), nextWaypoint.getY());
			}
		}
		
		this.isNavigating = false;
	}
	
	/*
	 * Follow the wall. At this point, the robot stopped around 10 cm away from the wall
	 * We need to turn right and follow the wall. After this method ends, the robot will
	 * continue following waypoints.
	 */
	private void followWall() {
		// Follow the wall now.
		// nextWaypoint was the waypoint when navigation was interrupted
		// The remaining waypoints are still in the waypoints Queue
		Sound.twoBeeps();
		usMotor.rotateTo(75);
		usMotor.stop();
		
		// Turn right to avoid obstacle
		turnTo(odometer.getTheta() + 90.0);
		
		// Start following the wall
		double finalAngle = odometer.getTheta() - 180.0;
				
		// Follow the wall until you make a full 180 turn
		while (Math.abs(odometer.getTheta() - finalAngle) > WF_FINAL_ANGLE_THRESHOLD) {
			
			int distance = ow.getDistance();
			
			// Right turn
			if (distance < (WF_BANDCENTER - WF_BANDWIDTH)) {
				leftMotor.setSpeed(WF_MOTOR_LOW);
				rightMotor.setSpeed(0);
			}
			
			// Left turn
			else if (distance > (WF_BANDCENTER + WF_BANDWIDTH)) {
				leftMotor.setSpeed((int)(WF_MOTOR_LOW*1.35));
				rightMotor.setSpeed(WF_MOTOR_HIGH);
			}
			
			// Straight
			else {
				leftMotor.setSpeed(WF_MOTOR_HIGH);
				rightMotor.setSpeed(WF_MOTOR_HIGH);
			}
			
			leftMotor.forward();
			rightMotor.forward();
			
			// Pause for 100 ms
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * This was needed to reduce slip/increase odometer precision.
		 */
		leftMotor.setSpeed(WF_MOTOR_LOW);
		rightMotor.setSpeed(WF_MOTOR_LOW);
		
		// Give the robot time to stabilize
		try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Stop - wall following needs to stop.
		leftMotor.stop(true);
		rightMotor.stop();
		
		
		// Rotate sensor back to straight.
		usMotor.rotateTo(0);
		usMotor.stop();
		
		// Now we need to get back on track.
	}
	
	/*
	 * Interrupt Waypoint following and stop. An instance of ObstacleWatcher is passed
	 * so that it can be used to gather distance data to follow the wall.
	 */
	public void interrupt(ObstacleWatcher ow) {
		// Calling .stop() causes the thread that is currently navigating to stop.
		leftMotor.stop(true);
		rightMotor.stop();
		this.interrupted = true;
		
		this.ow = ow;
	}
}
