package tritechplugins.display.swing;

import java.io.Serializable;

import PamView.ColourArray.ColourArrayType;

/**
 * Parameters controlling layout of the sonars panel. 
 * @author dg50
 *
 */
public class SonarsPanelParams implements Serializable, Cloneable {

	public static final int RESOLUTION_DEFAULT = 0;
	public static final int RESOLUTION_HIGH = 1;
	public static final int RESOLUTION_BEST = 2;
	
	public static final long serialVersionUID = 1L;

	public SonarsLayout sonarsLayout = SonarsLayout.SEPARATE;
	
	public int amplitudeMin = 0;
	
	public int amplitudeMAx = 255;
	
	public ColourArrayType colourMap = ColourArrayType.HOT;

	public int displayGain = 1;
	
	public boolean showGrid;

	public boolean flipLeftRight;
	
	public boolean subtractBackground = false;
	
	public int backgroundTimeFactor = 50;
	
	public double backgroundScale = 1.0;
	
	public int resolution = 0;
	
	public static final int[] getResolutionValues() {
		int[] vals = {RESOLUTION_DEFAULT, RESOLUTION_HIGH, RESOLUTION_BEST};
		return vals;
	}

	public static String getResolutionName(int res) {
		switch (res) {
		case RESOLUTION_DEFAULT:
			return "Normal";
		case RESOLUTION_HIGH:
			return "High";
		case RESOLUTION_BEST:
			return "Best";
		default:
			return "Unknown";
		}
	}

}
