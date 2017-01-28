import lejos.hardware.Sound;

/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	public static OdoColorSensor colorSensor = new OdoColorSensor();
	public static double measuredX = 0.0, measuredY = 0.0, displayedX = 0.0, displayedY = 0.0;
	private static final double ONE_TILE = 30.48;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {

		colorSensor.start(); //initialize the color sensor
		long correctionStart, correctionEnd;
		
		Sound.setVolume(50);
		
		displayedX = odometer.getX();
		displayedY = odometer.getY();

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO: put your correction code here
			if(colorSensor.sample < 0.2){
				//if the color being reported is the black line
				Sound.beep();
				if(odometer.getOrientation().equals("+y")){
					//if the robot is traveling in the positive Y direction
					if(measuredY == 0.0){
						//add the length of one half tile
						measuredY += ONE_TILE/2;
						displayedY = odometer.getY();
					}
					else{
						measuredY += ONE_TILE;
						odometer.setY(displayedY + ONE_TILE);
						displayedY = odometer.getY();
					}
				}
				else if (odometer.getOrientation().equals("+x")) {
					if ( measuredX == 0.0 ){
						measuredY += ONE_TILE/2;								//approximate y position
						displayedY = odometer.getY();
						measuredX += ONE_TILE/2;								//approximate x position
						displayedX = odometer.getX();
					}
					else{
						measuredX += ONE_TILE;								//approximate x position
						odometer.setX(displayedX + ONE_TILE);				//correct odometer x
						displayedX = odometer.getX();
					}
				}
				else if (odometer.getOrientation().equals("-y")){
					if ( measuredY == ONE_TILE*3 ){
						measuredX += ONE_TILE/2;								//approximate x position
						displayedX = odometer.getX();
						measuredY -= ONE_TILE/2;								//approximate y position
						displayedY = odometer.getY();
					}
					else{
						measuredY -= ONE_TILE;								//approximate y position
						odometer.setY(displayedY - ONE_TILE);				//correct odometer y
						displayedY = odometer.getY();
					}
				}
				else if (odometer.getOrientation().equals("-x")){

					if ( measuredX == ONE_TILE*3 ){
						measuredY -= ONE_TILE/2;								//approximate y position
						displayedY = odometer.getY();
						measuredX -= ONE_TILE/2;								//approximate x position
						displayedX = odometer.getX();
					}
					else{
						measuredX -= ONE_TILE;								//approximate x position
						odometer.setX(displayedX - ONE_TILE);				//correct odometer x
						displayedX = odometer.getX();
					}
				}
			}
			
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}