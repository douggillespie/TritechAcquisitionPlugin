package tritechplugins.acquire;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import fileOfflineData.OfflineFileList;

import java.util.Set;

import geminisdk.GenesisSerialiser;
import geminisdk.GenesisSerialiser.GlfLib;
import geminisdk.LoggerStatusInfo;
import geminisdk.OutputFileInfo;
import geminisdk.Svs5Commands;
import geminisdk.Svs5ErrorType;
import geminisdk.Svs5Exception;
import geminisdk.Svs5StandardCallback;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.ConfigOnline;
import geminisdk.structures.GLFLogger;
import geminisdk.structures.GemFileLocation;
import geminisdk.structures.GemRecord;
import geminisdk.structures.GemStatusPacket;
import geminisdk.structures.GeminiGain;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.GeminiStructure;
import geminisdk.structures.PingMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.offline.TritechFileFilter;

public class TritechJNADaq {

	private TritechAcquisition tritechAcquisition;
	private TritechDaqProcess tritechProcess;
	private GlfLib gSerialiser;
	private Svs5Commands svs5Commands;

	private HashMap<Integer, SonarStatusData> deviceInfo = new HashMap<>();
	private int cuurrentRunMode;

	public TritechJNADaq(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechProcess = tritechProcess;

	}

	public boolean initialise() {

		gSerialiser = GenesisSerialiser.getLibrary();
		if (gSerialiser == null) {
			return false;
		}
		svs5Commands = new Svs5Commands();
		long ans1 = gSerialiser.svs5StartSvs5(new GeminiCallback());
//
//		try {
//
//			setOnline(false, 0);
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

	public String getLibVersion() {
		String sv5Inf = gSerialiser.svs5GetLibraryVersionInfo();
		return sv5Inf;
	}

	public SonarStatusData findSonarStatusData(int sonarId) {
		synchronized (deviceInfo) {
			return deviceInfo.get(sonarId); 
		}
	}

	public boolean prepareProcess() {

		if (gSerialiser == null) {
			return false;
		}
		
		unprepareProcess();

		TritechDaqParams params = tritechAcquisition.getDaqParams();
		cuurrentRunMode = params.getRunMode();
		
		if (cuurrentRunMode == TritechDaqParams.RUN_ACQUIRE) {
			return prepareAcquisition();
		}
		else if (cuurrentRunMode == TritechDaqParams.RUN_REPROCESS) {
			return prepareFilePlayback();
		}
		else {
			return false;
		}
	}
	
	private boolean prepareFilePlayback() {
		if (gSerialiser == null) {
			return false;
		}
		// make a list of files from the set folder, and pass to svs4
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		OfflineFileList fileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		String[] allFiles = fileList.asStringList();
		int ans = gSerialiser.setInputFileList(allFiles, allFiles.length);
		System.out.println("Set list of files returned " + ans);
		return ans == 0;
//		return false;
	}

	public boolean prepareAcquisition() {
		
		int waitCount = 0;
		while (deviceInfo.size() < 1) {
			System.out.println("Waiting for devices ...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (++waitCount > 6) {
				System.out.println("No sonars found");
				return false;
			}
		}
		int err = 0;
		try {
			if (tritechAcquisition.getDaqParams().getOfflineFileFolder() != null) {
				setFileLocation(tritechAcquisition.getDaqParams().getOfflineFileFolder());
			}
			
			GeminiRange range = new GeminiRange(tritechAcquisition.getDaqParams().getRange());
			err = svs5Commands.setConfiguration(range, 0);
			//		err += svs5Commands.setConfiguration(range, 1);
			System.out.println("setRange returned " + err);
			err = setRange(tritechAcquisition.getDaqParams().getRange(), 0);

			ChirpMode chirpMode = new ChirpMode(ChirpMode.CHIRP_AUTO);
			err = svs5Commands.setConfiguration(chirpMode, 0);
			System.out.println("setConfiguration chirpMode returned " + err);


			RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
			err = svs5Commands.setConfiguration(rfConfig);
			System.out.println("setConfiguration returned " + err);
			//		
			//	
			////		SimulateADC simADC = new SimulateADC(true);
			////		err = svs5Commands.setConfiguration(simADC);
			////		System.out.println("Simulate returned " + err);
			//
			//		PingMode pingMode = new PingMode();
			//		pingMode.m_bFreeRun = false;
			//		pingMode.m_msInterval = 250;
			//		err += svs5Commands.setConfiguration(pingMode, 0);
			//		err = svs5Commands.setConfiguration(pingMode, 1);
			//		System.out.println("setConfiguration pingMode returned " + err);


			//		err = setFileLocation("C:\\GeminiData");
			//		String fileLoc = getFileLocation();
			//		System.out.printf("Gemini file location is \"%s\"\n", fileLoc);

			ConfigOnline cOnline = new ConfigOnline(true);
			err = svs5Commands.setConfiguration(cOnline, 0);
			//		cOnline.value = false;
			//		err += svs5Commands.setConfiguration(cOnline, 0);
			System.out.println("setOnline returned " + err);



		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void unprepareProcess() {
		// TODO Auto-generated method stub
		
	}

	public boolean start() {
		long err;
		try {
			err = setRecord(true);
		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		//		System.out.println("Set record returned " + err);
		//
		return true;

	}

	public boolean stop() {

		if (gSerialiser == null) {
			return true;
		}


		ConfigOnline cOnline = new ConfigOnline(true);
		long err;
		try {
			err = svs5Commands.setConfiguration(cOnline);
			System.out.println("setOnline off returned " + err);
		} catch (Svs5Exception e) {
			System.err.println("Tritech stop: " + e.getMessage());
		}

		return true;
	}


	public void pamClose() {
		// only want to run this when really cleaning up the process. 
		long ans2 = gSerialiser.svs5StopSvs5();
		System.out.printf("SvS5 stopped with code %d\n", ans2);
	}

	public SonarStatusData checkDeviceInfo(GLFStatusData statusPacket) {
		int n = 0;
		SonarStatusData sonarData = null;
		synchronized (deviceInfo) {
			n = deviceInfo.size();
			sonarData = deviceInfo.get((int) statusPacket.m_deviceID);
			if (sonarData == null) {
				sonarData = new SonarStatusData(statusPacket);
				deviceInfo.put((int) statusPacket.m_deviceID, sonarData);
			}
			else {
				sonarData.setStatusPacket(statusPacket);
			}
			//			sonarData.lastStatusPacket = statusPacket;
		}
		int nNow = deviceInfo.size();
		if (nNow > n) {
			saySonarSummary(sonarData);
		}
		return sonarData;
	}

	public void summariseAllSonarData() {
		Collection<SonarStatusData> devDatas  = null;
		synchronized (deviceInfo) {
			devDatas = deviceInfo.values();
		}
		if (devDatas == null) {
			return;
		}
		for (SonarStatusData sd : devDatas) {
			saySonarSummary(sd);
		}
	}

	public void saySonarSummary(SonarStatusData sonarData) {
		String ip = "?";
		GLFStatusData statusPacket = sonarData.getStatusPacket();
		try {
			InetAddress iNA = InetAddress.getByName(String.valueOf(Integer.toUnsignedLong(statusPacket.m_sonarAltIp)));
			ip = iNA.getHostAddress();
		} catch (UnknownHostException e) {
			ip = String.format("Unknown 0X%X", Integer.toUnsignedLong(statusPacket.m_sonarAltIp));
		}
		//		System.out.printf("Device id %d at ip address %s total images %d\n", statusPacket.m_sonarId, ip, sonarData.totalImages);
	}

	public class GeminiCallback extends Svs5StandardCallback {

		public GeminiCallback() {
			super();
			setVerbose(false);
		}

		int frameCalls = 0;
		@Override
		public void setFrameRate(int framesPerSecond) {
			//			GeminiRange range = new GeminiRange(0);
			//			svs5Commands.getConfiguration(range.defaultCommand(), range, 853);
			//			if (frameCalls++ % 10 == 0) { 
			//				System.out.println("Frame rate is " + framesPerSecond);
			////				summariseAllSonarData();
			//			}
			//			currentFrameRate = framesPerSecond;
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
				System.out.printf("Unable to find sonar data for id %d\n", glfImage.genericHeader.tm_deviceId);
			}
			int chan = glfImage.getSonarIndex();
			long timeMS = glfImage.recordTimeMillis;
			if (glfImage instanceof GeminiImageRecordI) {
				ImageDataUnit imageDataUnit = new ImageDataUnit(timeMS, 1<<(chan-1), (GeminiImageRecordI) glfImage);
				tritechProcess.getImageDataBlock().addPamData(imageDataUnit);
			}
			if (nImages++ % 20 == 0) {
				summariseAllSonarData();
				//			guiControl.getTritechDisplayPanel().showLiveImage(glfImage);
			}
			//			System.out.printf("Image sonar %d with %d beams\n", glfImage.getDeviceId(), glfImage.bearingTable.length);
		}

		@Override
		public void newStatusPacket(GLFStatusData statusData) {
			// m_sonarId and m_deviceId are the same thing. 
			//			System.out.printf("Sonar id %d device id = %d\n", statusPacket.m_sonarId, statusPacket.m_deviceID);
			SonarStatusData sonarStatusData = checkDeviceInfo(statusData);
			if (sonarStatusData != null) {
				tritechProcess.updateStatusData(sonarStatusData);
			}


			//			GeminiRange range;

			//			range = new GeminiRange(1.5);
			//			long err = svs5Commands.setConfiguration(range,0);
			//			err += svs5Commands.setConfiguration(range,1);
			//			System.out.printf("setRange returned %d; ", err);
			//			
			////			range = new GeminiRange(2);
			//			long rErr = svs5Commands.getConfiguration(range.defaultCommand(), range, 0);
			//			double r1 = range.range;
			//			rErr += svs5Commands.getConfiguration(range.defaultCommand(), range, 1);
			//			double r2 = range.range;
			//				System.out.printf("Range err %d, value %3.1f and %3.1f\n", rErr, r1,r2);

		}

		@Override
		public void recUpdateMessage(OutputFileInfo outputFileInfo) {
			System.out.printf("Record update message \"%s\" nREc %d, percentDisk %3.1f\n", outputFileInfo.getM_strFileName(),
					outputFileInfo.getM_uiNumberOfRecords(), outputFileInfo.getM_percentDiskSpaceFree());
			tritechProcess.updateFileName(outputFileInfo);
		}

		@Override
		public void loggerStatusInfo(LoggerStatusInfo loggerStatusInfo) {
			System.out.printf("Logger status information code %d message %s", loggerStatusInfo.getErrorType(), loggerStatusInfo.toString());
		}

	}



	public int getNumSonars() {
		return deviceInfo.size();
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
//		return svs5Commands.sendStringCommand(GeminiStructure.SVS5_CONFIG_FILE_LOCATION, filePath, 0);
		return 0;
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
//		GemRecord gemRecord = new GemRecord(record);
//		long err = svs5Commands.setConfiguration(gemRecord, 0);
//		return err;
		return 0;
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
}
