package tritechplugins.detect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class RegionDataBlock extends PamDataBlock<RegionDataUnit> {

	public RegionDataBlock(String dataName, ThresholdProcess thresholdProcess) {
		super(RegionDataUnit.class, dataName, thresholdProcess, 0);
	}


}
