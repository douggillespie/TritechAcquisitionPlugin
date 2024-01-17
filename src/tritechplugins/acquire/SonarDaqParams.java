package tritechplugins.acquire;

import java.io.Serializable;

import geminisdk.structures.ChirpMode;
import geminisdk.structures.RangeFrequencyConfig;

/**
 * Daq params for a single sonar. These can now be 
 * different for each device or 'ganged'. They are held
 * as a hashtable in TritechDaqParams. 
 * @author dg50
 *
 */
public class SonarDaqParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private int range = 60;
	
	private int gain = 50;
	
	private int chirpMode = ChirpMode.CHIRP_DISABLED;
	
	/**
	 * 1200i only
	 */
	private int rangeConfig = RangeFrequencyConfig.FREQUENCY_AUTO;

	/**
	 * 1200i only
	 */
	private double rangeRangeThreshold = 40;
	
	private boolean setOnline = true;
	
	private boolean useFixedSoundSpeed;
	
	private double fixedSoundSpeed = 1500;

	private boolean highResolution = true;

	/**
	 * @return the range
	 */
	public int getRange() {
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

	@Override
	protected SonarDaqParams clone() {
		try {
			return (SonarDaqParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the setOnline
	 */
	public boolean isSetOnline() {
		return setOnline;
	}

	/**
	 * @param setOnline the setOnline to set
	 */
	public void setSetOnline(boolean setOnline) {
		this.setOnline = setOnline;
	}

	/**
	 * @return the useFixedSoundSpeed
	 */
	public boolean isUseFixedSoundSpeed() {
		return useFixedSoundSpeed;
	}

	/**
	 * @param useFixedSoundSpeed the useFixedSoundSpeed to set
	 */
	public void setUseFixedSoundSpeed(boolean useFixedSoundSpeed) {
		this.useFixedSoundSpeed = useFixedSoundSpeed;
	}

	/**
	 * @return the fixedSoundSpeed
	 */
	public double getFixedSoundSpeed() {
		if (fixedSoundSpeed == 0) {
			fixedSoundSpeed = 1500;
		}
		return fixedSoundSpeed;
	}

	/**
	 * @param fixedSoundSpeed the fixedSoundSpeed to set
	 */
	public void setFixedSoundSpeed(double fixedSoundSpeed) {
		this.fixedSoundSpeed = fixedSoundSpeed;
	}

	/**
	 * 
	 * Get value for SVS5 SVS5_CONFIG_HIGH_RESOLUTION command
	 * @param high true for high resolution
	 */
	public void setHighResolution(boolean high) {
		this.highResolution = high;
	}
	/**
	 * Get value for SVS5 SVS5_CONFIG_HIGH_RESOLUTION command
	 * @return true for high resolution
	 */
	public boolean isHighResolution() {
		return highResolution;
	}

}
