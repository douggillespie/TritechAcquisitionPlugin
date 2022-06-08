package tritechplugins.display.swing.overlays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.OverlayDataManager;
import PamguardMVC.PamDataBlock;
import tritechplugins.display.swing.SonarsPanel;

public class SonarOverlayManager extends OverlayDataManager<SonarOverlayData> {

	private static ParameterType[] paramTypes = {ParameterType.X, ParameterType.Y};
	private static ParameterUnits[] paramUnits = {ParameterUnits.METERS, ParameterUnits.METERS};
	
	private SonarsPanel sonarsPanel;
	
	public SonarOverlayManager(SonarsPanel sonarsPanel) {
		super(paramTypes, paramUnits);
		this.sonarsPanel = sonarsPanel;
		
	}

	@Override
	public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
		SonarOverlayData overlayData = getOverlayInfo(dataBlock);
		overlayData.select = selected;
	}

	@Override
	public String getDataSelectorName() {
		return sonarsPanel.getDataSelectorName();
	}

	@Override
	public SonarOverlayData getOverlayInfo(PamDataBlock dataBlock) {
		HashMap<String, SonarOverlayData> overlayDatas = getOverlayData();
		SonarOverlayData overlayData = overlayDatas.get(dataBlock.getLongDataName());
		if (overlayData == null) {
			overlayData = new SonarOverlayData(dataBlock.getLongDataName());
			overlayDatas.put(dataBlock.getLongDataName(), overlayData);
		}
		return overlayData;
	}

	private HashMap<String, SonarOverlayData> getOverlayData() {
		return sonarsPanel.getSonarsPanelParams().getOverlayDatas();
	}
	/**
	 * Get a list of selected datablocks. 
	 * @return
	 */
	public Collection<SonarOverlayData> getSelectedDataBlocks() {
		HashMap<String, SonarOverlayData> overlayDatas = getOverlayData();
		Collection<SonarOverlayData> values = overlayDatas.values();
		ArrayList<SonarOverlayData> selected = new ArrayList<>();
		for (SonarOverlayData aData : values) {
			if (aData.select) {
				selected.add(aData);
			}
		}
		return selected;
	}

	@Override
	protected GeneralProjector getProjector() {
		return sonarsPanel.getFirstXYProjector();
	}

}
