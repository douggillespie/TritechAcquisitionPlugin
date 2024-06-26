package tritechplugins.detect.threshold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.Deflater;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.background.BackgroundManager;
import generalDatabase.DBControlUnit;
import tritechgemini.detect.DetectedRegion;
import tritechgemini.detect.RegionDetector;
import tritechgemini.detect.TwoThresholdDetector;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.detect.threshold.background.ThresholdBackgroundDataUnit;
import tritechplugins.detect.threshold.rangefilter.ImageRangeFilter;
import tritechplugins.detect.track.TrackLinkProcess;
import warnings.PamWarning;
import warnings.WarningSystem;

public class ChannelDetector {

	private ThresholdDetector thresholdDetector;
	private ThresholdProcess thresholdProcess;
	private int sonarId;
	
	private RegionDetector regionDetector;
	
	private BackgroundRemoval backgroundRemoval;
//	private RegionDataBlock regionDataBlock;
	
	private static final int MAX_FRAME_REGIONS = 1000;
	
	private PamWarning regionWarning = new PamWarning("Tritech Detector", "Too many", 0);
	
	private long lastBackgroundWrite = 0;
	
	private ImageRangeFilter imageRangeFilter;
	
	/**
	 * Detector for a single sonar. 
	 * @param thresholdDetector
	 * @param thresholdProcess
	 */
	public ChannelDetector(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess, int sonarId) {
		this.thresholdDetector = thresholdDetector;
		this.thresholdProcess = thresholdProcess;
		this.sonarId = sonarId;
		
		backgroundRemoval = new BackgroundRemoval();
		regionDetector = new TwoThresholdDetector();
		
//		regionDataBlock = thresholdProcess.getRegionDataBlock();
		
		imageRangeFilter = new ImageRangeFilter();
	}

	/**
	 * New data, either real time or in offline processing. 
	 * @param imageData
	 */
	public List<DetectedRegion> newData(ImageDataUnit imageDataUnit) {
		if (lastBackgroundWrite == 0) {
			lastBackgroundWrite = imageDataUnit.getTimeMilliseconds();
		}
		GeminiImageRecordI image = imageDataUnit.getGeminiImage();
		byte[] imageData = image.getImageData();
		if (imageData == null) {
			return null;
		}
		ThresholdParams params = thresholdDetector.getThresholdParams();
		backgroundRemoval.setTimeConstant(params.backgroundTimeConst);
		backgroundRemoval.setRemovalScale(params.backgroundScale);
		backgroundRemoval.setRemovalScale(params.backgroundScale, params.backgroundSTDs);
//		backgroundRemoval.setRemovalScale(1.5); // WTF is this doing here ? Was it there for all processing ? 
		byte[] filteredData;
		if (params.filterRange) {
			filteredData = imageRangeFilter.filterImage(imageData, image.getnBeam(), image.getnRange());
		}
		else {
			filteredData = imageData;
		}
		byte[] noBackground = backgroundRemoval.removeBackground(filteredData, image.getnBeam(), image.getnRange(), true);
		
		if (imageDataUnit.getTimeMilliseconds()-lastBackgroundWrite > params.backgroundIntervalSecs*1000) {
			writeBackground(imageDataUnit, lastBackgroundWrite, imageDataUnit.getTimeMilliseconds());
			lastBackgroundWrite = imageDataUnit.getTimeMilliseconds();
		}
		
		thresholdDetector.notifyRawUpdate(sonarId, imageData);
		thresholdDetector.notifyTreatedUpdate(sonarId, noBackground);
		
		/*
		 * Can escape here if the background has recently been reset in order to avoid the problem of lots
		 * of false dets firing off as things settle. 
		 */
		if (backgroundRemoval.getUpdateCount() < params.backgroundTimeConst*2) {
			return null;
		}
		
		GeminiImageRecordI clonedImage = image.clone();
		clonedImage.setImageData(noBackground);
		
		regionDetector.setMinObjectSize(params.minSize);
		regionDetector.setMaxObjectSize(params.maxSize);
		ArrayList<DetectedRegion> regions = regionDetector.detectRegions(image, clonedImage, params.highThreshold, params.lowThreshold, params.connectionType);
		
		if (regions == null || regions.size() == 0 ) {
			return null;
		}
		if (regions.size() > MAX_FRAME_REGIONS) {
			regionWarning.setEndOfLife(6000);
			String msg = String.format("High detection count: %d on sonar %d", regions.size(), sonarId);
			regionWarning.setWarningMessage(msg);
			regionWarning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(regionWarning);
			return null;
		}
		
				
		/*
		 * Filter the regions somehow ...
		 * Might be better to copy into a new list, since this is an array list and removing
		 * items is inefficient. 
		 */
		ListIterator<DetectedRegion> it = regions.listIterator();
		while (it.hasNext()) {
			DetectedRegion region = it.next();
			if (wantRegion(image, region, imageData) == false) {
				it.remove();
			}
		}

//		mergeOverlaps(regions);
		
		return regions;
		/*
		 * Make data units out of what's left. 
		 */
//		for (DetectedRegion region : regions) {
//			RegionDataUnit rdu = new RegionDataUnit(image.getRecordTime(), image.getDeviceId(), region);
//			regionDataBlock.addPamData(rdu);
//			if (thresholdDetector.isViewer()) {
//				regionDataBlock.getLogging().logData(DBControlUnit.findConnection(), rdu);
//			}
//		}
		
//		System.out.printf("Detected %3d regions, retained %d on sonar %d at %s\n", nFound, regions.size(), sonarId, PamCalendar.formatDBDateTime(imageDataUnit.getTimeMilliseconds(), true));
		
	}

