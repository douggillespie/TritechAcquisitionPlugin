package tritechplugins.detect.threshold.stats;

import PamguardMVC.PamDataUnit;

/**
 * Data unit for detector stats. May not use in long term, but need for development
 * @author dg50
 *
 */
public class DetStatsDataUnit extends PamDataUnit{

	private int regionCount;
	private int trackCount;
	private int nFrame;
	private int usedRegionCount;

	public DetStatsDataUnit(long timeMilliseconds, int sonarId, long currentTime, int nFrame, int regionCount, int usedRegionCount, int trackCount) {
		super(timeMilliseconds);
		setChannelBitmap(sonarId);
		setDurationInMilliseconds(currentTime-timeMilliseconds);
		this.nFrame = nFrame;
		this.regionCount = regionCount;
		this.usedRegionCount = usedRegionCount;
		this.trackCount = trackCount;
	}

	public int getRegionCount() {
		return regionCount;
	}

	public int getTrackCount() {
		return trackCount;
	}

	public int getnFrame() {
		return nFrame;
	}

	public int getUsedRegionCount() {
		return usedRegionCount;
	}

}
