package tritechplugins.acquire;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import fileOfflineData.OfflineFileList;

import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

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
import geminisdk.structures.LoggerPlaybackUpdate;
import geminisdk.structures.PingMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.offline.TritechFileFilter;
import tritechplugins.acquire.swing.DaqControlPanel;
import tritechplugins.acquire.swing.SonarsStatusPanel;
import tritechplugins.display.swing.SonarDisplayDecoration;
import tritechplugins.display.swing.SonarDisplayDecorations;

public class TritechJNADaq extends Svs5JNADaqSystem {

	private int cuurrentRunMode;
	private JNADecorations swingDecorations;

	public TritechJNADaq(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
	}

	public boolean prepareProcess() {
		
		unprepareProcess();

		return prepareAcquisition();
	}

	private boolean prepareAcquisition() {

		geminiCallback.setPlaybackMode(false);

		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		if (daqParams.getOfflineFileFolder() != null) {
			try {
				setFileLocation(tritechAcquisition.getDaqParams().getOfflineFileFolder());
			} catch (Svs5Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * No longer wait here for sonars. They will initialise when they become available. 
		 */

//		int waitCount = 0;
//		while (deviceInfo.size() < 1) {
//			System.out.println("Waiting for devices ...");
//			try {
//				Thread.sleep(20);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if (++waitCount > 6) {
//				System.out.println("No sonars found");
//				return false;
//			}
//		}

		
		int[] sonars = getSonarIds();
		return prepareAllDevices(sonars);
		
		
		
		
//		return prepareDevice(0);
		
	}
	private boolean prepareAllDevices(int[] sonars) {
		if (sonars == null) {
			return true;
		}
		boolean ok = true;
		for (int i = 0; i < sonars.length; i++) {
			ok |= prepareDevice(sonars[i]);
		}
		return ok;
	}

	private boolean prepareDevice(int deviceId) {
		int err = 0;
//		if (1>0) return false;
		try {
			
			SonarDaqParams sonarParams = tritechAcquisition.getDaqParams().getSonarParams(deviceId);

			GeminiRange range = new GeminiRange(sonarParams.getRange());
			err = svs5Commands.setConfiguration(range, deviceId);
			//		err += svs5Commands.setConfiguration(range, 1);
//			System.out.println("setRange returned " + err);
			err = setRange(sonarParams.getRange(), deviceId);
			
			setGain(sonarParams.getGain(), deviceId);
			
//			PingMode pingMode = new PingMode(true, (short) 0);
			err = svs5Commands.setPingMode(true, (short) 5000);
//			System.out.println("setConfiguration pingMode returned " + err);
			

			ChirpMode chirpMode = new ChirpMode(sonarParams.getChirpMode());
			err = svs5Commands.setConfiguration(chirpMode, deviceId);
//			System.out.println("setConfiguration chirpMode returned " + err);


			RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
			rfConfig.m_frequency = sonarParams.getRangeConfig();
			err = svs5Commands.setConfiguration(rfConfig);
//			System.out.println("setConfiguration returned " + err);
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
			err = svs5Commands.setConfiguration(cOnline, deviceId);
			//		cOnline.value = false;
			//		err += svs5Commands.setConfiguration(cOnline, 0);
//			System.out.println("setOnline returned " + err);



		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Error e) {
			System.out.println("Error calling SvS5 startup functions:" + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void unprepareProcess() {
		stop();
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	public boolean start() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		cuurrentRunMode = params.getRunMode();

		totalFrames = 0;

		return startAcquisition();
	}

	private boolean startAcquisition() {
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

//		System.out.println("Total frames processed from svs5 is " + totalFrames);


		return stopAcquisition();
	}


	private boolean stopAcquisition() {
		/*
		 * Leave it online, just stop recording
		 */
		//		ConfigOnline cOnline = new ConfigOnline(true);
		long err;
		try {
			//			err = svs5Commands.setConfiguration(cOnline);
			err = setRecord(false);
			//			System.out.println("setOnline off returned " + err);
		} catch (Svs5Exception e) {
			System.err.println("Tritech stop: " + e.getMessage());
		}

		return true;
	}


	@Override
	protected void newSonar(SonarStatusData sonarData) {
		/**
		 * Called on first status data for each sonar so can check it's
		 * set up correctly. 
		 */
		prepareDevice(sonarData.getDeviceId());
//		TritechDaqParams params = tritechAcquisition.getDaqParams();
//		try {
//			setRange(params.getRange(), sonarData.getDeviceId());
//			setGain(params.getGain(), sonarData.getDeviceId());
//			setChirpMode(params.getChirpMode(), sonarData.getDeviceId());
//			svs5Commands.setBoolCommand(GeminiStructure.SVS5_CONFIG_HIGH_RESOLUTION, false, sonarData.getDeviceId());
//			RangeFrequencyConfig rfConfig = new RangeFrequencyConfig(RangeFrequencyConfig.FREQUENCY_LOW);
//			int err = svs5Commands.setConfiguration(rfConfig, sonarData.getDeviceId());
//			System.out.printf("Error %d from set rangefrequencyconfig\n", err);
//			
//		} catch (Svs5Exception e) {
//			e.printStackTrace();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
	}

	public void pamClose() {
		// only want to run this when really cleaning up the process. 
		long ans2 = gSerialiser.svs5StopSvs5();
		System.out.printf("SvS5 stopped with code %d\n", ans2);
	}


	/**
	 * Clear all device info data. 
	 */
	public void clearDeviceInfo() {
		synchronized (deviceInfo) {
			deviceInfo.clear();
		}

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
		if (statusPacket == null) {
			return;
		}
		try {
			InetAddress iNA = InetAddress.getByName(String.valueOf(Integer.toUnsignedLong(statusPacket.m_sonarAltIp)));
			ip = iNA.getHostAddress();
		} catch (UnknownHostException e) {
			ip = String.format("Unknown 0X%X", Integer.toUnsignedLong(statusPacket.m_sonarAltIp));
		}
		//		System.out.printf("Device id %d at ip address %s total images %d\n", statusPacket.m_sonarId, ip, sonarData.totalImages);
	}

	@Override
	public SonarDisplayDecorations getSwingDecorations() {
		if (swingDecorations == null) {
			swingDecorations = new JNADecorations();
		}
		return swingDecorations;
	}

	private class JNADecorations extends SonarDisplayDecorations {

		@Override
		public SonarDisplayDecoration getNorthWestInset() {
			return new SonarsStatusPanel(tritechAcquisition, TritechJNADaq.this);
		}

		@Override
		public SonarDisplayDecoration getSouthWestInset() {
			return null;
//			return new DaqControlPanel(tritechAcquisition, TritechJNADaq.this, TritechDaqParams.DEFAULT_SONAR_PARAMETERSET);
		}

		@Override
		public int addMenuItems(JMenu menu) {
			int[] sonarIDs = getSonarIDs();
			JMenuItem menuItem = new JMenuItem("Reboot sonar(s)");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
//					try {
						rebootSonars();
//					} catch (Svs5Exception e1) {
//						e1.printStackTrace();
//					}
				}
			});
			menu.add(menuItem);
			return 1;
		}
		
	}
	

}
