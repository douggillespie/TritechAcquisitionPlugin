package tritechplugins.acquire;

/**
 * Callback data for JavaFileAcquisition
 * @author dg50
 *
 */
public class JavaFileStatus {
	private int nFiles;
	private int currentFile;
	private String fileName;
	
	public JavaFileStatus(int nFiles, int currentFile, String fileName) {
		this.nFiles = nFiles;
		this.currentFile = currentFile;
		this.fileName = fileName;
	}

	/**
	 * @return the nFiles
	 */
	public int getnFiles() {
		return nFiles;
	}

	/**
	 * @return the currentFile
	 */
	public int getCurrentFile() {
		return currentFile;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

}
