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
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.detect.threshold.background.ThresholdBackgroundDataUnit;
import tritechplugins.detect.threshold.rangefilter.ImageRangeFilter;
import tritechplugins.detect.track.TrackLinkProcess;
import warnings.PamWarning;
import warnings.WarningSystem;

public abstract class ChannelDetector {

	protected ThresholdDetector thresholdDetector;
	protected ThresholdProcess thresholdProcess;
	protected int sonarId;

	private boolean currentOutOfWater;
	
	/**
	 * Detector for a single sonar. 
	 * @param thresholdDetector
	 * @param thresholdProcess
	 */
	public ChannelDetector(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess, int sonarId) {
		this.thresholdDetector = thresholdDetector;
		this.thresholdProcess = thresholdProcess;
		this.sonarId = sonarId;
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


	/**
	 * New data, either real time or in offline processing. 
	 * @param imageData
	 */
	public abstract List<DetectedRegion> newData(ImageDataUnit imageDataUnit);

	public boolean wantRegion(GeminiImageRecordI image, DetectedRegion region, byte[] imageData) {
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

	/**
	 * New status data. Now received in both real time and offline. 
	 * @param imageData
	 */
	public void newStatusData(ImageDataUnit imageData) {
		SonarStatusData statusData = imageData.getSonarStatusData();
		GLFStatusData statusPacket = statusData.getStatusPacket();
		if (statusPacket == null) {
			return;
		}
		boolean oow = statusPacket.isOutOfWater();
		checkOOWChange(oow);
		currentOutOfWater = oow;
	}


	private void checkOOWChange(boolean oow) {
		if (currentOutOfWater == oow) {
			return;
		}
		oowStateChange(oow);
	}


	/**
	 * Called whenever the out of water state for this detector changes. 
	 * @param isOOW true if currently out of water. 
	 */
	protected abstract void oowStateChange(boolean isOOW);
	
	

}
