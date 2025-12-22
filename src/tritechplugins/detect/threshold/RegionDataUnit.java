package tritechplugins.detect.threshold;

import PamDetection.PamDetection;
import PamUtils.PamCalendar;
import PamUtils.time.CalendarControl;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.acquire.SonarPosition;

public class RegionDataUnit extends PamDataUnit implements PamDetection {

	private DetectedRegion region;
	private int sonarId;
	private int frameDetectionCount;

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
//		str += "UID: " + getUID();
		str += "Sonar " + sonarId + "<p>";
		if (getParentDataBlock() != null) {
			str += "<i>" + getParentDataBlock().getLongDataName() + "</i><p>";
		}
		DataUnitBaseData basicData = getBasicData();
//		str += PamCalendar.formatDateTime(timeMilliseconds) + "<p>";
		double degsSize = Math.toDegrees(Math.abs(region.getMaxBearing()-region.getMinBearing()));
		double angMetres = Math.abs(region.getMaxBearing()-region.getMinBearing())*region.getPeakRange();
		double radMetres = region.getMaxRange()-region.getMinRange();
		str += String.format("<b>%s %s</b> %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), true),
				PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, true),
				CalendarControl.getInstance().getTZCode(true));
		if (CalendarControl.getInstance().isUTC() == false) {
			str += String.format("%s %s %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), false),
					PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, false),
					"UTC");
		}
		// add the peak x information and possibly also the absolut position if different. 
		double x = region.getPeakRange() * -Math.sin(region.getPeakBearing());
		double y = region.getPeakRange() * Math.cos(region.getPeakBearing());
		SonarPosition sonarPos = getSonarPosition();
		if (sonarPos == null || sonarPos.isZero()) {
			str += String.format("Peak xy: %3.2f,%3.2fm<br>", x, y);
		}
		else {
			double[] newXY = sonarPos.translate(x, y);
			str += String.format("Peak xy: (%3.2f,%3.2f)m; absolute: (%3.1f,%3.1f)m<br>", x, y, newXY[0], newXY[1]);
		}
		str += String.format("Size: %d pix, %3.2fx%3.2fm, Occupancy %3.1f%%<br>", region.getRegionSize(), angMetres, radMetres, region.getOccupancy());
		str += String.format("Level: Mean %d, Max %d<br>", region.getAverageValue(), region.getMaxValue());
		str += String.format("Angles: %3.1f to %3.1f<br>", Math.toDegrees(region.getMinBearing()), 
				Math.toDegrees(region.getMaxBearing()));
		
		String annotStr = getAnnotationsSummaryString();
		if (annotStr != null) {
			str += "<br>" + annotStr;
		}

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
		str += "</p></html>";
		str = str.replace("<p>", "<p width=\"400\">");
		return str;
	}

	private SonarPosition getSonarPosition() {
		// find the sonar position information. 
		// but there is no easy link back from this datablock to the tritech process
		// because it's layers doen in a separate module !
		PamDataBlock datablock = getParentDataBlock();
		if (datablock instanceof RegionDataBlock == false) {
			return null;
		}
		RegionDataBlock rdb = (RegionDataBlock) datablock;
		return rdb.getSonarPosition(region.getSonarId());
	}

	@Override
	public int getChannelBitmap() {
		if (super.getChannelBitmap() != 0) {
			return super.getChannelBitmap();
		}
		else if (getParentDataBlock() instanceof RegionDataBlock) {
			RegionDataBlock regionDataBlock = (RegionDataBlock) getParentDataBlock();
			return regionDataBlock.sonarIdToChannelMap(this.sonarId);
		}
		else {
			return super.getChannelBitmap();
		}
	}

	@Override
	public SuperDetection getSuperDetection(PamDataBlock superDataBlock) {
		/**
		 * Have overridden this, so that it looks for super super detections
		 * which is needed on the Tritech display data selector which may be
		 * used with a Detection Grouper. The group is a super of the lined regions 
		 * data unit so is the super-super of this. 
		 * As an alternative, it may be pracital to change line 64 of
		 * SuperDetDataSelector ...
		 * SuperDetection superDetection = pamDataUnit.getSuperDetection(superDataBlock);
		 * so that this calls with the additional 'true'argument. That may affect other
		 * PAMGuard modules though. 
		 */
		return super.getSuperDetection(superDataBlock, true);
	}

	/**
	 * Set a count of the number of detections in this sonar frame 
	 * (Get's used in various vetoes of noisy data)
	 * @param detCount
	 */
	public void setFrameDetectionCount(Integer detCount) {
		frameDetectionCount = detCount == null ? 0 : detCount;
	}

	/**
	 * Get a count of the number of detections in this sonar frame 
	 * (Get's used in various vetoes of noisy data)
	 * @return count of detections in one frame. 
	 */
	public int getFrameDetectionCount() {
		return frameDetectionCount;
	}

}
