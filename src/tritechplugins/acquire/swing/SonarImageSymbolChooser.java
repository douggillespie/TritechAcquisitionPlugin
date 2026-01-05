package tritechplugins.acquire.swing;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.PamSymbolOptions;
import PamView.symbol.SwingSymbolOptionsPanel;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class SonarImageSymbolChooser extends PamSymbolChooser {
	
	private SonarImageSymbolOptions symbolOptions = new SonarImageSymbolOptions();



	public SonarImageSymbolChooser(PamSymbolManager pamSymbolManager, PamDataBlock pamDataBlock, String displayName,
			GeneralProjector projector) {
		super(pamSymbolManager, pamDataBlock, displayName, projector);
	}

	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
		/**
		 * Not used, since never using a standard symbol. The drawing display will 
		 * get the options and use in it's own bespoke way. 
		 */
		return null;
	}

	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
		if (symbolOptions instanceof SonarImageSymbolOptions) {
			this.symbolOptions = (SonarImageSymbolOptions) symbolOptions;
		}
	}


	@Override
	public SonarImageSymbolOptions getSymbolOptions() {
		return symbolOptions;
	}

	@Override
	public SwingSymbolOptionsPanel getSwingOptionsPanel(GeneralProjector projector) {
		return new SonarImageSymbolPanel(this, projector);
	}




}
