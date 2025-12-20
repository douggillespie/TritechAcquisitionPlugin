package tritechplugins.acquire;

import java.util.HashMap;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class SonarStatusDataBlock extends PamDataBlock<SonarStatusDataUnit> {

	private TritechDaqProcess tritechDaqProcess;
	
	private HashMap<Short, Long> lastLogTime = new HashMap<>();

	public SonarStatusDataBlock(TritechDaqProcess tritechDaqProcess) {
		super(SonarStatusDataUnit.class, "Sonar Status", tritechDaqProcess, 0);
		this.tritechDaqProcess = tritechDaqProcess;
	}

	@Override
	public void addPamData(SonarStatusDataUnit pamDataUnit, Long uid) {
		super.addPamData(pamDataUnit, uid);
		lastLogTime.put(pamDataUnit.getStatusData().m_deviceID,  pamDataUnit.getTimeMilliseconds());
	}
	
	/**
	 * Clear last log times. 
	 */
	public void clearLastLogTimes() {
		synchronized (lastLogTime) {
			lastLogTime.clear();			
		}
	}

	/**
	 * @return the lastLogTime
	 */
	public long getLastLogTime(short sonarId) {
		Long lastLog = null;
		synchronized (lastLogTime) {
			lastLog = lastLogTime.get(sonarId);
		}
		if (lastLog == null) {
			return 0;
		}
		return lastLog;
	}

	/**
	 * @param lastLogTime the lastLogTime to set
	 */
	public void setLastLogTime(short sonarId, long lastLogTime) {
		synchronized (this.lastLogTime) {
			this.lastLogTime.put(sonarId, lastLogTime);
		}
	}



}
