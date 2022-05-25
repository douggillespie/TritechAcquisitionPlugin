package tritechplugins.acquire;

import java.io.Serializable;

import geminisdk.structures.ChirpMode;
import geminisdk.structures.RangeFrequencyConfig;

public class TritechDaqParams implements Serializable, Cloneable{


	public static final long serialVersionUID = 1L;

	public static final int RUN_ACQUIRE = 0;
	public static final int RUN_REPROCESS = 1;
	public static final int RUN_SIMULATE = 2;
	
	private int runMode = RUN_ACQUIRE;
	
	private String offlineFileFolder = "C:\\GeminiData\\LD";
	
	private boolean offlineSubFolders = false;
	
	private int range = 60;
	
	private int gain = 50;
	
	private int chirpMode = ChirpMode.CHIRP_ENABLED;
	
	/**
	 * 1200i only
	 */
	private int rangeConfig = RangeFrequencyConfig.FREQUENCY_AUTO;

	/**
	 * 1200i only
	 */
	private double rangeRangeThreshold = 40;

	/**
	 * @return the offlineFileFolder
	 */
	public String getOfflineFileFolder() {
		return offlineFileFolder;
	}

	/**
	 * @param offlineFileFolder the offlineFileFolder to set
	 */
	public void setOfflineFileFolder(String offlineFileFolder) {
		this.offlineFileFolder = offlineFileFolder;
	}

	@Override
	public TritechDaqParams clone() {
		try {
			return (TritechDaqParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the offlineSubFolders
	 */
	public boolean isOfflineSubFolders() {
		return offlineSubFolders;
	}

	/**
	 * @param offlineSubFolders the offlineSubFolders to set
	 */
	public void setOfflineSubFolders(boolean offlineSubFolders) {
		this.offlineSubFolders = offlineSubFolders;
	}

	/**
	 * @return the range
	 */
	public int getRange() {
		if (range == 0) {
			range = 30;
		}
		return range;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * @return the gain
	 */
	public int getGain() {
		if (gain == 0) {
			gain = 50;
		}
		return gain;
	}

	/**
	 * @param gain the gain to set
	 */
	public void setGain(int gain) {
		this.gain = gain;
	}

	/**
	 * @return the chirpMode
	 */
	public int getChirpMode() {
		return chirpMode;
	}

	/**
	 * @param chirpMode the chirpMode to set
	 */
	public void setChirpMode(int chirpMode) {
		this.chirpMode = chirpMode;
	}

	/**
	 * @return the rangeConfig
	 */
	public int getRangeConfig() {
		return rangeConfig;
	}

	/**
	 * @param rangeConfig the rangeConfig to set
	 */
	public void setRangeConfig(int rangeConfig) {
		this.rangeConfig = rangeConfig;
	}

	/**
	 * @return the rangeRangeThreshold
	 */
	public double getRangeRangeThreshold() {
		return rangeRangeThreshold;
	}

	/**
	 * @param rangeRangeThreshold the rangeRangeThreshold to set
	 */
	public void setRangeRangeThreshold(double rangeRangeThreshold) {
		this.rangeRangeThreshold = rangeRangeThreshold;
	}
	
	public static int[] getRunModes() {
		int[] modes = {RUN_ACQUIRE, RUN_REPROCESS};
		return modes;
	}
	
	public static String getRunModeName(int mode) {
		switch (mode) {
		case RUN_ACQUIRE:
			return "Acquire sonar data";
		case RUN_REPROCESS:
			return "Reprocess files";
		case RUN_SIMULATE:
			return "Simulate data";
		}
		return "Unknown run mode";
	}

	/**
	 * @return the runMode
	 */
	public int getRunMode() {
		return runMode;
	}

	/**
	 * @param runMode the runMode to set
	 */
	public void setRunMode(int runMode) {
		this.runMode = runMode;
	}

}
