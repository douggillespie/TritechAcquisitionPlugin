package tritechplugins.acquire;

import java.util.Collection;
import java.util.HashMap;

import com.sun.jna.Pointer;

import PamUtils.PamCalendar;
import geminisdk.GenesisSerialiser;
import geminisdk.LoggerStatusInfo;
import geminisdk.OutputFileInfo;
import geminisdk.Svs5Commands;
import geminisdk.Svs5ErrorType;
import geminisdk.Svs5Exception;
import geminisdk.Svs5StandardCallback;
import geminisdk.GenesisSerialiser.GlfLib;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.GemRecord;
import geminisdk.structures.GeminiGain;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.GeminiStructure;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Daq systems based around the JNA interface to the Svs5 library. There
 * will be two subclasses of this one for real time and one for offline files. 
 * @author dg50
 *
 */
abstract public class Svs5JNADaqSystem extends TritechDaqSystem {

	protected GeminiCallback geminiCallback;

	protected GlfLib gSerialiser;
	
	protected Svs5Commands svs5Commands;

	private String lastFileName = "";
	
	private int[] recordIndexes = new int[4];
	
	private PamWarning oowWarning = new PamWarning("Gemini"	, "", 2);

	public Svs5JNADaqSystem(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		geminiCallback = new GeminiCallback();
		boolean isInit = initialise();
		String version = getLibVersion();
		System.out.printf("JNA Daq initialised %s version %s\n", new Boolean(isInit).toString(), version);
	}

	public boolean initialise() {

		gSerialiser = GenesisSerialiser.getLibrary();
		if (gSerialiser == null) {
			return false;
		}
		svs5Commands = new Svs5Commands();
		long ans1 = gSerialiser.svs5StartSvs5(geminiCallback = new GeminiCallback());

//		try {

			setOnline(false, 0);
//			Boolean isOn1 = getOnline(0);
//			setOnline(true, 0);
////			Boolean isOn2 = getOnline(0);
////			System.out.printf("Online status are %s and %s\n", isOn1, isOn2);
//
//			svs5Commands.setConfiguration(new GLFLogger(true));
//
//
//
//			long err=0;//
//			err = setFileLocation("C:\\GeminiData2");
////			String fileLoc = getFileLocation();
////			System.out.printf("Gemini file location is %d \"%s\"\n", err,  fileLoc);
//
//		} catch (Svs5Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return svs5Commands != null;

	}
	
	/**
	 * 
	 * @return The SVS5 callback queue size.
	 */
	public int getSvs5QueueSize() {
		return geminiCallback.getSvs5QueueSize();
	}
	
	@Override
	protected void uninitialise() {
		if (gSerialiser != null) {
			gSerialiser.svs5StopSvs5();
		}
	}
	
	public void preProcessSv5Callback(int msgType, long size, Pointer pointer) {
		
	}
	public void postProcessSv5Callback(int msgType, long size, Pointer pointer) {
		
	}
	
	public class GeminiCallback extends Svs5StandardCallback {

		public GeminiCallback() {
			super();
			setVerbose(false);
		}

		int frameCalls = 0;
		
		
		
		@Override
		public void setFrameRate(int framesPerSecond, double trueFPS) {
			tritechProcess.updateFrameRate(framesPerSecond, trueFPS);
			tritechProcess.updateQueueSize(getSvs5QueueSize());
		}

		int nImages = 0;
		
		@Override
		public void processSv5Callback(int msgType, long size, Pointer pointer) {
			preProcessSv5Callback(msgType, size, pointer);
			super.processSv5Callback(msgType, size, pointer);
			postProcessSv5Callback(msgType, size, pointer);
		}

