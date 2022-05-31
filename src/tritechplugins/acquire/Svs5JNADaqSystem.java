package tritechplugins.acquire;

import java.util.Collection;
import java.util.HashMap;

import geminisdk.GenesisSerialiser;
import geminisdk.LoggerStatusInfo;
import geminisdk.OutputFileInfo;
import geminisdk.Svs5Commands;
import geminisdk.Svs5ErrorType;
import geminisdk.Svs5Exception;
import geminisdk.Svs5StandardCallback;
import geminisdk.GenesisSerialiser.GlfLib;
import geminisdk.structures.GemRecord;
import geminisdk.structures.GeminiGain;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.GeminiStructure;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;

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
	
	@Override
	protected void uninitialise() {
//		svs5Commands.
	}
	
	public class GeminiCallback extends Svs5StandardCallback {

		public GeminiCallback() {
			super();
			setVerbose(false);
		}

		int frameCalls = 0;
		
		@Override
		public void setFrameRate(int framesPerSecond) {
			tritechProcess.updateFrameRate(framesPerSecond);
		}

		int nImages = 0;
		@Override
		public void newGLFLiveImage(GLFImageRecord glfImage) {
			SonarStatusData sonarData = findSonarStatusData(glfImage.genericHeader.tm_deviceId);
			if (sonarData != null) {
				sonarData.totalImages++;
			}
			else {
//				System.out.printf("Unable to find sonar data for id %d\n", glfImage.genericHeader.tm_deviceId);
			}
			int chan = glfImage.getSonarIndex();
			long timeMS = glfImage.getRecordTime();
			if (glfImage instanceof GeminiImageRecordI) {
				totalFrames++;
				ImageDataUnit imageDataUnit = new ImageDataUnit(timeMS, 1<<(chan-1), (GeminiImageRecordI) glfImage);
				tritechProcess.getImageDataBlock().addPamData(imageDataUnit);
			}
//			if (nImages++ % 20 == 0) {
//				summariseAllSonarData();
//				//			guiControl.getTritechDisplayPanel().showLiveImage(glfImage);
//			}
		}

		@Override
		public void newStatusPacket(GLFStatusData statusData) {
			// m_sonarId and m_deviceId are the same thing. 
			//			System.out.printf("Sonar id %d device id = %d\n", statusPacket.m_sonarId, statusPacket.m_deviceID);
			SonarStatusData sonarStatusData = checkDeviceInfo(statusData);
			if (sonarStatusData != null) {
				tritechProcess.updateStatusData(sonarStatusData);
			}

		}

		@Override
		public void recUpdateMessage(OutputFileInfo outputFileInfo) {
//			System.out.printf("Record update message \"%s\" nREc %d, percentDisk %3.1f\n", outputFileInfo.getM_strFileName(),
//					outputFileInfo.getM_uiNumberOfRecords(), outputFileInfo.getM_percentDiskSpaceFree());
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
		String sv5Inf = gSerialiser.svs5GetLibraryVersionInfo();
		return sv5Inf;
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

	public void rebootSonars() throws Svs5Exception {
		long ans  = svs5Commands.setConfiguration(GeminiStructure.SVS5_CONFIG_REBOOT_SONAR, null, 0);
		System.out.println("Reboot returned : " + ans);
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
	
	public int setGain(int gain, int deviceId) throws Svs5Exception {
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
