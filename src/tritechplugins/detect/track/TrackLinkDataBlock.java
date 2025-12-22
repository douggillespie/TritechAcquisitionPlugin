package tritechplugins.detect.track;

import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import pamScrollSystem.ViewLoadObserver;
import tritechplugins.detect.threshold.RegionDataUnit;

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

}
