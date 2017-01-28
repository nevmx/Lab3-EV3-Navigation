/*
 * code inspired by
 * http://stackoverflow.com/questions/26816086/measuring-ev3-color-sensor-reflected-light-intensity-via-lejos
 */

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

public class OdoColorSensor extends Thread{

    // Modes and samples are explained in LeJOS wiki:
    // http://sourceforge.net/p/lejos/wiki/Sensor%20Framework/
    private static Port colorSensorPort = SensorPort.S1;
    private static EV3ColorSensor colorSensor;
    private static SampleProvider sampleProvider;
    private static int sampleSize;
    private float[] sampleArray;
    public static double sample=2.0;	//public so we can access within OdometryCorrection


    public void run() {
        // Initializes the sensor & sensor mode
        colorSensor = new EV3ColorSensor(colorSensorPort);
        sampleProvider = colorSensor.getRedMode();
        sampleSize = sampleProvider.sampleSize();
		

    	while(true){
            // Initializes the array for holding samples
    		sampleArray = new float[sampleSize];
            // Gets the sample an returns it
            sampleProvider.fetchSample(sampleArray, 0);
            sample=(double)sampleArray[0];
    		//try { Thread.sleep(50); } catch(Exception e){}	
    }
    }
}