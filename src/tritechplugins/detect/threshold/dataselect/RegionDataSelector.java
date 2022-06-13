package tritechplugins.detect.threshold.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import tritechplugins.detect.threshold.RegionDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;

public class RegionDataSelector extends DataSelector {
	
	private RegionDataSelectorParams regionDataSelectorParams = new RegionDataSelectorParams();

	public RegionDataSelector(RegionDataBlock regionDataBlock, String selectorName) {
		super(regionDataBlock, selectorName, false);
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof RegionDataSelectorParams) {
			this.regionDataSelectorParams = (RegionDataSelectorParams) dataSelectParams;
		}
	}

	@Override
	public DataSelectParams getParams() {
		if (regionDataSelectorParams == null) {
			regionDataSelectorParams = new RegionDataSelectorParams();
		}
		return regionDataSelectorParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new RegionDataSelectorPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		RegionDataUnit regionDataUnit = (RegionDataUnit) pamDataUnit;
		return 1.;
	}

	/**
	 * @return the regionDataSelectorParams
	 */
	public RegionDataSelectorParams getRegionDataSelectorParams() {
		return regionDataSelectorParams;
	}

	/**
	 * @param regionDataSelectorParams the regionDataSelectorParams to set
	 */
	public void setRegionDataSelectorParams(RegionDataSelectorParams regionDataSelectorParams) {
		this.regionDataSelectorParams = regionDataSelectorParams;
	}


}
