package tritechplugins.display.swing;

import java.io.Serializable;
import java.util.HashMap;

import PamView.ColourArray.ColourArrayType;
import tritechplugins.display.swing.overlays.SonarOverlayData;

/**
 * Parameters controlling layout of the sonars panel. 
 * @author dg50
 *
 */
public class SonarsPanelParams implements Serializable, Cloneable {

	public static final int RESOLUTION_DEFAULT = 0;
	public static final int RESOLUTION_HIGH = 1;
	public static final int RESOLUTION_BEST = 2;
	
	public static final int OVERLAY_TAIL_NONE = 0;
	public static final int OVERLAY_TAIL_TIME = 1;
	public static final int OVERLAY_TAIL_ALL = 2;
	
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
	
	public int tailOption = OVERLAY_TAIL_TIME;
	/**
	 * Tail time in seconds. 
	 */
	public double tailTime = 2.0; 
	
	private HashMap<String, SonarOverlayData> overlayDatas = new HashMap<>();
	
	/**
	 * @return the overlayDatas
	 */
	public HashMap<String, SonarOverlayData> getOverlayDatas() {
		if (overlayDatas == null) {
			overlayDatas = new HashMap<>();
		}
		return overlayDatas;
	}

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
	
	public static final int[] getOverlayOptValues() {
		int[] vals = {OVERLAY_TAIL_NONE, OVERLAY_TAIL_TIME, OVERLAY_TAIL_ALL};
		return vals;
	}
	
	public static String getOverlayOptName(int tailOption) {
		switch (tailOption) {
		case OVERLAY_TAIL_NONE:
			return "No Tail";
		case OVERLAY_TAIL_TIME:
			return "Time Tail";
		case OVERLAY_TAIL_ALL:
			return "Show Everything";
		}
		return "Unknown";
	}

}
