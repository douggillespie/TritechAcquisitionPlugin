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
	private double processingRate;
	private long remainingTime;
	
	public JavaFileStatus(int nFiles, int currentFile, String fileName, double processingRate, long remTime) {
		this.nFiles = nFiles;
		this.currentFile = currentFile;
		this.fileName = fileName;
		this.processingRate = processingRate;
		this.remainingTime = remTime;
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

	/**
	 * Get the processing rate (ratio or data rate to real time)
	 * @return
	 */
	public double getProcessingRate() {
		return processingRate;
	}

	public long getRemainingTime() {
		return remainingTime;
	}

}
