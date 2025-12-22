package tritechplugins.detect.threshold;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import PamController.PamController;
import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.display.swing.overlays.SonarSymbolManager;

public class RegionDataBlock extends PamDataBlock<RegionDataUnit> {

	private Set<Integer> sonarIds = Collections.synchronizedSortedSet(new TreeSet<Integer>());
	private PamProcess thresholdProcess;
	
	public RegionDataBlock(String dataName, PamProcess thresholdProcess) {
		super(RegionDataUnit.class, dataName, thresholdProcess, 0);
		this.thresholdProcess = thresholdProcess;
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

	@Override
	public void clearAll() {
		// TODO Auto-generated method stub
		super.clearAll();
	}

	/**
	 * @return the thresholdProcess
	 */
	public PamProcess getThresholdProcess() {
		return thresholdProcess;
	}
	
	private TritechAcquisition tritechAcquisition;
	
	@Override
	public void noteNewSettings() {
		super.noteNewSettings();
		tritechAcquisition = null; // reset now and then to handle config changes. 
	}

	/**
	 * find the tritech acquisition. It may not exist !
	 * no direct link to it, so search and remember. 
	 * @return
	 */
	private TritechAcquisition findTritechAcquisition() {
		if (tritechAcquisition != null) {
			return tritechAcquisition;
		}
		// therwise search Will go wrong if the module is removed and added again, but can't handle that easily. 
		tritechAcquisition = (TritechAcquisition) PamController.getInstance().findControlledUnit(TritechAcquisition.unitType);
		return tritechAcquisition;
	}

	/**
	 * Get the sonar position data. This is a bit messy since we have
	 * to find the tritech Acquisition process first and there is no direct link
	 * to it. 
	 * @param sonarId
	 * @return
	 */
	public SonarPosition getSonarPosition(int sonarId) {
		TritechAcquisition tritechDaq = findTritechAcquisition();
		if (tritechDaq == null) {
			return null;
		}
		return tritechDaq.getDaqParams().getSonarPosition(sonarId);
	}

}
