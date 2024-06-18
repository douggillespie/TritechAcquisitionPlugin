package tritechplugins.detect.threshold;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import tritechplugins.display.swing.overlays.SonarSymbolManager;

public class RegionDataBlock extends PamDataBlock<RegionDataUnit> {

	private Set<Integer> sonarIds = Collections.synchronizedSortedSet(new TreeSet<Integer>());
	
	public RegionDataBlock(String dataName, PamProcess thresholdProcess) {
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

	/**
	 * @return the sonarIds
	 */
	public Set<Integer> getSonarIds() {
		return sonarIds;
	}

	/**
	 * Get a collection of sonar id's. 
	 * @param sonarIds the sonarIds to set
	 */
	public void setSonarIds(Set<Integer> sonarIds) {
		this.sonarIds = sonarIds;
	}


}
