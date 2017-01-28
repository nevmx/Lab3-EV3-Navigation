import java.util.concurrent.Callable;

import lejos.hardware.Sound;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class ObstacleWatcher extends Thread {
	
	private Callable<Void> callback;
	
	private SampleProvider us;
	private float[] usData;
	private Port usPort;
	
	private boolean watching;
	private boolean wallFollowing;
	
	private SensorModes usSensor;
	
	public ObstacleWatcher(Port usPort, Callable<Void> callback) {
		this.callback = callback;
		this.usPort = usPort;
		
		watching = true;
		wallFollowing = false;
		
		usSensor = new EV3UltrasonicSensor(usPort);
		us = usSensor.getMode("Distance");
		usData = new float[us.sampleSize()];
		
		Sound.setVolume(30);
	}
	
	@Override
	public void run() {
		int distance;
		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);
			
			if (distance < 15 && watching) {
				try {
					callback.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				watching = false;
			}
			
			if (wallFollowing) {
				// TODO Wall following
			}
			
			try { Thread.sleep(50); } catch(Exception e){}
		}
	}
}
