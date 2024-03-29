package tritechplugins.detect.threshold;

import java.util.HashSet;
import java.util.Set;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tritechplugins.display.swing.overlays.SonarSymbolManager;

public class RegionDataBlock extends PamDataBlock<RegionDataUnit> {

	private Set<Integer> sonarIds = new HashSet<Integer>();
	
	public RegionDataBlock(String dataName, ThresholdProcess thresholdProcess) {
		super(RegionDataUnit.class, dataName, thresholdProcess, 0);
		setNaturalLifetimeMillis(2000);
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

	@Override
	public void addPamData(RegionDataUnit pamDataUnit, Long uid) {
		super.addPamData(pamDataUnit, uid);
		sonarIds.add(pamDataUnit.getSonarId());
	}

	public int sonarIdToChannelMap(int sonarId) {
		return sonarId;
	}

}
