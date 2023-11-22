package tritechplugins.acquire.swing.framerate;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class FrameRateDataBlock extends PamDataBlock<FrameRateDataUnit> {

	public FrameRateDataBlock(PamProcess parentProcess) {
		super(FrameRateDataUnit.class, "Frame Rate", parentProcess, 0);
	}

}
