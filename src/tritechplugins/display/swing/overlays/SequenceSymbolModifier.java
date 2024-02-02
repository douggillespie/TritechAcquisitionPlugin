package tritechplugins.display.swing.overlays;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class SequenceSymbolModifier extends SymbolModifier {

	private SymbolData symbolData = new SymbolData();
	
	private ColourArray colourArray = ColourArray.createStandardColourArray(256,ColourArrayType.HOT);
	
	public SequenceSymbolModifier(String name, PamSymbolChooser symbolChooser, int modifyableBits) {
		super(name, symbolChooser, modifyableBits);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		SuperDetection superDet = dataUnit.getSuperDetection(0);
		if (superDet == null) {
			return null;
		}
		int nSub = superDet.getSubDetectionsCount();
		int subIndex = superDet.getSubDetections().indexOf(dataUnit);
		int r = (int) ((double) subIndex / (double) nSub * colourArray.getNumbColours());
		r = Math.max(r,  1);
		r = Math.min(r, colourArray.getNumbColours()-1);
		symbolData.setFillColor(colourArray.getColour(r));
		symbolData.setLineColor(colourArray.getColour(r));
		return symbolData;
	}

}
