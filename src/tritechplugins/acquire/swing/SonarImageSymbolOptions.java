package tritechplugins.acquire.swing;

import java.io.Serializable;

import PamView.ColourArray.ColourArrayType;
import PamView.symbol.PamSymbolOptions;

public class SonarImageSymbolOptions extends PamSymbolOptions implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public ColourArrayType colourMap = ColourArrayType.HOT;
	
	public int displayGain = 1;
	
	public boolean showGrid = true;
	
	public boolean showImage = true;
	
	public int transparency;

	public SonarImageSymbolOptions() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SonarImageSymbolOptions clone() {
		try {
			return (SonarImageSymbolOptions) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
