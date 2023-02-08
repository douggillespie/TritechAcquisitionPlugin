package tritechplugins.detect.veto;

import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import tritechplugins.detect.veto.swing.VetoOverlayDraw;

public class SpatialVetoDataBlock extends PamDataBlock<SpatialVetoDataUnit> {

	public SpatialVetoDataBlock(PamProcess parentProcess) {
		super(SpatialVetoDataUnit.class, "Spatial Vetos", parentProcess, 0);
		setPamSymbolManager(new StandardSymbolManager(this, VetoOverlayDraw.defaultSymbol.getSymbolData()));
	}

	@Override
	public void clearAll() {
	}

	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		return 0;
	}

	@Override
	protected int removeOldUnitsS(long mastrClockSample) {
		return 0;
	}

	/**
	 * Way of calling clearAll, which has been overridden to do nothing
	 */
	public void removeEverything() {
		super.clearAll();
	}

	

}
