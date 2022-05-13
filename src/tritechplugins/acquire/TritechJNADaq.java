package tritechplugins.acquire;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import geminisdk.GenesisSerialiser;
import geminisdk.GenesisSerialiser.GlfLib;
import geminisdk.Svs5Commands;
import geminisdk.Svs5StandardCallback;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.ConfigOnline;
import geminisdk.structures.GemStatusPacket;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.PingMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;

public class TritechJNADaq {

	private TritechAcquisition tritechAcquisition;
	private TritechDaqProcess tritechProcess;
	private GlfLib gSerialiser;
	private Svs5Commands svs5Commands;

	private HashMap<Integer, SonarStatusData> deviceInfo = new HashMap<>();

	public TritechJNADaq(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechProcess = tritechProcess;
		
	}
	
	public boolean initialise() {

		gSerialiser = GenesisSerialiser.getLibrary();
		svs5Commands = new Svs5Commands();
		
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
	
	public boolean start() {
		long ans1 = gSerialiser.svs5StartSvs5(new GeminiCallback());
		long err;
		
		while (deviceInfo.size() < 1) {
			System.out.println("Waiting for devices ...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		GeminiRange range = new GeminiRange(5.);
		err = svs5Commands.setConfiguration(range, 0);
//		err += svs5Commands.setConfiguration(range, 1);
		System.out.println("setRange returned " + err);
		
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
		PingMode pingMode = new PingMode();
		pingMode.m_bFreeRun = false;
		pingMode.m_msInterval = 250;
		err += svs5Commands.setConfiguration(pingMode, 0);
		err = svs5Commands.setConfiguration(pingMode, 1);
		System.out.println("setConfiguration pingMode returned " + err);

		ConfigOnline cOnline = new ConfigOnline(false);
		err = svs5Commands.setConfiguration(cOnline, 0);
//		cOnline.value = false;
//		err += svs5Commands.setConfiguration(cOnline, 0);
		System.out.println("setOnline returned " + err);
//
return true;
		
	}
	
	public boolean stop() {

		ConfigOnline cOnline = new ConfigOnline(true);
		long err = svs5Commands.setConfiguration(cOnline);
		System.out.println("setOnline off returned " + err);

		long ans2 = gSerialiser.svs5StopSvs5();
		System.out.printf("SvS5 stopped with code %d\n", ans2);
		
		return true;
	}
	

	public SonarStatusData checkDeviceInfo(GemStatusPacket statusPacket) {
		int n = 0;
		SonarStatusData sonarData = null;
		synchronized (deviceInfo) {
			n = deviceInfo.size();
			sonarData = deviceInfo.get((int) statusPacket.m_sonarId);
			if (sonarData == null) {
				sonarData = new SonarStatusData(statusPacket.m_sonarId);
				deviceInfo.put((int) statusPacket.m_sonarId, sonarData);
			}
			sonarData.lastStatusPacket = statusPacket;
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
		GemStatusPacket statusPacket = sonarData.lastStatusPacket;
		try {
			InetAddress iNA = InetAddress.getByName(String.valueOf(Integer.toUnsignedLong(statusPacket.m_sonarAltIp)));
			ip = iNA.getHostAddress();
		} catch (UnknownHostException e) {
			ip = String.format("Unknown 0X%X", Integer.toUnsignedLong(statusPacket.m_sonarAltIp));
		}
		System.out.printf("Device id %d at ip address %s total images %d\n", statusPacket.m_sonarId, ip, sonarData.totalImages);
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
			if (frameCalls++ % 10 == 0) { 
				System.out.println("Frame rate is " + framesPerSecond);
//				summariseAllSonarData();
			}
		}

		int nImages = 0;
		@Override
		public void newGLFLiveImage(GLFImageRecord glfImage) {
			SonarStatusData sonarData = findSonarStatusData(glfImage.tm_deviceId);
			if (sonarData != null) {
				sonarData.totalImages++;
			}
			else {
				System.out.printf("Unable to find sonar data for id %d\n", glfImage.tm_deviceId);
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
		public void newStatusPacket(GemStatusPacket statusPacket) {
			// m_sonarId and m_deviceId are the same thing. 
//			System.out.printf("Sonar id %d device id = %d\n", statusPacket.m_sonarId, statusPacket.m_deviceID);
			checkDeviceInfo(statusPacket);

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
		
	}

	public int getNumSonars() {
		return deviceInfo.size();
	}

	public int[] getSonarIDs() {
		 Collection<SonarStatusData> devs = deviceInfo.values();
		int n = devs.size();
		int[] ids = new int[n];
		int i = 0;
		for (SonarStatusData sd : devs) {
			ids[i++] = sd.sonarId;
		}
		return ids;
	}


}
