package tritechplugins.acquire.swing.framerate;

import PamguardMVC.PamDataUnit;

public class FrameRateDataUnit extends PamDataUnit {

	private int sonarId;

	public FrameRateDataUnit(long timeMilliseconds, int sonarId) {
		super(timeMilliseconds);
		this.sonarId = sonarId;
	}

	public int getSonarId() {
		return sonarId;
	}


}
