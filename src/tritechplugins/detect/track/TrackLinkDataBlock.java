package tritechplugins.detect.track;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import pamScrollSystem.ViewLoadObserver;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.ThresholdDetector;

public class TrackLinkDataBlock extends SuperDetDataBlock<TrackLinkDataUnit, RegionDataUnit> {

	private TrackLinkProcess trackLinkProcess;

	public TrackLinkDataBlock(String dataName, TrackLinkProcess trackLinkProcess) {
		super(TrackLinkDataUnit.class, dataName, trackLinkProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.trackLinkProcess = trackLinkProcess;
		setNaturalLifetimeMillis(2000);
	}


	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		
		trackLinkProcess.countFrameDetections();
		
		return loadOk;
	}


	@Override
	public boolean reattachSubdetections(ViewLoadObserver viewLoadObserver) {
		// this has already been done as data were loaded, so can return immediately
		//		return super.reattachSubdetections(viewLoadObserver);
		return true;
	}


	@Override
	public void clearAll() {
		/*
		 * Brian reversed the oder blocks are loaded in, so the Region datablock was getting cleared
		 * when it loaded after this, which wasn't good !
		 * So I've turned off automatic clearing of the RegionDatAblock, but we need to clear it when 
		 * this block clears, or it's going to grow and grow. 
		 */
		super.clearAll();
		PamDataBlock regionDatablock = findRegionBlock();
		if (regionDatablock != null) {
			regionDatablock.clearAll();
		}
	}


	private PamDataBlock findRegionBlock() {
		// TODO Auto-generated method stub
		ThresholdDetector detector = trackLinkProcess.getThresholdDetector();
		if (detector == null) return null;
		return detector.getThresholdProcess().getRegionDataBlock();
	}

}
