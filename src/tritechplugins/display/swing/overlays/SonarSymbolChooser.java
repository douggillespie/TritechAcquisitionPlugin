package tritechplugins.display.swing.overlays;

import java.util.ArrayList;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolOptions;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SwingSymbolOptionsPanel;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class SonarSymbolChooser extends StandardSymbolChooser{
	
	private SonarSymbolOptions sonarSymbolOptions = new SonarSymbolOptions();

	public SonarSymbolChooser(SonarSymbolManager sonarSymbolManager, PamDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(sonarSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
		addSymbolModifier(new SequenceSymbolModifier("Sequence (blue to red)", this, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR));
	}

	@Override
	public SonarSymbolOptions getSymbolOptions() {
//		if (sonarSymbolOptions == null) {
//			sonarSymbolOptions = new SonarSymbolOptions();
//		}
		ArrayList<SymbolModifier> modifiers = getSymbolModifiers();
		for (SymbolModifier symbolModifier: modifiers) {
			sonarSymbolOptions.setModifierParams(symbolModifier.getName(), symbolModifier.getSymbolModifierParams());
		}
//		super.setSymbolOptions(sonarSymbolOptions);
//		StandardSymbolOptions symbOpts = super.getSymbolOptions();
		return sonarSymbolOptions;
	}

	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
		if (symbolOptions instanceof SonarSymbolOptions) {
			sonarSymbolOptions = (SonarSymbolOptions) symbolOptions;
			super.setSymbolOptions(symbolOptions);
		}
	}

	@Override
	public SwingSymbolOptionsPanel getSwingOptionsPanel(GeneralProjector projector) {
		return new SonarSymbolPanel(getSymbolManager(), this);
	}

	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return super.getSymbolChoice(projector, dataUnit);
	}

}
