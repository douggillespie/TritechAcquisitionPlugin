package tritechplugins.detect.threshold.stats;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class DetStatsDataBlock extends PamDataBlock<DetStatsDataUnit> {

	/**
	 * Interval for output of detector stats
	 */
	public static final long DETSTATAINTERVAL = 60000;

	public DetStatsDataBlock(PamProcess parentProcess) {
		super(DetStatsDataUnit.class, "Sonar Detector Stats", parentProcess, 0);
	}

}
