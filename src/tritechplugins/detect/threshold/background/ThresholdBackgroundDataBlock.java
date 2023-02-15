package tritechplugins.detect.threshold.background;

import PamguardMVC.background.BackgroundDataBlock;
import PamguardMVC.background.BackgroundManager;

public class ThresholdBackgroundDataBlock extends BackgroundDataBlock<ThresholdBackgroundDataUnit> {

	public ThresholdBackgroundDataBlock(BackgroundManager backgroundManager) {
		super(ThresholdBackgroundDataUnit.class, backgroundManager);
	}

}
