package tritechplugins.detect;

import java.util.HashMap;

import PamController.PamControlledUnit;
import PamController.PamController;

public class ThresholdDetector extends PamControlledUnit {
	
	private ThresholdProcess thresholdProcess;
	
	private ThresholdParams thresholdParams = new ThresholdParams();
	

	public static final String unitType = "Gemini Threshold Detector";
	public ThresholdDetector(String unitName) {
		super(unitType, unitName);
		
		thresholdProcess = new ThresholdProcess(this);
		addPamProcess(thresholdProcess);
		
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			thresholdProcess.prepareProcess();
			break;
		}
	}
	
	/**
	 * @return the thresholdParams
	 */
	public ThresholdParams getThresholdParams() {
		return thresholdParams;
	}
	/**
	 * @param thresholdParams the thresholdParams to set
	 */
	public void setThresholdParams(ThresholdParams thresholdParams) {
		this.thresholdParams = thresholdParams;
	}

}
