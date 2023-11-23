package tritechplugins.acquire.swing.framerate;

import java.util.HashMap;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class FrameRateDataBlock extends PamDataBlock<FrameRateDataUnit> {

	private HashMap<Integer, Long> sonarIds = new HashMap<>();
	
	private long lastTimeMilliseconds = 0;
	
	public FrameRateDataBlock(PamProcess parentProcess) {
		super(FrameRateDataUnit.class, "Frame Rate", parentProcess, 0);
	}

	@Override
	public void addPamData(FrameRateDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
		sonarIds.put(pamDataUnit.getSonarId(), pamDataUnit.getTimeMilliseconds());
		lastTimeMilliseconds = Math.max(lastTimeMilliseconds, pamDataUnit.getTimeMilliseconds());
	}

	public long getLastTimeMilliseconds() {
		return lastTimeMilliseconds;
	}

	public HashMap<Integer, Long> getSonarIds() {
		return sonarIds;
	}
}
