package tritechplugins.display.swing.overlays;

import PamView.symbol.StandardSymbolManager;
import tritechplugins.detect.swing.RegionOverlayDraw;
import tritechplugins.detect.threshold.RegionDataBlock;

public class SonarSymbolManager extends StandardSymbolManager {
	
	public SonarSymbolManager(RegionDataBlock regionDataBlock) {
		super(regionDataBlock, RegionOverlayDraw.defaultSymbol.getSymbolData());
		// TODO Auto-generated constructor stub
	}


}
