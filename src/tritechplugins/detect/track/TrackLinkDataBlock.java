package tritechplugins.detect.track;

import PamguardMVC.superdet.SuperDetDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;

public class TrackLinkDataBlock extends SuperDetDataBlock<TrackLinkDataUnit, RegionDataUnit> {

	public TrackLinkDataBlock(String dataName, TrackLinkProcess trackLinkProcess) {
		super(TrackLinkDataUnit.class, dataName, trackLinkProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		setNaturalLifetimeMillis(2000);
	}

}
