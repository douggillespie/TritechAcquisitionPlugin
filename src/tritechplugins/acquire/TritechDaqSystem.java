package tritechplugins.acquire;

import java.util.Collection;
import java.util.HashMap;

import PamUtils.PamCalendar;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.display.swing.SonarDisplayDecorations;
import warnings.PamWarning;
import warnings.WarningSystem;

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

	protected int[] recordIndexes = new int[4];
	
	private PamWarning oowWarning = new PamWarning("Gemini"	, "", 2);

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
	
	public void newGLFLiveImage(GLFImageRecord glfImage) {
		SonarStatusData sonarData = findSonarStatusData(glfImage.genericHeader.tm_deviceId);
		if (sonarData != null) {
			sonarData.totalImages++;
			sonarData.lastImageTime = glfImage.getRecordTime();
			sonarData.interStatusImages++;
		}
		else {
//			System.out.printf("Unable to find sonar data for id %d\n", glfImage.genericHeader.tm_deviceId);
		}
		int chan = glfImage.getSonarIndex();
		glfImage.recordIndex = recordIndexes[chan]++;;
		long timeMS = glfImage.getRecordTime();
		if (glfImage instanceof GeminiImageRecordI) {
			totalFrames++;
			ImageDataUnit imageDataUnit = new ImageDataUnit(timeMS, 1<<(chan-1), (GeminiImageRecordI) glfImage);
			tritechProcess.getImageDataBlock().addPamData(imageDataUnit);
		}
	}

	public void newStatusPacket(GLFStatusData statusData) {
		// m_sonarId and m_deviceId are the same thing. 
		//			System.out.printf("Sonar id %d device id = %d\n", statusPacket.m_sonarId, statusPacket.m_deviceID);
		SonarStatusData sonarStatusData = checkDeviceInfo(statusData);
		
		checkOutOfWater(statusData);
		
		if (sonarStatusData != null) {
			tritechProcess.updateStatusData(sonarStatusData);
//			checkWatchdog(sonarStatusData);
			sonarStatusData.interStatusImages = 0;
			ImageDataUnit imageDataUnit = new ImageDataUnit(PamCalendar.getTimeInMillis(), 0, sonarStatusData);
			tritechProcess.getImageDataBlock().addPamData(imageDataUnit);
		}
	}
	public void checkOutOfWater(GLFStatusData statusData) {
		if (statusData == null) {
			return;
		}
		OpsSonarStatusData opsData = getOpsSonarStatusData(statusData.m_deviceID);
		if (statusData.isOutOfWater() != opsData.outOfWater) {
			opsData.outOfWater = statusData.isOutOfWater();
//			System.out.println("OOW is " + opsData.outOfWater);
			sayOOWWarning();
		}
	}


	private void sayOOWWarning() {
		int nOOW = 0;
		int[] sonars = getSonarIDs();
		String warning = "";
		for (int i = 0; i < sonars.length; i++) {
			OpsSonarStatusData opsData = getOpsSonarStatusData(sonars[i]);
			if (opsData.outOfWater) {
				if (nOOW == 0) {
					warning = String.format("Sonar %d", sonars[i]);
				}
				else {
					warning += String.format(" and sonar %d", sonars[i]);
				}
				nOOW++;
			}
		}
		if (nOOW == 1) {
			warning += " is out of water";
		}
		else if (nOOW > 1) {
			warning += " are out of water";
		}
		if (nOOW == 0) {
			WarningSystem.getWarningSystem().removeWarning(oowWarning);
		}
		else {
			oowWarning.setWarningMessage(warning);
			oowWarning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(oowWarning);
		}
	}
	public int[] getSonarIDs() {
		// could probably actually use the keys since the sonar id's are the keys in the hash table.
		Collection<SonarStatusData> devs = deviceInfo.values();
		int n = devs.size();
		int[] ids = new int[n];
		int i = 0;
		for (SonarStatusData sd : devs) {
			ids[i++] = sd.getStatusPacket().m_deviceID;
		}
		return ids;
	}

}
