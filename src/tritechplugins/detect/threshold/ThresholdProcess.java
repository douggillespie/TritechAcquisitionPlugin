package tritechplugins.detect.threshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.detect.swing.RegionOverlayDraw;
import tritechplugins.detect.track.TrackLinkProcess;
import tritechplugins.display.swing.overlays.SonarSymbolManager;

public class ThresholdProcess extends PamProcess {

	private ThresholdDetector thresholdDetector;
	private ImageDataBlock sourceDataBlock;
	private HashMap<Integer, ChannelDetector> channelDetectors = new HashMap<>();
	
	private RegionDataBlock regionDataBlock;
	
	private RegionLogging regionLogging;
	
	/**
	 * @return the regionLogging
	 */
	public RegionLogging getRegionLogging() {
		return regionLogging;
	}


	public ThresholdProcess(ThresholdDetector thresholdDetector) {
		super(thresholdDetector, null);
		this.thresholdDetector = thresholdDetector;
		regionDataBlock = new RegionDataBlock(thresholdDetector.getUnitName() + " targets", this);
		regionDataBlock.setPamSymbolManager(new SonarSymbolManager(regionDataBlock));
		addOutputDataBlock(regionDataBlock);
		regionLogging = new RegionLogging(thresholdDetector, regionDataBlock);
		// not sure I need this - logging function get called from the super detection clss. 
//		regionDataBlock.SetLogging(regionLogging);
		regionDataBlock.setPamSymbolManager(new SonarSymbolManager(regionDataBlock));
		regionDataBlock.setOverlayDraw(new RegionOverlayDraw());
		
	}


	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		ImageDataUnit imageData = (ImageDataUnit) arg;
		ChannelDetector cd = findChannelDetector(imageData.getGeminiImage().getDeviceId(), true);
		List<DetectedRegion> regions = cd.newData(imageData);
		TrackLinkProcess trackLinkProcess = thresholdDetector.getTrackLinkProcess();
		if (trackLinkProcess != null) {
			trackLinkProcess.newRegionsList(imageData.getGeminiImage().getDeviceId(), imageData.getTimeMilliseconds(), regions);
		}
	}


	@Override
	public void prepareProcess() {
		super.prepareProcess();
		ThresholdParams params = thresholdDetector.getThresholdParams();
		sourceDataBlock = (ImageDataBlock) PamController.getInstance().getDataBlockByLongName(processName);
		if (sourceDataBlock == null) {
			sourceDataBlock = findAnySource();
			if (sourceDataBlock != null) {
				params.imageDataSource = sourceDataBlock.getLongDataName();
			}
		}
		setParentDataBlock(sourceDataBlock);
	}


	/**
	 * Find the channel detector for the given sonar. 
	 * @param sonarId sonar id (not index since that's inconsistent)
	 * @param create create if not found
	 * @return channel detector or null. 
	 */
	private ChannelDetector findChannelDetector(int sonarId, boolean create) {
		ChannelDetector cd = channelDetectors.get(sonarId);
		if (cd == null && create) {
			cd = new ChannelDetector(thresholdDetector, this, sonarId);
			channelDetectors.put(sonarId, cd);
		}
		return cd;
	}

	@Override
	public void pamStart() {
//		trackLinkProcess.startProcess();
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	/**
	 * Find any data block. Generally there will be only one, so this should 
	 * just find the one from TritechAcquisition.
	 * @return image data block. 
	 */
	private ImageDataBlock findAnySource() {
		/*
		 * Consider moving this to a public static in imageDataBlock.
		 */
		 ArrayList<PamDataBlock> imageDataBlocks = PamController.getInstance().getDataBlocks(ImageDataUnit.class, true);
		 if (imageDataBlocks.size() > 0) {
			 return (ImageDataBlock) imageDataBlocks.get(0);
		 }
		 else {
			 return null;
		 }
	}


	/**
	 * @return the sourceDataBlock
	 */
	public ImageDataBlock getSourceDataBlock() {
		return sourceDataBlock;
	}


	/**
	 * @return the regionDataBlock
	 */
	public RegionDataBlock getRegionDataBlock() {
		return regionDataBlock;
	}
	
}
