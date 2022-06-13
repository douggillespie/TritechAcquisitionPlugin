package tritechplugins.detect.track;

import java.util.HashMap;
import java.util.List;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import annotation.handler.AnnotationHandler;
import annotation.handler.ManualAnnotationHandler;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.threshold.RegionDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.threshold.ThresholdProcess;
import tritechplugins.display.swing.overlays.TrackSymbolManager;

/**
 * Process that works with the threshold detector to join detections into tracks. 
 * this is written semi-separately on the basis that both the detector and the tacker
 * may get rewritten with something better in future iterations, so may need to 
 * totally separate sometime in the future. 
 * @author dg50
 *
 */
public class TrackLinkProcess extends PamProcess {

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
	
	/**
	 * Hash map for when sonars being processed separately
	 */
	private HashMap<Integer, TrackLinker> trackLinkers;
	
	/**
	 * simple reference for when being processed together. 
	 */
	private TrackLinker singleLinker;

	private ManualAnnotationHandler annotationHandler;

	private TrackLogging trackLogging;

	public TrackLinkProcess(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess) {
		super(thresholdDetector, null);
		this.thresholdDetector = thresholdDetector;
		trackLinkDataBlock = new TrackLinkDataBlock("Gemini Tracks", this);
		trackLogging = new TrackLogging(thresholdDetector, trackLinkDataBlock, true);
		trackLogging.setSubLogging(thresholdProcess.getRegionLogging());
		trackLinkDataBlock.SetLogging(trackLogging);
		annotationHandler = new ManualAnnotationHandler(thresholdDetector, trackLinkDataBlock);
		trackLinkDataBlock.setAnnotationHandler(annotationHandler);
		trackLinkDataBlock.setPamSymbolManager(new TrackSymbolManager(trackLinkDataBlock));
		addOutputDataBlock(trackLinkDataBlock);
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		regionDataBlock = thresholdDetector.getThresholdProcess().getRegionDataBlock();
		setParentDataBlock(regionDataBlock, true); // consider rethreading ?
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
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

	public void newRegionsList(int sonarId, long imageTime, List<DetectedRegion> regions) {
		TrackLinker trackLinker = findTrackLinker(sonarId);
		trackLinker.newImageRegions(imageTime, regions);
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

	/**
	 * @return the annotationHandler
	 */
	public ManualAnnotationHandler getAnnotationHandler() {
		return annotationHandler;
	}

	public void sortSQLLogging() {
		annotationHandler.addAnnotationSqlAddons(trackLogging);
	}
		
}
