package tritechplugins.acquire;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.GLFStatusData;

public class SonarStatusDataUnit extends PamDataUnit {

	private GLFStatusData statusData;
	private boolean pamStarted;

	public SonarStatusDataUnit(long timeMilliseconds, boolean pamStarted, GLFStatusData statusData) {
		super(timeMilliseconds);
		this.pamStarted = pamStarted;
		this.statusData = statusData;
	}

	/**
	 * @return the statusData
	 */
	public GLFStatusData getStatusData() {
		return statusData;
	}

	/**
	 * @return the pamStarted
	 */
	public boolean isPamStarted() {
		return pamStarted;
	}


}
