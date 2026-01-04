package tritechplugins.detect.track;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import annotation.handler.AnnotationHandler;
import annotation.handler.ManualAnnotationHandler;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.detect.threshold.RegionDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.threshold.ThresholdProcess;
import tritechplugins.detect.threshold.background.ThresholdBackgroundManager;
import tritechplugins.detect.threshold.stats.DetStatsDataBlock;
import tritechplugins.detect.threshold.stats.DetStatsLogging;
import tritechplugins.detect.track.dataselect.TrackDataSelectCreator;
import tritechplugins.display.swing.overlays.TrackSymbolManager;

/**
 * Process that works with the threshold detector to join detections into tracks. 
 * this is written semi-separately on the basis that both the detector and the tacker
 * may get rewritten with something better in future iterations, so may need to 
 * totally separate sometime in the future. 
 * @author dg50
 *
 */
public class TrackLinkProcess extends PamProcess implements PamSettings {

	private ThresholdDetector thresholdDetector;
	/**
	 * @return the thresholdDetector
	 */
	public ThresholdDetector getThresholdDetector() {
		return thresholdDetector;
	}

	private RegionDataBlock regionDataBlock;
	
	private TrackLinkDataBlock trackLinkDataBlock;
	
	/**
	 * @return the trackLinkDataBlock
	 */
	public TrackLinkDataBlock getTrackLinkDataBlock() {
		return trackLinkDataBlock;
	}

	protected TrackLinkParameters trackLinkParams = new TrackLinkParameters();
	
	public TrackLinkParameters getTrackLinkParams() {
		return trackLinkParams;
	}

	public void setTrackLinkParams(TrackLinkParameters trackLinkParams) {
		this.trackLinkParams = trackLinkParams;
	}

	/**
	 * Hash map for when sonars being processed separately
	 */
	private HashMap<Integer, TrackLinker> trackLinkers;
	
	/**
	 * simple reference for when being processed together. 
	 */
	private TrackLinker singleLinker;

//	private ManualAnnotationHandler annotationHandler;

	private TrackLogging trackLogging;

private DetStatsDataBlock detStatsDataBlock;

private TritechAcquisition foundAcquisition;
	
	private static final String defaultName = "Gemini Tracks";

