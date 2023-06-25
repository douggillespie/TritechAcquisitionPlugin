package tritechplugins.acquire;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import geminisdk.structures.ChirpMode;
import geminisdk.structures.RangeFrequencyConfig;

public class TritechDaqParams implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;

	public static final int RUN_ACQUIRE = 0;
	public static final int RUN_REPROCESS = 1;
	public static final int RUN_SIMULATE = 2;
	
	private int runMode = RUN_ACQUIRE;
	
	public static final int DEFAULT_SONAR_PARAMETERSET = -1;

	/*
	 * List of available playback speeds for offline analysis in 'normal' mode via 
	 * svs5 or via the pure Java reader -1 or 0 mean free run (-1 works with svs5). 
	 */
	public static final double[] playSpeeds = {-1, .5, 1, 2, 4, 8, 16};
	
	/**
	 * folder for both processing and reprocessing. Gets
	 * a bit confused when debugging, so may have to change. 
	 */
	private String offlineFileFolder = "C:\\GeminiData\\LD";
	
	private boolean offlineSubFolders = false;
	
	private int range = 60;
	
	private int gain = 50;
	
	private int chirpMode = ChirpMode.CHIRP_ENABLED;
	
	private double playSpeed = 1.0;
	
	/**
	 * All sonars use the same settings
	 */
	private boolean allTheSame = true;
	
	private String offlinetimeZoneId;
	
	private HashMap<Integer, SonarDaqParams> sonarSpecificParams;
	
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

//	/**
//	 * @return the range
//	 */
//	public int getRange() {
//		if (range == 0) {
//			range = 30;
//		}
//		return range;
//	}
//
//	/**
//	 * @param range the range to set
//	 */
//	public void setRange(int range) {
//		this.range = range;
//	}
//
//	/**
//	 * @return the gain
//	 */
//	public int getGain() {
//		if (gain == 0) {
//			gain = 50;
//		}
//		return gain;
//	}
//
//	/**
//	 * @param gain the gain to set
//	 */
//	public void setGain(int gain) {
//		this.gain = gain;
//	}
//
//	/**
//	 * @return the chirpMode
//	 */
//	public int getChirpMode() {
//		return chirpMode;
//	}
//
//	/**
//	 * @param chirpMode the chirpMode to set
//	 */
//	public void setChirpMode(int chirpMode) {
//		this.chirpMode = chirpMode;
//	}
//
//	/**
//	 * @return the rangeConfig
//	 */
//	public int getRangeConfig() {
//		return rangeConfig;
//	}
//
//	/**
//	 * @param rangeConfig the rangeConfig to set
//	 */
//	public void setRangeConfig(int rangeConfig) {
//		this.rangeConfig = rangeConfig;
//	}
//
//	/**
//	 * @return the rangeRangeThreshold
//	 */
//	public double getRangeRangeThreshold() {
//		return rangeRangeThreshold;
//	}
//
//	/**
//	 * @param rangeRangeThreshold the rangeRangeThreshold to set
//	 */
//	public void setRangeRangeThreshold(double rangeRangeThreshold) {
//		this.rangeRangeThreshold = rangeRangeThreshold;
//	}
	
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

	/**
	 * @return the playSpeed
	 */
	public double getPlaySpeed() {
		return playSpeed;
	}

	/**
	 * @param playSpeed the playSpeed to set
	 */
	public void setPlaySpeed(double playSpeed) {
		this.playSpeed = playSpeed;
	}

	/**
	 * @return the allTheSame
	 */
	public boolean isAllTheSame() {
		return allTheSame;
	}

	/**
	 * @param allTheSame the allTheSame to set
	 */
	public void setAllTheSame(boolean allTheSame) {
		this.allTheSame = allTheSame;
	}
	
	public void setSonarParams(int sonarId, SonarDaqParams sonarDaqParams) {
		if (allTheSame) {
			sonarId = DEFAULT_SONAR_PARAMETERSET;
		}
		if (sonarSpecificParams == null) {
			sonarSpecificParams = new HashMap<>();
		}
		sonarSpecificParams.put(sonarId, sonarDaqParams);
	}
	
	public SonarDaqParams getSonarParams(int sonarId) {
		if (allTheSame) {
			sonarId = DEFAULT_SONAR_PARAMETERSET;
		}
		if (sonarSpecificParams == null) {
			sonarSpecificParams = new HashMap<>();
		}
		SonarDaqParams sonarParams = sonarSpecificParams.get(sonarId);
		if (sonarParams == null) {
			sonarParams = getAnyParams();
			if (sonarParams != null) {
				sonarParams = sonarParams.clone();
				sonarSpecificParams.put(sonarId, sonarParams);
			}
		}
		if (sonarParams == null) {
			sonarParams = new SonarDaqParams();
			sonarSpecificParams.put(sonarId, sonarParams);
		}
		return sonarParams;
	}

	/**
	 * Get any existing params. 
	 * @return any existing params. 
	 */
	private SonarDaqParams getAnyParams() {
		if (sonarSpecificParams == null) {
			return null;
		}
		Collection<SonarDaqParams> available = sonarSpecificParams.values();
		Iterator<SonarDaqParams> iterator = available.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	/**
	 * @return the offlinetimeZoneId
	 */
	public String getOfflinetimeZoneId() {
		if (offlinetimeZoneId == null) {
			offlinetimeZoneId = TimeZone.getDefault().getID();
		}
		return offlinetimeZoneId;
	}
	
	/**
	 * Get the time zone to use for offline files. 
	 * @return time zone to use with offline files. 
	 */
	public TimeZone getOfflineTimeZone() {
		/*
		 * Note that TimeZone is not serializable, so we store the id which is a string and OK
		 */
		TimeZone tz = TimeZone.getTimeZone(getOfflinetimeZoneId());
		if (tz == null) {
			tz = TimeZone.getDefault();
		}
		return tz;
	}

	/**
	 * @param offlinetimeZoneId the offlinetimeZoneId to set
	 */
	public void setOfflinetimeZoneId(String offlinetimeZoneId) {
		this.offlinetimeZoneId = offlinetimeZoneId;
	}

}
