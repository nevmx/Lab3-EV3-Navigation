import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;

public class NavigationManager {
	private Navigator navigator;
	private Odometer odometer;
	private ObstacleWatcher ow;
	private Port usPort;
	private EV3MediumRegulatedMotor usMotor;
		
	public NavigationManager(Odometer odometer, Port usPort, EV3MediumRegulatedMotor usMotor) {
		this.odometer = odometer;
		this.usPort = usPort;
		
		this.usMotor = usMotor;
	}
	
	public void runPartOne() {
		// Build the list of waypoints
		Queue<Waypoint> waypoints = new LinkedList<Waypoint>();
		waypoints.add(new Waypoint(60, 30));
		waypoints.add(new Waypoint(30, 30));
		waypoints.add(new Waypoint(30, 60));
		waypoints.add(new Waypoint(60, 0));
		
		navigator = new Navigator(odometer, waypoints, usMotor);
		
		navigator.start();
	}
	
	public void runPartTwo() {
		// Initialize the ObstacleWatcher class
		ow = new ObstacleWatcher(usPort, new Callable<Void> () {
			@Override
			public Void call() {
				Sound.beep();
				navigator.interrupt();
				return null;
			}
		});
		
		ow.start();
		
		// Build the list of waypoints
		Queue<Waypoint> waypoints = new LinkedList<Waypoint>();
		waypoints.add(new Waypoint(0, 60));
		waypoints.add(new Waypoint(60, 0));
		
		navigator = new Navigator(odometer, waypoints, usMotor);
		
		navigator.start();
	}
}
