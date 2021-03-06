package tritechplugins.acquire;

import java.util.HashMap;

import tritechgemini.imagedata.GLFStatusData;
import tritechplugins.display.swing.SonarDisplayDecorations;

/**
 * There are multiple ways of getting data into this module
 * <br>1) Real time acquisition via the Tritech Svs5 C interface
 * <br>2) using the svs5 interface to read Tritech files
 * <br>3) using my own pure Java glf file reader to chomp through files.
 * <br>Realistically it's going to be easiest to have an abstract class that 
 * has appropriate functions to manage all three possibilities.  
 * @author dg50
 *
 */
public abstract class TritechDaqSystem {
	
	protected TritechAcquisition tritechAcquisition;
	protected TritechDaqProcess tritechProcess;
	
	protected ImageDataBlock outputData;
	
	protected HashMap<Integer, SonarStatusData> deviceInfo = new HashMap<>();
	
	protected int totalFrames;

	public TritechDaqSystem(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechProcess = tritechProcess;
		outputData = tritechProcess.getImageDataBlock();
	}

	public int getNumSonars() {
		return deviceInfo.size();
	}
	
	/**
	 * Called when this system is selected and before processing starts
	 * @return
	 */
	public abstract boolean prepareProcess();
	
	/**
	 * Start processing
	 * @return
	 */
	public abstract boolean start();
	
	/**
	 * Stop processing. 
	 * @return
	 */
	public abstract boolean stop();


	/**
	 * Called when PAMGuard is closing or when this sytem is no deselected
	 */
	protected abstract void uninitialise();

	/**
	 * Is processing real time, or playing back from file. 
	 * @return true if realtime
	 */
	public abstract boolean isRealTime();
	
	/**
	 * Called when processing has ended OR when this system is deselected
	 * @return OK
	 */
	public abstract void unprepareProcess();

	/**
	 * HashMap of device info. When working offline we need to ensure that live
	 * sonars don't get added to this list and also probably clear it when 
	 * starting a file analysis.  
	 * @param statusPacket
	 * @return
	 */
	public SonarStatusData checkDeviceInfo(GLFStatusData statusPacket) {
		int n = 0;
		SonarStatusData sonarData = null;
		synchronized (deviceInfo) {
			n = deviceInfo.size();
			sonarData = findSonarStatusData(statusPacket.m_deviceID);
//			sonarData = deviceInfo.get((int) statusPacket.m_deviceID);
//			if (sonarData == null) {
//				sonarData = new SonarStatusData(statusPacket);
//				deviceInfo.put((int) statusPacket.m_deviceID, sonarData);
//			}
//			else {
				sonarData.setStatusPacket(statusPacket);
//			}
			//			sonarData.lastStatusPacket = statusPacket;
		}
//		int nNow = deviceInfo.size();
//		if (nNow > n) {
//			saySonarSummary(sonarData);
//		}
		return sonarData;
	}
	
	public SonarStatusData findSonarStatusData(int sonarId) {
		synchronized (deviceInfo) {
			SonarStatusData statusData = deviceInfo.get(sonarId);
			if (statusData == null) {
				statusData = new SonarStatusData(null);
				deviceInfo.put(sonarId, statusData);
			}
			return statusData; 
		}
	}
	
	/**
	 * Get controls and info strips to add to the corners of the sonars display panel. 
	 * @return controls for the corners.
	 */
	public SonarDisplayDecorations getSwingDecorations() {
		return null;
	}
}
