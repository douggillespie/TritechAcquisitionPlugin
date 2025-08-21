package tritechplugins.acquire;

/**
 * Additional sonar status data which is operational specific, 
 * e.g. things like PAMGuard shutting down due to out of water
 * @author dg50
 *
 */
public class OpsSonarStatusData {

	private int lastShutdownCode;
	
	/**
	 * Time of last shutdown error time. 
	 */
	private long lastShutdownEerrTime;

	/**
	 * @return the lastShutdownCode
	 */
	public int getLastShutdownCode() {
		return lastShutdownCode;
	}

	/**
	 * @param lastShutdownCode the lastShutdownCode to set
	 */
	public void setLastShutdownCode(int lastShutdownCode) {
		this.lastShutdownCode = lastShutdownCode;
	}

	/**
	 * @return the lastShutdownEerrTime
	 */
	public long getLastShutdownEerrTime() {
		return lastShutdownEerrTime;
	}

	/**
	 * @param lastShutdownEerrTime the lastShutdownEerrTime to set
	 */
	public void setLastShutdownEerrTime(long lastShutdownEerrTime) {
		this.lastShutdownEerrTime = lastShutdownEerrTime;
	}

}
