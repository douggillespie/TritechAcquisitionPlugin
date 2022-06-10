package tritechplugins.acquire.offline;

public class OfflineCatalogProgress {

	public int state;
	public int nFiles;
	public String lastFile;
	public OfflineCatalogProgress(int state, int nFiles, String lastFile) {
		super();
		this.state = state;
		this.nFiles = nFiles;
		this.lastFile = lastFile;
	}
	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}
	/**
	 * @return the nFiles
	 */
	public int getnFiles() {
		return nFiles;
	}
	/**
	 * @return the lastFile
	 */
	public String getLastFile() {
		return lastFile;
	}
	

}