	public TrackLinkProcess(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess) {
		super(thresholdDetector, null);
		this.thresholdDetector = thresholdDetector;
		trackLinkDataBlock = new TrackLinkDataBlock(getDataName(), this);
		trackLogging = new TrackLogging(this, thresholdDetector, trackLinkDataBlock, true);
		trackLogging.setSubLogging(thresholdProcess.getRegionLogging());
		trackLinkDataBlock.SetLogging(trackLogging);
		trackLinkDataBlock.setBinaryDataSource(new TrackBinarySource(this, trackLinkDataBlock));
		trackLinkDataBlock.setDataSelectCreator(new TrackDataSelectCreator(trackLinkDataBlock));
//		annotationHandler = new ManualAnnotationHandler(thresholdDetector, trackLinkDataBlock);
//		trackLinkDataBlock.setAnnotationHandler(annotationHandler);
		trackLinkDataBlock.setPamSymbolManager(new TrackSymbolManager(trackLinkDataBlock));
		trackLinkDataBlock.setBackgroundManager(new ThresholdBackgroundManager(this, trackLinkDataBlock));
		addOutputDataBlock(trackLinkDataBlock);

		if (DetStatsDataBlock.DETSTATAINTERVAL > 0) {
			detStatsDataBlock = new DetStatsDataBlock(this);
			detStatsDataBlock.SetLogging(new DetStatsLogging(detStatsDataBlock, thresholdDetector.getUnitName()+"_stats"));
			addOutputDataBlock(detStatsDataBlock);
		}
		
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 *  wee bit of a mess since in V1 this was called "Gemini Tracks" but we need 
	 *  a more uniquename I think ? 
	 * @return more sensible name ? 
	 */
	private String getDataName() {
		return defaultName;
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		regionDataBlock = thresholdDetector.getThresholdProcess().getRegionDataBlock();
		setParentDataBlock(regionDataBlock, true); // consider rethreading ?
	}
	
	/**
	 * Get the region data block that feed the linker. 
	 * @return
	 */
	public RegionDataBlock findRegionDataBlock() {
		return thresholdDetector.getThresholdProcess().getRegionDataBlock();
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}
	
	public double[] getAbsoluteXY(DetectedRegion region) {
		TritechAcquisition daq = getTritechAcquisition();
		double x = region.getPeakX();
		double y = region.getPeakY();
		double[] xy = {x,y};
		if (daq != null) {
			xy = daq.getAbsoluteXY(region.getSonarId(), x, y);
		}
		return xy;
	}
	
	/**
	 * Get the upstream TritechAcquisition module. Remembers the
	 * result, so doesn't repeat a search once the module is found. 
	 * @return
	 */
	public TritechAcquisition getTritechAcquisition() {
		if (foundAcquisition == null) {
			foundAcquisition = findTritechAcquisition();
		}
		return foundAcquisition;
	}
	
	/***
	 * Find the upstream TritechAcquisition module. Don't call this directly
	 * but use the getTritechAcquisition function which will remember the result
	 * and avoid repeating the search every time it's needed. 
	 * @return upstream TritechAcquisition module, or null. 
	 */
	private TritechAcquisition findTritechAcquisition() {
		// work back through the parents. 
		PamProcess process = this;
		while (process != null) {
			if (process.getPamControlledUnit() instanceof TritechAcquisition) {
				return (TritechAcquisition) process.getPamControlledUnit();
			}
			process = process.getParentProcess();
		}
		return null;
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * this isn't being done this way. We're passing over lists of regions from a single
	 * image. 
	 */
//	@Override
//	public void newData(PamObservable o, PamDataUnit arg) {
//		RegionDataUnit regionDataUnit = (RegionDataUnit) arg;
//		TrackLinker trackLinker = findTrackLinker(regionDataUnit.getSonarId());
//		trackLinker.newData(regionDataUnit);
//	}

	public int newRegionsList(int sonarId, long imageTime, List<DetectedRegion> regions) {
		TrackLinker trackLinker = findTrackLinker(sonarId);
		return trackLinker.newImageRegions(imageTime, regions);
	}
	
	private TrackLinker findTrackLinker(int sonarId) {
		if (trackLinkParams.separateSonars) {
			TrackLinker linker = trackLinkers.get(sonarId);
			if (linker == null) {
				linker = new TrackLinker(this, sonarId);
				trackLinkers.put(sonarId, linker);
			}
			return linker;
		}
		else {
			if (singleLinker == null) {
				singleLinker = new TrackLinker(this, 0);
			}
			return singleLinker;
		}
	}

	public DetStatsDataBlock getDetStatsDataBlock() {
		return detStatsDataBlock;
	}

	@Override
	public String getUnitName() {
		return thresholdDetector.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Gemini Track Link Process";
	}

	@Override
	public Serializable getSettingsReference() {
		return trackLinkParams;
	}

	@Override
	public long getSettingsVersion() {
		return TrackLinkParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		trackLinkParams = (TrackLinkParameters) pamControlledUnitSettings.getSettings();
		trackLinkParams = trackLinkParams.clone();
		return true;
	}

	/**
	 * Called after data loaded from binary files in viewer mode and will add a count of how many detections
	 * per frame there are to each region and track. This can be used on the display to 
	 * cut overly busy frames from the display to remove clutter. 
	 */
	public void countFrameDetections() {

		/**
		 * Make a map of all frames then count how many items there were 
		 * per frame, then go back and add those data back into the regions. 
		 */
		HashMap<Long, Integer> frameCountMap = new HashMap<>();
		synchronized (regionDataBlock.getSynchLock()) {
			ListIterator<RegionDataUnit> iter = regionDataBlock.getListIterator(0);
			while (iter.hasNext()) {
				RegionDataUnit region = iter.next();
				Integer val = frameCountMap.get(region.getTimeMilliseconds());
				if (val == null) {
					frameCountMap.put(region.getTimeMilliseconds(), 1);
				}
				else {
					frameCountMap.put(region.getTimeMilliseconds(), val+1);
				}
			}
			iter = regionDataBlock.getListIterator(0);
			while (iter.hasNext()) {
				RegionDataUnit region = iter.next();
				Integer val = frameCountMap.get(region.getTimeMilliseconds());
				region.setFrameDetectionCount(val);
			}
		}
	}

//	/**
//	 * @return the annotationHandler
//	 */
//	public ManualAnnotationHandler getAnnotationHandler() {
//		return annotationHandler;
//	}
//
//	public void sortSQLLogging() {
//		annotationHandler.addAnnotationSqlAddons(trackLogging);
//	}
		
}
