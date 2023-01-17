package tritechplugins.detect.track.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import tritechplugins.detect.track.TrackLinkDataBlock;
import tritechplugins.detect.track.TrackLinkDataUnit;

public class TrackDataSelectCreator extends DataSelectorCreator {
	
	TrackLinkDataBlock trackLinkDataBlock;

	public TrackDataSelectCreator(TrackLinkDataBlock trackLinkDataBlock) {
		super(trackLinkDataBlock);
		this.trackLinkDataBlock = trackLinkDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new TrackDataSelector(trackLinkDataBlock, selectorName);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new TrackDataSelectorParams();
	}

}