		@Override
		public void newGLFLiveImage(GLFImageRecord glfImage) {
			SonarStatusData sonarData = findSonarStatusData(glfImage.genericHeader.tm_deviceId);
			if (sonarData != null) {
				sonarData.totalImages++;
				sonarData.lastImageTime = glfImage.getRecordTime();
				sonarData.interStatusImages++;
			}
			else {
//				System.out.printf("Unable to find sonar data for id %d\n", glfImage.genericHeader.tm_deviceId);
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

		@Override
		public void newStatusPacket(GLFStatusData statusData) {
			// m_sonarId and m_deviceId are the same thing. 
			//			System.out.printf("Sonar id %d device id = %d\n", statusPacket.m_sonarId, statusPacket.m_deviceID);
			SonarStatusData sonarStatusData = checkDeviceInfo(statusData);
			
			checkOutOfWater(statusData);
			
			if (sonarStatusData != null) {
				tritechProcess.updateStatusData(sonarStatusData);
				checkWatchdog(sonarStatusData);
				sonarStatusData.interStatusImages = 0;
			}
		}

		@Override
		public void recUpdateMessage(OutputFileInfo outputFileInfo) {
//			System.out.printf("Record update message \"%s\" nREc %d, percentDisk %3.1f\n", outputFileInfo.getM_strFileName(),
//					outputFileInfo.getM_uiNumberOfRecords(), outputFileInfo.getM_percentDiskSpaceFree());
			if (outputFileInfo.getM_strFileName() != null) {
				if (outputFileInfo.getM_strFileName().equals(lastFileName) == false) {
					lastFileName = outputFileInfo.getM_strFileName();
					for (int i = 0; i < recordIndexes.length; i++) {
						recordIndexes[i] = 0;
					}
				}
			}
			
			tritechProcess.updateFileName(outputFileInfo);
		}

		@Override
		public void loggerStatusInfo(LoggerStatusInfo loggerStatusInfo) {
			System.out.printf("Logger status information code %d message %s", loggerStatusInfo.getErrorType(), loggerStatusInfo.toString());
		}

		@Override
		public void newLoggerPlaybackUpdate(LoggerPlaybackUpdate loggerPlaybackUpdate) {
			tritechProcess.updateLoggerPlayback(loggerPlaybackUpdate);
		}

		@Override
		public void loggerFileIndex(int fileIndex) {
//			System.out.println("File index: " + fileIndex);
			tritechProcess.updateFileIndex(fileIndex);
		}

	}

	public String getLibVersion() {
		if (gSerialiser == null) {
			return "Not installed";
		}
		String sv5Inf = gSerialiser.svs5GetLibraryVersionInfo();
		return sv5Inf;
	}


	/**
	 * Check on each sonar whether packets have been received or not 
	 * since the last sonar status data. If we get more than a 
	 * few packets without any data, then take action !
	 * @param sonarStatusData
	 */
	public void checkWatchdog(SonarStatusData sonarStatusData) {
		if (sonarStatusData.interStatusImages > 0) {
			sonarStatusData.zeroPacketWarnings = 0;
			return;
		}
		/*
		 *  check that this sonar is actually enabled.
		 *  If a sonar is not online, it still sends status messages
		 *  though no data.  
		 */
		SonarDaqParams sonarParams = tritechAcquisition.getDaqParams().getSonarParams(sonarStatusData.getDeviceId());
		if (sonarParams.isSetOnline() == false) {
//			System.out.println("No need to reboot sonar " + sonarStatusData.getDeviceId());
			return;
		}
		long gapTime = System.currentTimeMillis() - sonarStatusData.lastImageTime;
		if (gapTime > 5000 && System.currentTimeMillis() - sonarStatusData.lastReboot > 60000) {
			// we've gone five seconds without receiving any data, so reboot and haven't rebooted for > 1 minute
			System.out.printf("%s: No data received from sonar %d in %3.1f seconds. Reboot it\n", 
					PamCalendar.formatDBDateTime(System.currentTimeMillis()),
					sonarStatusData.getDeviceId(), (double) gapTime/1000.);
			sonarStatusData.lastReboot = System.currentTimeMillis();
			rebootSonar(sonarStatusData.getDeviceId());
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

	public void rebootSonar(int sonarId) {
		try {
			long ans  = svs5Commands.setConfiguration(GeminiStructure.SVS5_CONFIG_REBOOT_SONAR, null, sonarId);
//			System.out.println("Reboot returned : " + ans);
		}
		catch ( Svs5Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the sonar(s) online status
	 * @param online
	 * @param deviceId
	 * @return svs5 error
	 */
	public int setOnline(boolean online, int deviceId) {
		try {
			return svs5Commands.setBoolCommand(GeminiStructure.SVS5_CONFIG_ONLINE, online, deviceId);
		}
		catch (Svs5Exception e) {
			System.out.println("SetOnline error " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Set the sonar(s) online status
	 * @param online
	 * @param deviceId
	 * @return svs5 error
	 */
	public boolean getOnline(int deviceId) {
		try {
			return svs5Commands.getBoolCommand(GeminiStructure.SVS5_CONFIG_ONLINE, deviceId);
		}
		catch (Svs5Exception e) {
			System.out.println("SetOnline error " + e.getMessage());
			return false;
		}
	}


	public int setRange(int range, int deviceId) throws Svs5Exception {
		GeminiRange rangeObj = new GeminiRange(range);
		int err = 0;
		if (svs5Commands == null) {
			return 0;
		}
		err = svs5Commands.setConfiguration(rangeObj, deviceId);
		if (err != Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
			throw new Svs5Exception(err);
		}
		return err;
	}
	
	public double getRange(int deviceId) throws Svs5Exception {
		GeminiRange rangeObj = new GeminiRange(deviceId);
		int err = 0;
		err = svs5Commands.setConfiguration(rangeObj, 0);
		if (err != Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
			throw new Svs5Exception(err);
		}
		return rangeObj.range;
	}
	
	public int setChirpMode(int chipMode, int deviceId) throws Svs5Exception {

		if (svs5Commands == null) {
			return 0;
		}
		ChirpMode chirpMode = new ChirpMode(ChirpMode.CHIRP_AUTO);
		int err = svs5Commands.setConfiguration(chirpMode, deviceId);
		if (err != Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
			throw new Svs5Exception(err);
		}
		return err;
	}
	
	public int setGain(int gain, int deviceId) throws Svs5Exception {
		if (svs5Commands == null) {
			return 0;
		}
		GeminiGain gainObj = new GeminiGain(gain);
		int err = svs5Commands.setConfiguration(gainObj, deviceId);	
		if (err != Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
			throw new Svs5Exception(err);
		}	
		return err;
	}
	
	public int getGain(int deviceId) throws Svs5Exception {
		GeminiGain gainObj = new GeminiGain(deviceId);
		int err = svs5Commands.setConfiguration(gainObj, 0);	
		if (err != Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
			throw new Svs5Exception(err);
		}	
		return gainObj.gain;
	}

	/**
	 * Set the output file location. Probably sensible to check the folder
	 * exists before calling this. 
	 * @param filePath file path
	 * @return error code. 
	 * @throws Svs5Exception 
	 */
	public long setFileLocation(String filePath) throws Svs5Exception {
		//		GemFileLocation gemLoc = new GemFileLocation(filePath);
		//		long err = svs5Commands.setConfiguration(gemLoc, 0);
		//		return err;
		//		return 0;
		if (svs5Commands == null) {
			return 0;
		}
		return svs5Commands.sendStringCommand(GeminiStructure.SVS5_CONFIG_FILE_LOCATION, filePath, 0);
//		return 0;
	}

	/** **** Do not call this function since it sets the output path to null and stuff everything ****<p>
	 * Get the output file path. 
	 * @return file path or null if it can't be read. 
	 * @throws Svs5Exception 
	 */
	public String getFileLocation() throws Svs5Exception { 
		//		GemFileLocation gemLoc = new GemFileLocation(new byte[64]);
		//		long err = svs5Commands.getConfiguration(gemLoc.defaultCommand(), gemLoc, 0);
		//		if (err == Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
		//			return gemLoc.getFilePath();
		//		}
		//		return null;
//		return svs5Commands.getStringCommand(GeminiStructure.SVS5_CONFIG_FILE_LOCATION, 128, 0);
		return null;
	}

	/**
	 * Set recording on or off
	 * @param record record status. 
	 * @return 0 if no error issuing command. 
	 * @throws Svs5Exception 
	 */
	long setRecord(boolean record) throws Svs5Exception {
		GemRecord gemRecord = new GemRecord(record);
		long err = svs5Commands.setConfiguration(gemRecord, 0);
		return err;
//		return 0;
	}

	/**
	 * Get if the system is currently recording. 
	 * @return record state
	 * @throws Svs5Exception 
	 */
	boolean getRecord() throws Svs5Exception {
//		GemRecord gemRecord = new GemRecord(false);
//		long err = svs5Commands.getConfiguration(gemRecord.defaultCommand(), gemRecord, 0);
//		if (err == Svs5ErrorType.SVS5_SEQUENCER_STATUS_OK) {
//			return gemRecord.isRecord();
//		}
		return false;
	}

	public void setPlaybackSpeed(double speed) {
		try {
			svs5Commands.setDoubleCommand(GeminiStructure.SVS5_CONFIG_PLAY_SPEED, speed, 0);
		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
