package tritechplugins.detect.track.dataselect;

import java.io.Serializable;

import PamguardMVC.dataSelector.DataSelectParams;

public class TrackDataSelectorParams extends DataSelectParams implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Minimum end to end (straight) length
	 */
	public double minLength = 4;
	
	/**
	 * min straighness. 0 - 1. Ratio of striaght lenght to wobbly length
	 */
	public double minStraightness = 0.5;
	
	/**
	 * Min duration in seconds
	 */
	public double minDuration = 2.;
	
	/**
	 * Min number of points
	 */
	public int minPoints = 10;
	
	/**
	 * Ignore tracks only on x = 0;
	 */
	public boolean vetoXzero = true;
	
	/**
	 * Max number of detected regions per frame. 
	 */
	public int maxPointsPerFrame = 100;
	

	@Override
	protected TrackDataSelectorParams clone() {
		try {
			return (TrackDataSelectorParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
