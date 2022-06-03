package tritechplugins.display.swing.overlays;

import java.io.Serializable;

import PamView.paneloverlay.OverlayDataInfo;
import PamguardMVC.PamDataBlock;

public class SonarOverlayData extends OverlayDataInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public SonarOverlayData(String dataBlockName) {
		super(dataBlockName);
	}


}
