package tritechplugins.display.swing.overlays;

import java.awt.Color;

import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.PamDataBlock;

public class TrackSymbolManager extends StandardSymbolManager {

	private static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CROSS, 6, 6, false, Color.RED, Color.RED);
	
	public TrackSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, defaultSymbol);
	}
	
	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		psc.addSymbolModifier(new RotateColoursModifier("Colour randomly", psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR));
//		psc.addSymbolModifier(new SequenceSymbolModifier("Colour Sequentially", psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR));
	}

}
