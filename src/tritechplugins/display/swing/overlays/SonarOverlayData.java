package tritechplugins.display.swing.overlays;

import java.io.Serializable;

import PamView.paneloverlay.OverlayDataInfo;
import PamguardMVC.PamDataBlock;

public class SonarOverlayData extends OverlayDataInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	private PamDataBlock dataBlock;

	public SonarOverlayData(PamDataBlock dataBlock) {
		super(dataBlock.getDataName());
		this.dataBlock = dataBlock;
	}

	/**
	 * @return the dataBlock
	 */
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

}
