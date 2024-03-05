package tritechplugins.detect.track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.Track;

import PamUtils.PamCalendar;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.threshold.RegionDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;

/**
 * Does the actual work for the TrackLinkProcess. May be for one or more (aligned) sonars. 
 * @author dg50
 *
 */
public class TrackLinker {

	private TrackLinkProcess trackLinkProcess;
	private int sonarId;
	
	private LinkedList<TrackChain> embryos = new LinkedList<>();
	private TrackLinkDataBlock trackDataBlock;

	public TrackLinker(TrackLinkProcess trackLinkProcess, int sonarId) {
		this.trackLinkProcess = trackLinkProcess;
		this.sonarId = sonarId;
		trackDataBlock = trackLinkProcess.getTrackLinkDataBlock();
	}

	/**
	 * New list of regions from a single sonar image. 
	 * @param regions list of regions, can be null. 
	 */
	public void newImageRegions(long currentTime, List<DetectedRegion> regions) {
		closeOldOnes(currentTime);
		if (regions == null) {
			return;
		}
		/*
		 * What to do ? go through all chains and new data and work out a score for every 
		 * (reasonable) combination, then take these in order until we've run out ? 
		 */
		ArrayList<CandidateLink> candidateLinks = new ArrayList<>();
		for (TrackChain chain : embryos) {
			for (int i = 0; i < regions.size(); i++) {
				double score = matchRegion(chain, regions.get(i));
				if (score > 0) {
					candidateLinks.add(new CandidateLink(chain, i, score));
				}
			}
		}
		
		/**
		 * sort in what should be best match first order.
		 */
		if (candidateLinks.size() >= 2) {
			candidateLinks.sort(null);
		}
//		if (currentTime >= 1700068388159L) {
//			System.out.println("Break track");
//		}
		//  now go through and make the links, but don't reuse any chains OR new regions. 
		boolean[] usedRegions = new boolean[regions.size()];
		for (CandidateLink candidate : candidateLinks) {
			if (candidate.chain.getLastTime() == currentTime) {
				continue; // have already added to this chain.
			}
			if (usedRegions[candidate.regionIndex]) {
				continue; // this region has already been added to a chain. 
			}
			// should be good to add new region to chain. 
			candidate.chain.addRegion(regions.get(candidate.regionIndex));
			usedRegions[candidate.regionIndex] = true;
			/**
			 * If a chain isn't yet a data unit and is long enough, then we want to make
			 * an embryonic data unit to add to the output datablock so it get's displayed. 
			 */
			if (candidate.chain.getParentDataUnit() == null && wantChain(candidate.chain)) {
				intiailiseDataUnit(candidate.chain);
			}
			else if (candidate.chain.getParentDataUnit() != null) {
				/**
				 * Otherwise there may be a data unit, in which case we need to update it 
				 * with a new revion using this latest region. 
				 */
				extendDataUnit(candidate.chain, regions.get(candidate.regionIndex));
			}
			// or nothing needs to be done if the chain is still to small. 
		}
		
		// then take everything else and turn it into an embryo. 
		for (int i = 0; i < regions.size(); i++) {
			if (usedRegions[i]) {
				continue;
			}
			embryos.add(new TrackChain(regions.get(i)));
		}
		
	}
	
	/**
	 * Start processing (call on PAMStart) will clear
	 * up any junk from previous runs. 
	 */
	public void startProcessing() {
		embryos.clear();
	}
	/**
	 * Stop processing (call on PamStop) will clean up any 
	 * remaining embryonic chains. 
	 */
	public void stopProcessing() {
		closeOldOnes(Long.MAX_VALUE);
	}
	
	private class CandidateLink implements Comparable<CandidateLink>{
		TrackChain chain;
		int regionIndex;
		double score;
		public CandidateLink(TrackChain chain, int regionIndex, double score) {
			super();
			this.chain = chain;
			this.regionIndex = regionIndex;
			this.score = score;
		}
		
		@Override
		public int compareTo(CandidateLink other) {
			/*
			 * Want to sort by best score first, so 
			 * will do the opposite of what's 'normal'
			 */
			int lenDiff = other.chain.getChainLength()-this.chain.getChainLength();
			if (lenDiff != 0) {
				return lenDiff;
			}
			int ans = (int) Math.signum(other.score-this.score);
			return ans;
		}

		@Override
		public String toString() {
			return String.format("link ind %d to %s, score %3.2f", regionIndex, chain, score);
		}
	}

	private void closeOldOnes(long currentTime) {
		Iterator<TrackChain> chainIt = embryos.iterator();
		while (chainIt.hasNext()) {
			TrackChain chain = chainIt.next();
			if (currentTime - chain.getLastTime() > trackLinkProcess.trackLinkParams.maxTimeSeparation) {
				chainIt.remove();
				if (wantChain(chain)) {
					completeChain(chain);
				}
			}
		}
		
	}

	/**
	 * Start a data unit and add it to the output data block as an embryo. 
	 * Will stop being embryonic when the chain is completed. 
	 * @param chain
	 */
	private void intiailiseDataUnit(TrackChain chain) {
		TrackLinkDataUnit trackDataUnit = new TrackLinkDataUnit(chain);
		trackDataUnit.setEmbryonic(true);
		/*
		 *  me thinks that we're first going to have to make all the sub detections and
		 *  add them to this super detection 
		 */
		synchronized(chain) {
			for (DetectedRegion region : chain.getRegions()) {
				RegionDataUnit regionDataUnit = new RegionDataUnit(region.getTimeMilliseconds(), region.getSonarId(), region);
				trackDataUnit.addNewSubDetection(regionDataUnit);
				trackLinkProcess.getThresholdDetector().getThresholdProcess().getRegionDataBlock().addPamData(regionDataUnit);
			}
		}

		trackDataUnit.setEmbryonic(true);
		trackDataBlock.addPamData(trackDataUnit);
	}
	
