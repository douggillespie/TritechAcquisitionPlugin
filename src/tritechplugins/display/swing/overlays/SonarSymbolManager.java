package tritechplugins.display.swing.overlays;

import PamView.GeneralProjector;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import tritechplugins.detect.swing.RegionOverlayDraw;
import tritechplugins.detect.threshold.RegionDataBlock;

public class SonarSymbolManager extends StandardSymbolManager {
	
	private RegionDataBlock regionDataBlock;

	public SonarSymbolManager(RegionDataBlock regionDataBlock) {
		super(regionDataBlock, RegionOverlayDraw.defaultSymbol.getSymbolData());
		this.regionDataBlock = regionDataBlock;
	}

	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new SonarSymbolChooser(this, regionDataBlock, displayName, getDefaultSymbol(), projector);
	}

}
