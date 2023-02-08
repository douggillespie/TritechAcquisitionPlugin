package tritechplugins.display.swing;

import java.io.Serializable;
import java.util.Arrays;
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

	public static final int TOOLTIP_TEXT = 0x1;
	public static final int TOOLTIP_IMAGE = 0x2;
	public static final int TOOLTIP_BOTH = 0x3;
	
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
	
	public boolean usePersistence = false;
	
	public int persistentFrames = 10;

	public boolean rescalePersistence = true;
	
	private int toolTipType = TOOLTIP_BOTH;
	
	private HashMap<String, SonarOverlayData> overlayDatas = new HashMap<>();
	
	/**
	 * Last used range for display. Will get used when no image is loaded so we can still use the 
	 * display for detections. 
	 */
	private double[] lastKnownRange = new double[2];
	
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
	
	/**
	 * Get the last known range used for a particular sonar image. 
	 * @param imageIndex
	 * @return
	 */
	public double getLastKnownRange(int imageIndex) {
		checkLastRangeData(imageIndex);
		return lastKnownRange[imageIndex];
	}
	
	/**
	 * Set the last known range for a particular sonar image. 
	 * @param imageIndex 
	 * @param range
	 */
	public void setLastKnownRange(int imageIndex, double range) {
		checkLastRangeData(imageIndex);
		lastKnownRange[imageIndex] = range;
	}

	/**
	 * Check array is big enough
	 * @param imageIndex
	 */
	private void checkLastRangeData(int imageIndex) {
		if (lastKnownRange == null) {
			lastKnownRange = new double[imageIndex+1];
		}
		else if (lastKnownRange.length <= imageIndex) {
			lastKnownRange = Arrays.copyOf(lastKnownRange, imageIndex+1);
		}
		
	}

	/**
	 * @return the toolTipType
	 */
	public int getToolTipType() {
		if (toolTipType == 0) {
			toolTipType = TOOLTIP_BOTH;
		}
		return toolTipType;
	}

	/**
	 * @param toolTipType the toolTipType to set
	 */
	public void setToolTipType(int toolTipType) {
		this.toolTipType = toolTipType;
	}

	// cycle around different tool tip styles
	public void cycleTipType() {
		toolTipType ++;
		if (toolTipType > TOOLTIP_BOTH) {
			toolTipType = TOOLTIP_TEXT;
		}
//		System.out.println("Set Tritech tool tip type to " + toolTipType);
	}
	
	public String getTipDescription() {
		switch (getToolTipType()) {
		case TOOLTIP_TEXT:
			return "Show text only";
		case TOOLTIP_IMAGE:
			return "Show image clip only";
		case TOOLTIP_BOTH:
			return "Show text and image";
		}
		return null;
	}

}
