import java.util.concurrent.Callable;

import lejos.hardware.Sound;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class ObstacleWatcher extends Thread {
	
	private final static int OBJECT_DISTANCE = 15;
	private final static int BEEP_VOLUME = 30;
	
	private Callable<Void> callback;
	
	private SampleProvider us;
	private float[] usData;
	private Port usPort;
	
	private boolean watching;
	
	private SensorModes usSensor;
		
	private int distance;
	
	public ObstacleWatcher(Port usPort, Callable<Void> callback) {
		this.callback = callback;
		this.usPort = usPort;
		
		watching = true;
		
		usSensor = new EV3UltrasonicSensor(this.usPort);
		us = usSensor.getMode("Distance");
		usData = new float[us.sampleSize()];
		
		Sound.setVolume(BEEP_VOLUME);
	}
	
	@Override
	public void run() {
		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);
			
			// If the object is in watching mode, call the callback function 
			if (distance < OBJECT_DISTANCE && watching) {
				try {
					callback.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
				watching = false;
			}
			
			try { Thread.sleep(50); } catch(Exception e){}
		}
	}
	
	public int getDistance() {
		return this.distance;
	}
}
