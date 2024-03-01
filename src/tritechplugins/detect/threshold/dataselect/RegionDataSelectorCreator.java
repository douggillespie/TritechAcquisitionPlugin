package tritechplugins.detect.threshold.dataselect;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import tritechplugins.detect.threshold.RegionDataBlock;

public class RegionDataSelectorCreator extends DataSelectorCreator {

	private RegionDataBlock regionDataBlock;

	public RegionDataSelectorCreator(RegionDataBlock regionDataBlock) {
		super(regionDataBlock);
		this.regionDataBlock = regionDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new RegionDataSelector(regionDataBlock, selectorName);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return super.getSettingsReference();
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return super.restoreSettings(pamControlledUnitSettings);
	}

}
