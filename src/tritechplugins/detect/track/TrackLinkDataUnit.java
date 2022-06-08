package tritechplugins.detect.track;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import tritechplugins.detect.threshold.RegionDataUnit;

public class TrackLinkDataUnit extends SuperDetection<RegionDataUnit> {
	
	private TrackChain trackChain;

	public TrackLinkDataUnit(TrackChain trackChain) {
		super(trackChain.getFirstTime());
		this.trackChain = trackChain;
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
		trackChain.addRegion(subDetection.getRegion());
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
		str += String.format("<br>Sonars %s, Mean Occupancy %3.1f%%<br>Duration %3.1fs, points %d<br>", 
				trackChain.getsonarIdString(), trackChain.getMeanOccupancy(), getDurationInMilliseconds()/1000., 
				trackChain.getChainLength());
		
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
}
