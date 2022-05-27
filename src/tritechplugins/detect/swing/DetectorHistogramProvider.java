package tritechplugins.detect.swing;

import tritechplugins.detect.ThresholdDetector;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class DetectorHistogramProvider implements UserDisplayProvider{

	private ThresholdDetector thresholdDetector;

	public DetectorHistogramProvider(ThresholdDetector thresholdDetector) {
		this.thresholdDetector = thresholdDetector;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return thresholdDetector.getUnitName() + " histograms";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new DetectorHistogramPanel(thresholdDetector, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return DetectorHistogramPanel.class;
	}

	@Override
	public int getMaxDisplays() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		
	}

}
