package tritechplugins.detect.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import tritechgemini.detect.DetectedRegion;

public class TrackChain {

	private LinkedList<DetectedRegion> regions;
	
	private double meanOccupancy;
	
	private TrackLinkDataUnit parentDataUnit;
	
	public TrackChain(DetectedRegion region) {
		this.regions = new LinkedList<>();
		if (region != null) {
			addRegion(region);
		}
	}
	
	/**
	 * @return the regions
	 */
	public LinkedList<DetectedRegion> getRegions() {
		return regions;
	}
	
	/**
	 * Get the time of the first region in the chain. 
	 * @return chain start time (millis)
	 */
	public long getFirstTime() {
		if (regions.size() == 0) {
			return 0;
		}
		return regions.get(0).getTimeMilliseconds();
	}
	
	/**
	 * Get the time of the last added region in milliseconds. 
	 * @return milliseconds. 
	 */
	public long getLastTime() {
		if (regions.size() == 0) {
			return 0;
		}
		return getLastRegion().getTimeMilliseconds();
	}
	
	/**
	 * Get the last region in the chain. 
	 * @return
	 */
	public DetectedRegion getLastRegion() {
		return regions.get(regions.size()-1);
	}
	
	/**
	 * Get current length of track in units. 
	 * @return
	 */
	public int getChainLength() {
		return regions.size();
	}
	
	/**
	 * Get a Cartesian vector from the track start to the track end.  
	 * @return end to end velocity. 
	 */
	public double[] getEndsVector() {
		if (regions.size() < 1) {
			return null;
		}
		DetectedRegion r1 = regions.get(0);
		DetectedRegion r2 = regions.get(regions.size()-1);
		double x = (r2.getPeakX()-r1.getPeakX());
		double y = (r2.getPeakY()-r1.getPeakY());
		double[] velocityVec = {x, y};
		return velocityVec;
	}
	
	/**
	 * Get the end to end length in metres. 
	 * @return end to end length in metres. 
	 */
	public double getEnd2EndMetres() {
		double[] vec = getEndsVector();
		if (vec == null) {
			return 0;
		}
		return Math.sqrt(Math.pow(vec[0], 2) + Math.pow(vec[1], 2));
	}
	
	/**
	 * Get the point to point length in metres iterating through all points. 
	 * @return
	 */
	public double getWobblyLength() {
		if (regions.size() < 2) {
			return 0;
		}
		double len = 0.;
		synchronized (this) {
			ListIterator<DetectedRegion> it = regions.listIterator();
			DetectedRegion last = it.next();
			while (it.hasNext()) {
				DetectedRegion next = it.next();
				len += Math.sqrt(Math.pow(next.getPeakX()-last.getPeakX(), 2) + 
						Math.pow(next.getPeakY()-last.getPeakY(), 2));
				last = next;
			}
		}
		return len;
	}

	/**
	 * Velocity vector from end to end of the track. Null
	 * if only one point so far. 
	 * @return end to end velocity. 
	 */
	public double[] getEndsVelocity() {
		if (regions.size() < 2) {
			return null;
		}
		double[] vec = getEndsVector();
		DetectedRegion r1 = regions.get(0);
		DetectedRegion r2 = regions.get(regions.size()-1);
		double dt = (double) (r2.getTimeMilliseconds()-r1.getTimeMilliseconds())/1000.;
		vec[0] /= dt;
		vec[1] /= dt;
		return vec;
	}

	/**
	 * Track duration in milliseconds from first to last point
	 * @return duration in milliseconds. 
	 */
	public double getTrackDuration() {
		if (regions.size() < 2) {
			return 0;
		}
		DetectedRegion r1 = regions.get(0);
		DetectedRegion r2 = regions.get(regions.size()-1);
		return r2.getTimeMilliseconds()-r1.getTimeMilliseconds();
	}

	/**
	 * Add a new region to the end of the chain
	 * @param detectedRegion
	 */
	public void addRegion(DetectedRegion detectedRegion) {
		synchronized (this) {
			regions.add(detectedRegion);
		}
		
	}

	/**
	 * Get a list of sonar ids used in this track chain. 
	 * @return
	 */
	public int[] getSonarIds() {
		// make a list of all the sonar Ids in the region. 
		Set<Integer> uniqueIds = new HashSet<>();
		for (DetectedRegion region : regions) {
			uniqueIds.add(region.getSonarId());
		}
		int[] ids = new int[uniqueIds.size()] ;
		int i = 0;
		for (Integer id : uniqueIds) {
			ids[i++] = id;
		}
		return ids;
	}
	
	/**
	 * Get sonar id's as a comma deliminated string. 
	 * @return
	 */
	public String getsonarIdString() {
		int[] ids = getSonarIds();
		if (ids == null || ids.length == 0) {
			return null;
		}
		String str = String.format("%d", ids[0]);
		for (int i = 1; i < ids.length; i++) {
			str += String.format(",%d", ids[i]);
		}
		return str;
	}

	/**
	 * @param meanOccupancy the meanOccupancy to set
	 */
	public void setMeanOccupancy(double meanOccupancy) {
		this.meanOccupancy = meanOccupancy;
	}

	/**
	 * Get the mean occupancy of all points in the chain. 
	 * @return the meanOccupancy
	 */
	public double getMeanOccupancy() {
		if (meanOccupancy == 0) {
			meanOccupancy = calcMeanOccupancy();
		}
		return meanOccupancy;
	}

	/**
	 * Calculate a mean occupancy for all points in the chain. 
	 * @return mean occupancy
	 */
	private double calcMeanOccupancy() {
		double totalOcc = 0;
		double totalPix = 0;
		for (DetectedRegion r : regions) {
			totalOcc += (r.getOccupancy()*r.getnPoints());
			totalPix += r.getnPoints();
		}
		return totalOcc/totalPix;
	}

	/**
	 * @return the parentDataUnit
	 */
	public TrackLinkDataUnit getParentDataUnit() {
		return parentDataUnit;
	}

	/**
	 * @param parentDataUnit the parentDataUnit to set
	 */
	public void setParentDataUnit(TrackLinkDataUnit parentDataUnit) {
		this.parentDataUnit = parentDataUnit;
	}
}
