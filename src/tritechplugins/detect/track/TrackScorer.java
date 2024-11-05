package tritechplugins.detect.track;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import PamUtils.PamArrayUtils;
import tritechgemini.detect.DetectedRegion;

/**
 * Function(s) to get a quality score for a track. 
 * @author dg50
 *
 */
public class TrackScorer {

	/**
	 * Points to assess track velocity vector over. 
	 */
	private int velocityPoints = 5;
	
	/**
	 * Start index for track scoring. Must be 2 or greater. 
	 */
	int scoreStartIndex = 2;

	/**
	 * Score a track train. 
	 * @param trackChain
	 * @return number between 0 (rubbish) and 1 (good). 
	 */
	public double scoreTrack(TrackChain trackChain) {
		
		int[] sonarIds = trackChain.getSonarIds();
		/*
		 * will score track parts on each sonar separately, since a bit
		 * of spatial jittering about, perhaps caused by differences in SoS
		 * measurement on the two sonars would make a very poor score. 
		 */
		double[] scores = new double[sonarIds.length];
		double bestScore = 0;
		for (int i = 0; i < sonarIds.length; i++) {
			scores[i] = scoreTrack(trackChain, sonarIds[i]);
			bestScore = Math.max(bestScore, scores[i]);
		}
		
		return bestScore;
	}

	/**
	 * Score a track, only using data from one sonar. 
	 * @param regions
	 * @param i
	 * @return
	 */
	private double scoreTrack(TrackChain trackChain, int sonarId) {
		LinkedList<DetectedRegion> regions = trackChain.getRegions();
		double prevX = Double.NaN, prevY = Double.NaN;
		long prevMillis;
		
		ArrayList<DetectedRegion> sonarPoints = trackChain.getSonarRegions(sonarId);
		if (sonarPoints.size() < 3) {
			return 0;
		}
		
		// use array list and loop so can easily pick specific indexes. 
		double predX, predY;
		int nDiff = 0;
		double totalDiff = 0;
		// start at the third point along the track. 
		double scores[] = new double[sonarPoints.size()-scoreStartIndex];
		for (int i = scoreStartIndex, scoreInd = 0; i < sonarPoints.size(); i++, scoreInd++) {
			int startInd = Math.max(0,  i-velocityPoints );
			DetectedRegion thisPt = sonarPoints.get(i);
			DetectedRegion prevPt = sonarPoints.get(i-1);
			DetectedRegion startPt = sonarPoints.get(startInd);

			// time between two latest points
			double stepT = (double) (thisPt.getTimeMilliseconds()-prevPt.getTimeMilliseconds())/1000.;
			// time from previous point to start point
			double totalT = (double) (prevPt.getTimeMilliseconds()-startPt.getTimeMilliseconds())/1000.;
			// predictions of where we should be based on trajectory. 
			predX = prevPt.getPeakX() + (prevPt.getPeakX()-startPt.getPeakX()) * stepT / totalT;
			predY = prevPt.getPeakY() + (prevPt.getPeakY()-startPt.getPeakY()) * stepT / totalT;
			
			double pDiff = Math.sqrt(Math.pow(thisPt.getPeakX()-predX,2) + Math.pow(thisPt.getPeakY()-predY, 2));
			scores[scoreInd] = -Math.log10(Math.max(pDiff, .01));
			totalDiff += pDiff;
		}
		// now take the median score. 
		double median = PamArrayUtils.median(scores);
		
		return median;
	}
	
//	/**
//	Moved  to TrackChain class so the method can be correctly synchronized.
//	 * Get the track points for a single sonar. 
//	 * @param regions
//	 * @param sonarId
//	 * @return
//	 */
//	private ArrayList<DetectedRegion> getSonarRegions(List<DetectedRegion> regions, int sonarId) {
//		ArrayList<DetectedRegion> sonarPoints = new ArrayList<>(regions.size());
//		Iterator<DetectedRegion> it = regions.iterator();
//		while (it.hasNext()) {
//			DetectedRegion region = it.next();
//			if (region.getSonarId() == sonarId) {
//				sonarPoints.add(region);
//			}
//		}
//		return sonarPoints;
//	}
}
