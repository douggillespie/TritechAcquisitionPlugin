package tritechplugins.detect.track;

import java.io.Serializable;

/**
 * Parameters controlling track linking
 * @author dg50
 *
 */
public class TrackLinkParameters implements Serializable, Cloneable {


	public static final long serialVersionUID = 1L;
	
	/**
	 * Max time separation in milliseconds. 
	 */
	public long maxTimeSeparation = 500;
	
	/**
	 * Max separation in frames. Can be useful if framerate
	 * drops too low.  
	 */
	public int maxSeparationFrames = 5;
	private boolean maxFramesSet;
	
	/**
	 * Track within each sonar independently. Default
	 * is to process together (assumes both pointing at same space)
	 */
	public boolean separateSonars = false;
	
	/**
	 * Maximum speed in m/s
	 */
	public double maxSpeed = 5;
	
	/**
	 * Max ratio of target sizes for a match. 
	 */
	public double maxSizeRatio = 3;

	/**
	 * Min number of points in a track. 
	 */
	public int minTrackPoints = 10;
	
	public double minWobblyLength = 2;
	
	public double minStraightLength = 2;

	@Override
	public TrackLinkParameters clone() {
		try {
			TrackLinkParameters newParams = (TrackLinkParameters) super.clone();
			if (newParams.maxFramesSet == false) {
				newParams.maxSeparationFrames = new TrackLinkParameters().maxSeparationFrames;
				newParams.maxFramesSet = true;
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


}
