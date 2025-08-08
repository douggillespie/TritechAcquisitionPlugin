package tritechplugins.detect.threshold;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;

public class ThresholdOfflineTask extends OfflineTask {

	private ThresholdProcess thresholdProcess;
	private ImageDataBlock imageDataBlock;
	private MultiFileCatalog fileCatalog;
	
	private int imagesDone;
	private long processStart;
	private ThresholdDetector thresholdDetector;

	public ThresholdOfflineTask(ThresholdDetector thresholdDetector, ThresholdProcess thresholdProcess, ImageDataBlock imageDataBlock) {
		super(imageDataBlock);
		this.thresholdDetector =thresholdDetector;
		this.thresholdProcess = thresholdProcess;
		this.imageDataBlock = imageDataBlock;
//		fileCatalog = 
		fileCatalog = imageDataBlock.findFileCatalog();
		addRequiredDataBlock(imageDataBlock);
		addAffectedDataBlock(thresholdProcess.getRegionDataBlock());
		addAffectedDataBlock(thresholdDetector.getTrackLinkProcess().getTrackLinkDataBlock());
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		thresholdDetector.showSettingsDialog(null);
		return true;
	}

	@Override
	public String getName() {
		return "Gemini treshold detector";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		/* 
		 * data units arriving here should not have had a full load, so will need
		 * to do that and then to free the data after processing. 
		 */
		if (fileCatalog == null) {
			return false;
		}
		ImageDataUnit imageDataUnit = (ImageDataUnit) dataUnit;
		if (imageDataUnit.getGeminiImage() == null) {
			return false; // it's status data. 
		}
		GeminiImageRecordI record = imageDataUnit.getGeminiImage();
		if (record.isFullyLoaded() == false) {
			// really inefficient and needs serious sorting out. 
			record = fileCatalog.findRecordForTime(record.getDeviceId(), record.getRecordTime());
			imageDataUnit.setGeminiImage(record);
		}
//		byte[] data = record.getImageData();
		
		imagesDone++;
		thresholdProcess.addData(imageDataBlock, imageDataUnit);
		
		record.freeImageData();
		
		return false;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {

	}

	@Override
	public void loadedDataComplete() {

	}

	@Override
	public void prepareTask() {
		super.prepareTask();
		imagesDone = 0;
		processStart = System.currentTimeMillis();
	}

	@Override
	public void completeTask() {
		super.completeTask();
		System.out.printf("Offline gemini processing task processed %d images in %dms\n", imagesDone, System.currentTimeMillis()-processStart);
	}

}
