package tritechplugins.acquire;

import java.io.Serializable;

public class TritechDaqParams implements Serializable, Cloneable{


	public static final long serialVersionUID = 1L;
	
	private String offlineFileFolder;
	
	private boolean offlineSubFolders;

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
	

}
