package tritechplugins.detect.track;

import java.util.Iterator;

import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubdetectionInfo;
import PamguardMVC.superdet.SuperDetection;
import tritechplugins.detect.threshold.RegionDataUnit;

public class TrackLinkDataUnit extends SuperDetection<RegionDataUnit> {
	
	private TrackChain trackChain;

	public TrackLinkDataUnit(TrackChain trackChain) {
		super(trackChain.getFirstTime());
		this.trackChain = trackChain;
		trackChain.setParentDataUnit(this);
	}
	
	public TrackLinkDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		this.trackChain = new TrackChain(null);
	}

	@Override
	public long getEndTimeInMilliseconds() {
		return trackChain.getLastTime();
	}

	@Override
	public int addSubDetection(RegionDataUnit subDetection) {
		/*
		 * Used in viewer, need to add the region back to the chain 
		 * list, but in detection mode, the chain already exists, to this
		 * would cause a concurrentmodificationexception
		 */
		if (findSubDetection(subDetection.getUID()) == null) {
			trackChain.addRegion(subDetection.getRegion());
		}
//		else {
//			System.out.println("Sub chain detectoin already exists");
//		}
		return super.addSubDetection(subDetection);
	}
	
	/**
	 * Use in detection mode
	 * @param subDetection
	 * @return
	 */
	public int addNewSubDetection(RegionDataUnit subDetection) {
		return super.addSubDetection(subDetection);
	}

	@Override
	public Double getDurationInMilliseconds() {
		return (double) (getEndTimeInMilliseconds() - getTimeMilliseconds());
	}

	/**
	 * @return the trackChain
	 */
	public TrackChain getTrackChain() {
		return trackChain;
	}

	@Override
	public String getSummaryString() {
		String str = "<html>Gemini Track UID " + getUID();

		DataUnitBaseData basicData = getBasicData();
		str += String.format("<br><b>%s %s - %s</b> %s", PamCalendar.formatDate(basicData.getTimeMilliseconds(), false),
				PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, false),
				PamCalendar.formatTime(trackChain.getLastTime(), 3, false),
				"UTC");
		
		str += String.format("<br>Sonars %s, Mean Occupancy %3.1f%%<br>Duration %3.1fs, points %d (%3.1fpps)<br>", 
				trackChain.getsonarIdString(), trackChain.getMeanOccupancy(), getDurationInMilliseconds()/1000., 
				trackChain.getChainLength(), (trackChain.getChainLength()-1)/(getDurationInMilliseconds()/1000.));
		
		str+= String.format("Total length %3.1fm, Straight length %3.1fm, Straightness %4.2f<br>", 
				trackChain.getWobblyLength(), trackChain.getEnd2EndMetres(), trackChain.getStraigtness());
		
		String annotStr = getAnnotationsSummaryString();
		if (annotStr != null) {
			str += annotStr;
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
		
		return str;
	}
	
	/**
	 * Get the mean occupancy from the chain. 
	 * @return
	 */
	public double getMeanOccupancy() {
		return trackChain.getMeanOccupancy();
	}

	/**
	 * Set the mean occupancy for the chain (reading back from database)
	 * @param meanOccupancy
	 */
	public void setMeanOccupancy(double meanOccupancy) {
		trackChain.setMeanOccupancy(meanOccupancy);
	}
	
	/**
	 * Get a count of the maximum number of detections per frame within this
	 * track. Can be used to not display total crap. 
	 * @return max count
	 */
	public int getMaxFrameDetectionCount() {
		int n = 0;
		synchronized (getSubDetectionSyncronisation()) {
			Iterator<SubdetectionInfo<RegionDataUnit>> iter = getSubDetectionInfo().iterator();
			while (iter.hasNext()) {
				n = Math.max(n, iter.next().getSubDetection().getFrameDetectionCount());
			}
		} 
		return n;
	}
}
