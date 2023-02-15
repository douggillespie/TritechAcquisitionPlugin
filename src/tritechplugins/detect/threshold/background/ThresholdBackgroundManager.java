package tritechplugins.detect.threshold.background;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.background.BackgroundBinaryWriter;
import PamguardMVC.background.BackgroundDataBlock;
import PamguardMVC.background.BackgroundManager;

public class ThresholdBackgroundManager extends BackgroundManager {

	private ThresholdBackgroundWriter thresholdBackgroundWriter;
	
	private ThresholdBackgroundDataBlock thresholdBackgroundDataBlock;
	
	public ThresholdBackgroundManager(PamProcess detectorProcess, PamDataBlock detectorDataBlock) {
		super(detectorProcess, detectorDataBlock);
		thresholdBackgroundDataBlock = new ThresholdBackgroundDataBlock(this);
		thresholdBackgroundWriter = new ThresholdBackgroundWriter(this);
	}

	@Override
	public BackgroundDataBlock getBackgroundDataBlock() {
		return thresholdBackgroundDataBlock;
	}

	@Override
	public BackgroundBinaryWriter getBackgroundBinaryWriter() {
		return thresholdBackgroundWriter;
	}

}