	/**
	 * Merge any overlapping regions. 
	 * @param regions
	 */
	private void mergeOverlaps(ArrayList<DetectedRegion> regions) {
		int n = regions.size();
//		boolean[] merged = new boolean() 
		for (int i = 0; i < n; i++) {
			DetectedRegion r1 = regions.get(i);
			if (r1 == null) {
				continue;
			}
			for (int j = i+1; j < n; j++) {
				DetectedRegion r2 = regions.get(j);
				if (r2 == null) {
					continue;
				}
				if (r1.overlaps(r2)) {
//					System.out.printf("%s, Region %s overlaps %s\n", 
//							PamCalendar.formatDBDateTime(r1.getTimeMilliseconds(), true), r1, r2);
//					if (r2.getObjectSize() > r1.getObjectSize()) {
////						keep the biggest. 
//						regions.set(i,  r2);
//						r1 = r2;
//					}
					r1.merge(r2);
					regions.set(j, null);
				}
			}
		}
		Iterator<DetectedRegion> it = regions.iterator();
		while (it.hasNext()) {
			DetectedRegion region = it.next();
			if (region == null) {
				it.remove();
			}
		}
	}

	private void writeBackground(ImageDataUnit imageDataUnit, long lastBackgroundWrite2, long timeMilliseconds) {
		GeminiImageRecordI geminiImage = imageDataUnit.getGeminiImage();
		if (geminiImage instanceof GLFImageRecord == false) {
			// this system only works with GLF data, not with ECD data
			return;
		}
		geminiImage = geminiImage.clone(); // take a clone of latest image. 
		byte[] bgnd = backgroundRemoval.getBackground();
		/*
		 *  check the size of the background array which shouldn't be smaller but may be 
		 *  a bit larger than the current image.  
		 */
		int sz = geminiImage.getnRange()*geminiImage.getnBeam();
		if (sz != bgnd.length) {
			bgnd = Arrays.copyOf(bgnd, sz);
		}
		geminiImage.setImageData(bgnd);
		
		ThresholdBackgroundDataUnit dbdu = new ThresholdBackgroundDataUnit(lastBackgroundWrite2, imageDataUnit.getChannelBitmap(),
				timeMilliseconds-lastBackgroundWrite2, (GLFImageRecord) geminiImage);
		
		// now try to find the datablock for the Tracks (not the regions) and add to that
		TrackLinkProcess tlp = thresholdDetector.getTrackLinkProcess();
		if (tlp == null) {
			return;
		}
		BackgroundManager bgndManager = tlp.getTrackLinkDataBlock().getBackgroundManager();
		if (bgndManager != null) {
			if (PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING) {
				bgndManager.addData(dbdu);
			}
		}
		
		/*
		 *  we now have an image with all the meta data of the last image, but with the
		 *  background array instead of the raw data.  
		 */
		// see how well it compresses. 
//		Deflater shrinker = new java.util.zip.Deflater();
//		shrinker.setInput(bgnd);
//		int nbytes = shrinker.deflate(bgnd);
//		System.out.printf("Sonar %d Shrunk %d butes to %d = %3.2f%%\n", geminiImage.getDeviceId(), sz, nbytes, 100.*(double)nbytes/(double)sz);
	}

	private boolean wantRegion(GeminiImageRecordI image, DetectedRegion region, byte[] imageData) {
		if (region == null) {
			return false; // case for regions that were merged into others. 
		}
		// get rough coordinates of the corners in x,y
		int minPix = 10;
		int minArea = 0;
		if (region.getRegionSize() < minPix) {
			return false;
		}
		double sb1 = Math.sin(region.getMinBearing());
		double sb2 = Math.sin(region.getMinBearing());
		double cb1 = Math.cos(region.getMaxBearing());
		double cb2 = Math.cos(region.getMaxBearing());
//		double rangeScale = image.getMaxRange() / image.getnRange();
//		double flx = region.getMinRange() * sb1;
//		double fly = region.getMinRange() * cb1;
//		double rlx = region.getMaxRange() * sb1;
//		double rly = region.getMaxRange() * cb1;
//		double frx = region.getMinRange() * sb2;
//		double fry = region.getMinRange() * cb2;
//		double rrx = region.getMaxRange() * sb2;
//		double rry = region.getMaxRange() * cb2;
		/*
		 * nominal are is the distance across, which is angle* mean range * the difference in range. 
		 */
		double nomAreaMetres = Math.abs((region.getMaxBearing()-region.getMinBearing()) * (region.getMaxRange()+region.getMinRange())/2. * (region.getMaxRange()-region.getMinRange()));
		if (nomAreaMetres < minArea) {
			return false;
		}
		
		return true;
	}
	
	

}
