package tritechplugins.detect.threshold;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import PamUtils.PamCalendar;
import generalDatabase.DBControlUnit;
import tritechgemini.detect.DetectedRegion;
import tritechgemini.detect.RegionDetector;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataUnit;

public class ChannelDetector {

	private ThresholdDetector thresholdDetector;
	private ThresholdProcess thresholdProcess;
	private int sonarId;
	
	private RegionDetector regionDetector;
	
	private BackgroundRemoval backgroundRemoval;
	private RegionDataBlock regionDataBlock;
	
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
		regionDetector = new RegionDetector();
		
		regionDataBlock = thresholdProcess.getRegionDataBlock();
	}

	/**
	 * New data, either real time or in offline processing. 
	 * @param imageData
	 */
	public List<DetectedRegion> newData(ImageDataUnit imageDataUnit) {
		GeminiImageRecordI image = imageDataUnit.getGeminiImage();
		byte[] imageData = image.getImageData();
		if (imageData == null) {
			return null;
		}
		ThresholdParams params = thresholdDetector.getThresholdParams();
		backgroundRemoval.setTimeConstant(params.backgroundTimeConst);
		backgroundRemoval.setRemovalScale(params.backgroundScale);
		backgroundRemoval.setRemovalScale(1.5);
				
		byte[] noBackground = backgroundRemoval.removeBackground(imageData, image.getnBeam(), image.getnRange(), true);
		
		thresholdDetector.notifyRawUpdate(sonarId, imageData);
		thresholdDetector.notifyTreatedUpdate(sonarId, noBackground);
		
		GeminiImageRecordI clonedImage = image.clone();
		clonedImage.setImageData(noBackground);
		
		regionDetector.setMinObjectSize(params.minSize);
		regionDetector.setMaxObjectSize(params.maxSize);
		ArrayList<DetectedRegion> regions = regionDetector.detectRegions(clonedImage, params.highThreshold, params.lowThreshold, params.connectionType);
		
		if (regions == null || regions.size() == 0) {
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

	private boolean wantRegion(GeminiImageRecordI image, DetectedRegion region, byte[] imageData) {
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