	private void extendDataUnit(TrackChain chain, DetectedRegion region) {
		TrackLinkDataUnit trackDataUnit = chain.getParentDataUnit();
		if (trackDataUnit == null) {
			return;
		}
		RegionDataUnit regionDataUnit = new RegionDataUnit(region.getTimeMilliseconds(), region.getSonarId(), region);
		trackLinkProcess.getThresholdDetector().getThresholdProcess().getRegionDataBlock().addPamData(regionDataUnit);
		trackDataUnit.addNewSubDetection(regionDataUnit);
		trackDataBlock.updatePamData(trackDataUnit, region.getTimeMilliseconds());
		
	}

	/**
	 * Finish it - make data units, save to datablock, database, etc. 
	 * @param chain
	 */
	private void completeChain(TrackChain chain) {
//		TrackLinkDataUnit trackDataUnit = new TrackLinkDataUnit(chain);
//		/*
//		 *  me thinks that we're first going to have to make all the sub detections and
//		 *  add them to this super detection 
//		 */
//		synchronized(chain) {
//			for (DetectedRegion region : chain.getRegions()) {
//				RegionDataUnit regionDataUnit = new RegionDataUnit(region.getTimeMilliseconds(), region.getSonarId(), region);
//				trackDataUnit.addNewSubDetection(regionDataUnit);
//				trackLinkProcess.getThresholdDetector().getThresholdProcess().getRegionDataBlock().addPamData(regionDataUnit);
//			}
//		}
		TrackLinkDataUnit trackDataUnit = chain.getParentDataUnit();
		trackDataUnit.setEmbryonic(false);
		trackDataBlock.updatePamData(trackDataUnit, trackDataUnit.getEndTimeInMilliseconds());
		
//		trackDataBlock.addPamData(trackDataUnit);
	}

	/**
	 * Tests to see if a chain is worth keeping. May do something more sophisticated here, such as linking 
	 * to other recently completed chains in a third layer of detection ? but not now. 
	 * @param chain
	 * @return
	 */
	private boolean wantChain(TrackChain chain) {
		TrackLinkParameters trackParams = trackLinkProcess.trackLinkParams;
		return (chain.getChainLength() >= trackParams.minTrackPoints && 
				chain.getEnd2EndMetres() >= trackParams.minStraightLength &&
				chain.getWobblyLength() >= trackParams.minWobblyLength);
	}

	/**
	 * Score a match between trackchains and new regions
	 * @param trackChain
	 * @param regionDataBlock
	 * @return 0 = no match. Not sure what other numbers mean !
	 */
	private double matchRegion(TrackChain trackChain, DetectedRegion region) {
		TrackLinkParameters params = trackLinkProcess.trackLinkParams;
		if (region.getTimeMilliseconds() - trackChain.getLastTime() > params.maxTimeSeparation) {
			return 0;
		}
		DetectedRegion lastRegion = trackChain.getLastRegion();
		
		// check size since it's quickest
		double sizeRatio = region.getObjectSize() / lastRegion.getObjectSize();
		if (sizeRatio < 1) {
			sizeRatio = 1./sizeRatio;
		}
		if (sizeRatio > params.maxSizeRatio) {
			return 0;
		}
		
		// get distance to last point. 
//		double dist = getVectorMagnitude(newVec);
		double jumpTime = (double) (region.getTimeMilliseconds() - trackChain.getLastTime())/1000.;
		/*
		 *  if the track is very short, we want to only go on proximity and not worry too much about velocity
		 *  since that may be quite unstable at the start of a track. However things are moving fast so if we were
		 *  allowing 5m/s and a second, then we'd be jumping up to 5m ! so only do this if the track has < 3 points in 
		 *  it.  
		 */
		double distanceScore = 0;
		double maxDistance;
		double[] predictedPos = new double[2];
		if (trackChain.getChainLength() < 3) {
			maxDistance = params.maxSpeed * (trackChain.getTrackDuration() + .2);
			predictedPos[0] = lastRegion.getPeakX();
			predictedPos[1] = lastRegion.getPeakY();
		}
		else {
			maxDistance = params.maxSpeed * jumpTime;
			double[] velocity = trackChain.getEndsVelocity();
			predictedPos[0] = lastRegion.getPeakX() + velocity[0]*jumpTime;
			predictedPos[1] = lastRegion.getPeakY() + velocity[1]*jumpTime;
		}
		// vector from where we think we should be to the current region. 
		double[] newVec = {region.getPeakX() - predictedPos[0], region.getPeakY() - predictedPos[1]};
		
		double score = params.maxSpeed*jumpTime / getVectorMagnitude(newVec);
		if (score < 1) {
			return 0;
		}
		/*
		 *  can be infinite if the distance between the predicted and actual position is 
		 *  zero - happens most at start of track when there is no interpolation.  
		 */
//		if (Double.isInfinite(score)) {
//			System.out.println("Silly score " + score);
//		}
		
		return score-1; // should be a value > 0 if the point was close to the end of the Track.class 
	}
	
	/**
	 * Get the magnitude of the vector. 
	 * @param vec
	 * @return magnitude metres. 
	 */
	private double getVectorMagnitude(double[] vec) {
		return Math.sqrt(Math.pow(vec[0], 2) + Math.pow(vec[1], 2));
	}
	
	/**
	 * Get the direction of the vector as a bearing from north. 
	 * @param vec
	 * @return bearing from N (radians)
	 */
	private double getVectorBearing(double[] vec) {
		double bear = Math.atan2(vec[0], vec[1]);
		return bear;
		
	}


}
