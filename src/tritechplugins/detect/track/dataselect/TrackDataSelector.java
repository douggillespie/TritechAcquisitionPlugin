package tritechplugins.detect.track.dataselect;

import java.util.LinkedList;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.track.TrackChain;
import tritechplugins.detect.track.TrackLinkDataBlock;
import tritechplugins.detect.track.TrackLinkDataUnit;

public class TrackDataSelector extends DataSelector {
	
	private TrackDataSelectorParams trackDataSelectorParams;
	
	public TrackDataSelector(TrackLinkDataBlock trackLinkDataBlock, String selectorName) {
		super(trackLinkDataBlock,selectorName, false);
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof TrackDataSelectorParams) {
			trackDataSelectorParams = (TrackDataSelectorParams) dataSelectParams;
		}
	}

	@Override
	public TrackDataSelectorParams getParams() {
		if (trackDataSelectorParams == null) {
			trackDataSelectorParams = new TrackDataSelectorParams();
		}
		return trackDataSelectorParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new TrackDataSelectorPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		TrackLinkDataUnit trackDataUnit = (TrackLinkDataUnit) pamDataUnit;
		TrackChain chain = trackDataUnit.getTrackChain();
		TrackDataSelectorParams params = getParams();
		double wobLen = chain.getWobblyLength();
//		if (pamDataUnit.getUID() == 89000015) {
//			System.out.println("UID " + pamDataUnit.getUID());
//		}
		double e2eMetres = chain.getEnd2EndMetres();
		if (e2eMetres < params.minLength || wobLen == 0) {
			return 0.;
		}
		double strightness = chain.getEnd2EndMetres() / wobLen;
		if (strightness < params.minStraightness) {
			return 0.;
		}
		if (chain.getChainLength() < params.minLength) {
			return 0.;
		}
		if (trackDataUnit.getDurationInMilliseconds() < params.minDuration*1000) {
			return 0.;
		}
		if (chain.getPointRate() < params.minPointRate) {
			return 0;
		}
		if (params.maxPointsPerFrame > 0 && trackDataUnit.getMaxFrameDetectionCount() > params.maxPointsPerFrame) {
			return 0;
		}
		if (chain.getTrackLinkScore() < params.minTrackScore) {
			return 0;
		}		
		if (params.vetoXzero && isAllX0(chain)) {
			return 0.;
		}
		
		return 1.;
	}

	private boolean isAllX0(TrackChain chain) {
		LinkedList<DetectedRegion> regions = chain.getRegions();
		int onZCount = 0, offZCount = 0;
		double vetoAngle = Math.toRadians(4);
		for (DetectedRegion region : regions) {
//			System.out.printf("Min bearing %3.1f max %3.1f\n", 
//			region.getMinBearing(), region.getMaxBearing());
			if (region.getMinBearing() > vetoAngle || region.getMaxBearing() < -vetoAngle) {
				offZCount++;
//				System.out.printf("Accept: Min bearing %3.1f max %3.1f\n", 
//						region.getMinBearing(), region.getMaxBearing());
			}
			else {
				onZCount++;
//				System.out.printf("Veto: Min bearing %3.1f max %3.1f\n", 
//						region.getMinBearing(), region.getMaxBearing());
			}
		}
		return offZCount <= 2;
	}

}
