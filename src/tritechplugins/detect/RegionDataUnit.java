package tritechplugins.detect;

import PamguardMVC.PamDataUnit;
import tritechgemini.detect.DetectedRegion;

public class RegionDataUnit extends PamDataUnit {

	private DetectedRegion region;
	private int sonarId;

	public RegionDataUnit(long timeMilliseconds, int sonarId, DetectedRegion region) {
		super(timeMilliseconds);
		this.sonarId = sonarId;
		this.region = region;
	}

	/**
	 * @return the region
	 */
	public DetectedRegion getRegion() {
		return region;
	}

	/**
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}


}
