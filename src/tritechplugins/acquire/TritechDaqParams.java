package tritechplugins.acquire;

import java.io.Serializable;

public class TritechDaqParams implements Serializable, Cloneable{


	public static final long serialVersionUID = 1L;
	
	private String offlineFileFolder;
	
	private boolean offlineSubFolders;
	
	private int range = 60;
	
	private int gain = 50;

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
	protected TritechDaqParams clone() {
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
	

}
