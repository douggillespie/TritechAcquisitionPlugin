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
			return (TrackLinkParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


}
