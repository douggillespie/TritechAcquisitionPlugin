package tritechplugins.detect;

import tritechplugins.acquire.ImageDataUnit;

public class ChannelDetector {

	private ThresholdDetector thresholdDetector;
	private ThresholdProcess thresholdProcess;
	private int sonarId;
	
	/**
	 * Detector for a single sonar. 
	 * @param thresholdDetector
	 * @param thresholdProcess
	 */
	public ChannelDetector(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess, int sonarId) {
	}

	/**
	 * New data, either real time or in offline processing. 
	 * @param imageData
	 */
	public void newData(ImageDataUnit imageData) {
		// TODO Auto-generated method stub
		
	}

}
