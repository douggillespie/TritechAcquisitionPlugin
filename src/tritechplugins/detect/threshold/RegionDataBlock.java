package tritechplugins.detect.threshold;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class RegionDataBlock extends PamDataBlock<RegionDataUnit> {

	public RegionDataBlock(String dataName, ThresholdProcess thresholdProcess) {
		super(RegionDataUnit.class, dataName, thresholdProcess, 0);
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}


}
