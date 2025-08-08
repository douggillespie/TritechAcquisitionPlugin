package tritechplugins.acquire;

import java.util.Collection;
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
	
	protected HashMap<Integer, OpsSonarStatusData> opsStatusData = new HashMap();
	
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
	 * See if the device is out of water. Returns false if there is no 
	 * latest status data.  
	 * @param deviceId device id
	 * @return true if last status data packet has OOW set. 
	 */
	public boolean isOOW(int deviceId) {
		SonarStatusData statusData = getSonarStatusData(deviceId);
		return isOOW(statusData);
	}
	
	/**
	 * 
	 * See if the device is out of water. Returns false if there is no 
	 * latest status data.  
	 * @param statusData status data for a device. 
	 * @return true if last status data packet has OOW set. 
	 */
	public boolean isOOW(SonarStatusData statusData) {
		if (statusData == null) {
			return false;
		}
		GLFStatusData status = statusData.getStatusPacket();
		if (status == null) {
			return false;
		}
		if (status.isOutOfWater()) {
			return true;
		}
		return false;
	}
	
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
			sonarData = getSonarStatusData(statusPacket.m_deviceID);
			boolean isNewSonar = sonarData == null;
			
			sonarData = findSonarStatusData(statusPacket.m_deviceID);
			sonarData.setStatusPacket(statusPacket);
			
			if (isNewSonar) {
				newSonar(sonarData);
			}
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
	
	/**
	 * Called when a new sonar sends it's first status packet.
	 * Can be used to set up everything on that sonar. 
	 * @param sonarData
	 */
	protected abstract void newSonar(SonarStatusData sonarData);

	/**
	 * Gets sonar status data. Does NOT create it. 
	 * @param sonarId
	 * @return status data or null.
	 */
	public SonarStatusData getSonarStatusData(int sonarId) {
		synchronized (deviceInfo) {
			SonarStatusData statusData = deviceInfo.get(sonarId);
			return statusData;
		}
	}
	
	public int[] getSonarIds() {
		synchronized (deviceInfo) {
			Collection<SonarStatusData> sonarValues = deviceInfo.values();
			int[] ids = new int[sonarValues.size()];
			int i = 0;
			for (SonarStatusData val : sonarValues) {
				ids[i++] = val.getDeviceId();
			}
			return ids;
		}
	}
	
	/**
	 * finds sonar status data and creates if necessary
	 * @param sonarId
	 * @return
	 */
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
	 * Get an ops sonar data for each sonar. 
	 * @param sonarId
	 * @return
	 */
	public OpsSonarStatusData getOpsSonarStatusData(int sonarId) {
		OpsSonarStatusData opsData = opsStatusData.get(sonarId);
		if (opsData == null) {
			opsData = new OpsSonarStatusData();
			opsStatusData.put(sonarId, opsData);
		}
		return opsData;
	}
	
	/**
	 * Get controls and info strips to add to the corners of the sonars display panel. 
	 * @return controls for the corners.
	 */
	public SonarDisplayDecorations getSwingDecorations() {
		return null;
	}

	/**
	 * Reboot a sonar. 
	 * @param sonarId sonar id or 0 for all sonars on system
	 */
	protected abstract void rebootSonar(int sonarId);
	
	/**
	 * Set recording of GLF files off or on. 
	 * @param record
	 */
	public void setRecording(boolean record) {
	}
}
