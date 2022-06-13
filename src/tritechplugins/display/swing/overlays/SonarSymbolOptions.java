package tritechplugins.display.swing.overlays;

import java.io.Serializable;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import tritechplugins.detect.swing.RegionOverlayDraw;

public class SonarSymbolOptions extends StandardSymbolOptions implements Cloneable, Serializable {

	public SonarSymbolOptions() {
		super(RegionOverlayDraw.defaultSymbol.getSymbolData());
	}

	private static final long serialVersionUID = 1L;

	public static final int DRAW_BOX = 0;
	public static final int DRAW_SYMBOL = 1;

	public int symbolType = DRAW_BOX;

	@Override
	protected SonarSymbolOptions clone() {
			return (SonarSymbolOptions) super.clone();
	}

	public static int[] getSymbolTypes() {
		int[] t = {DRAW_BOX, DRAW_SYMBOL};
		return t;
	}

	public static String getSymbolTypeString(int type) {
		switch (type) {
		case DRAW_BOX:
			return "Shape outline";
		case DRAW_SYMBOL:
			return "Standard symbol";
		default:
			return "Unknown";
		}
	}

}
