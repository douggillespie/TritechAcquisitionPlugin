package tritechplugins.detect.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import PamUtils.PamArrayUtils;
import tritechgemini.detect.DetectedRegion;

public class TrackChain {

	private LinkedList<DetectedRegion> regions;
	
	private double meanOccupancy;
	
	private TrackLinkDataUnit parentDataUnit;
	
	private double trackLinkScore = -1;

	/**
	 * Count of skipped images. Used during track
	 * formation to count how many frames haven't 
	 * added to this chain. 
	 */
	public int imageSkips;
	
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
	 * Get a direction vector for the track. This also includes
	 * times, so can be used to extrapolate. 
	 * @return direction vector. 
	 */
	public TrackVector getTrackVector() {
		DetectedRegion r1 = regions.get(0);
		DetectedRegion r2 = regions.get(regions.size()-1);
		return new TrackVector(-r1.getPeakX(), r1.getPeakY(), -r2.getPeakX(), 
				r2.getPeakY(), getFirstTime(), getLastTime());
	}
	
	/**
	 * Chop up the track into segments based on a time in seconds
	 * and return multiple track vectors. 
	 * @param segmentDuration segment duration in seconds. <br> 0 will get a single segment for entire track.
	 * @return list of vectors along length of track. 
	 */
	public ArrayList<TrackVector> getTrackVectors(double segmentDuration) {
		ArrayList<TrackVector> segs = new ArrayList<>();
		int n = regions.size();
		if (n<2) {
			return segs;
		}
		long trackMillis = getTrackDuration();
		long segMillis = (long) (segmentDuration*1000);
		if (segMillis <= 0 || segMillis >= trackMillis) {
			segs.add(getTrackVector());
			return segs;
		}
		DetectedRegion segStart = null, segEnd = null;
		synchronized (this) {
			Iterator<DetectedRegion> it = regions.iterator();
			segStart = it.next();
			while (it.hasNext()) {
				DetectedRegion thisReg = it.next();
				if (thisReg.getTimeMilliseconds()-segStart.getTimeMilliseconds() > segMillis) {
					if (segEnd != null) {
						TrackVector v = new TrackVector(segStart.getPeakX(), segStart.getPeakY(), 
								segEnd.getPeakX(), segEnd.getPeakY(), segStart.getTimeMilliseconds(), segEnd.getTimeMilliseconds());
						segs.add(v);
						// reset, starting at end of last segment
						segStart = segEnd;
						segEnd = null;
					}
					/**
					 * If everything is too spaced out, this doing nothing here should trick it 
					 * into waiting until segs are long enough - though may end
					 * up with a seg for every pair of points. 
					 */
//					else {
						// not enough points to make a vector.
//						segStart = thisReg;
//						continue;
//					}
				}
				else {
					// not yet long enough. 
					// keep ref to this for next time. 
					segEnd = thisReg;
				}
			}
			/**
			 * If what's left is > half the span of another segment, keep it anyway. 
			 */
			if (segEnd != null & segEnd.getTimeMilliseconds()-segStart.getTimeMilliseconds() >= segMillis/2) {
				TrackVector v = new TrackVector(segStart.getPeakX(), segStart.getPeakY(), 
						segEnd.getPeakX(), segEnd.getPeakY(), segStart.getTimeMilliseconds(), segEnd.getTimeMilliseconds());
				segs.add(v);
			}
		}
		return segs;
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
			/* 
			 * may need to do this separately for each sonar if multiples are interleaved
			 * since the sonar to sonar data jump about quite a bit spatially, but on the 
			 * other hand, what do do if it starts on one and ends on the other ?  
			 */
			int startId = regions.get(0).getSonarId();
			int endId = regions.get(regions.size()-1).getSonarId();
			/*
			 *  go from the end, until we reach the first sonar with the other
			 *  id, then carry on with that one only.  
			 */
//			if (startId == endId) {
//				endId = startId+1; // something else, so never happens. 
//			}
			int usingSonar = startId;
//			System.out.println()
			ListIterator<DetectedRegion> it = regions.listIterator();
			DetectedRegion last = it.next();
			int i = 0;
//			System.out.println("\n\n\n\n");
			while (it.hasNext()) {
				i++;
				DetectedRegion next = it.next();
				if (next.getSonarId() == endId) {
					usingSonar = endId;
				}
				if (usingSonar != next.getSonarId()) {
//					System.out.printf("Skipping point %d from sonar %d to sonar %d\n", i, 
//							last.getSonarId(), next.getSonarId());
					continue;
				}
				if (next.getTimeMilliseconds() == last.getTimeMilliseconds()) {
					continue;
				}
					double x = next.getPeakX()-last.getPeakX();
					double y = next.getPeakY()-last.getPeakY();
					len += Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//					System.out.printf("Using point %d from sonar %d to sonar %d, xy = %3.1f,%3.1f t = %dms\n", i, 
//							last.getSonarId(), next.getSonarId(), x, y, next.getTimeMilliseconds()-last.getTimeMilliseconds());
//				}
//				else {
//					continue;
//				}
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
	public long getTrackDuration() {
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
		imageSkips = 0;
	}

	/**
	 * Get a list of sonar ids used in this track chain. 
	 * @return
	 */
	public int[] getSonarIds() {
		// make a list of all the sonar Ids in the region. 
		Set<Integer> uniqueIds = new HashSet<>();
		synchronized (this) {
			for (DetectedRegion region : regions) {
				uniqueIds.add(region.getSonarId());
			}
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
		synchronized (this) {
			for (DetectedRegion r : regions) {
				totalOcc += (r.getOccupancy()*r.getnPoints());
				totalPix += r.getnPoints();
			}
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

	public double getStraigtness() {
		double wl = getWobblyLength();
		if (wl == 0) {
			return 1.;
		}
		else {
			return getEnd2EndMetres() / wl;
		}
	}
	
	/**
	 * Get the average number of points per second within the 
	 * track. This is (the number of points in the track - 1) / track duration. 
	 * @return point rate. 
	 */
	public double getPointRate() {
		if (regions.size()<2) {
			return 0;
		}
		double np = regions.size()-1;
		double t = (double)(getLastTime()-getFirstTime())/1000.;
		return np/t;
	}

	@Override
	public String toString() {
		if (regions.size() == 0) {
			return "Empty track chain";
		}
		long t1 = regions.get(0).getTimeMilliseconds();
		long t2 = regions.get(regions.size()-1).getTimeMilliseconds();
		return String.format("Track Chain %d pts %3.2fs", regions.size(), (double) (t2-t1)/1000.);
	}

	public double getTrackLinkScore() {
//		if (trackLinkScore < 0) {
			TrackScorer tx = new TrackScorer();
			trackLinkScore = tx.scoreTrack(this);
//		}
		return trackLinkScore;
	}
	
	/**
	 * Get mean size of track in radial coordinate. 
	 * @return mean radial size in metres 
	 */
	public double getMeanRSize() {
		int n = 0;
		double rSize = 0;
		synchronized(this) {
			Iterator<DetectedRegion> it = regions.iterator();
			while (it.hasNext()) {
				DetectedRegion aR = it.next();
				n++;
				rSize += aR.getMaxRange()-aR.getMinRange();
			}
		}
		if (n == 0) {
			return 0;
		}
		return rSize/n;		
	}
	
	/**
	 * Get the track points for a single sonar. 
	 * @param sonarId
	 * @return copied list of the track points for the given sonar
	 */
	public ArrayList<DetectedRegion> getSonarRegions(int sonarId) {
		ArrayList<DetectedRegion> sonarPoints = new ArrayList<>(regions.size());
		synchronized (this) {
			Iterator<DetectedRegion> it = regions.iterator();
			while (it.hasNext()) {
				DetectedRegion region = it.next();
				if (region.getSonarId() == sonarId) {
					sonarPoints.add(region);
				}
			}
		}
		return sonarPoints;
	}
	
	/**
	 * Get the median of the radial coordinate of the track size. 
	 * @return median radial size. Metres
	 */
	public double getMedianRSize() {
		int n = 0;
		double[] values = null;
		synchronized(this) {
			if (regions.size() == 0) {
				return 0;
			}
			Iterator<DetectedRegion> it = regions.iterator();
			values = new double[regions.size()]; 
			while (it.hasNext()) {
				DetectedRegion aR = it.next();
				values[n++] = aR.getMaxRange()-aR.getMinRange();
			}
		}
		return PamArrayUtils.median(values);
	}
}
