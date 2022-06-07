package tritechplugins.detect.threshold;

import PamUtils.PamCalendar;
import PamUtils.time.CalendarControl;
import PamguardMVC.DataUnitBaseData;
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

	@Override
	public String getSummaryString() {
		String str = "<html>";
		str += "UID: " + getUID() + "<p>";
		if (getParentDataBlock() != null) {
			str += "<i>" + getParentDataBlock().getLongDataName() + "</i><p>";
		}
		DataUnitBaseData basicData = getBasicData();
//		str += PamCalendar.formatDateTime(timeMilliseconds) + "<p>";
		str += String.format("%s %s %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), true),
				PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, true),
				CalendarControl.getInstance().getTZCode(true));
		if (CalendarControl.getInstance().isUTC() == false) {
			str += String.format("%s %s %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), false),
					PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, false),
					"UTC");
		}
		str += String.format("Size: %d pix, %3.1fm<br>", region.getRegionSize(), region.getObjectSize());
		str += String.format("Level: Mean %d, Max %d<br>", region.getAverageValue(), region.getMaxValue());
		str += String.format("Angles: %3.1f to %3.1f<br>", Math.toDegrees(region.getMinBearing()), 
				Math.toDegrees(region.getMaxBearing()));

		int nSuperDet = getSuperDetectionsCount();
		if (nSuperDet > 0) {
			for (int i = 0; i < nSuperDet; i++) {
				PamDataUnit sd = getSuperDetection(i);
				String sdString = sd.getSummaryString();
				if (sdString == null) {
					continue;
				}
				if (sdString.startsWith("<html>")) {
					sdString = sdString.substring(6);
				}
				sdString = "<b>Super detection</b> " + sdString;
				Object sdBlock = sd.getParentDataBlock();
				if (sdBlock != null) {
					str += "Grouped in " + sd.getParentDataBlock().getDataName() + "<br>";
				}
				str += sdString;
			}
		}
		return str;
	}


}
