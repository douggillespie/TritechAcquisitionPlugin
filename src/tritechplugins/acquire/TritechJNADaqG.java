package tritechplugins.acquire;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.sun.jna.Pointer;

import geminisdk.Svs5Exception;
import geminisdk.Svs5MessageType;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.ConfigOnline;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.RangeFrequencyConfig;

/**
 * Implementation of Tritech JNA daq that uses a mix of SVS5 and GEMX functions
 * to try to improve dual sonar performance. 
 * @author dg50
 *
 */
public class TritechJNADaqG extends TritechJNADaq {
	
	private volatile int pingCount = 0;
	
	private Timer pingTimer;
	
	private int maxPingReports = 0;

	volatile private int lastSonarIndex = -1;

	volatile private long lastPingTime;

	private volatile boolean keepPinging;
	
	private boolean pingActive = false;

	private Thread pingThread;

	public TritechJNADaqG(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
//		pingTimer = new Timer(1000, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				pingTimerAction(e);
//			}
//		});
//		pingTimer.start();
	}

	/**
	 * This is primarily just for emergencies should there not have
	 * been a ping for more than a second. It can occurr if for any reason
	 * a sonar misses / fails to make it's ping. 
	 * @param e
	 */
	protected void pingTimerAction(ActionEvent e) {
		if (System.currentTimeMillis() - lastPingTime > 1000) {
			activeSonarList = makeActiveList();
			pingWithDelay(0);
		}
	}

	private void pingNextSonar() {
		synchronized (pingSynch) {
			TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
			if (activeSonarList == null) {
				activeSonarList = makeActiveList();
			}
			if (activeSonarList == null || activeSonarList.length == 0) {
				return;
			}
			lastSonarIndex++;
			if (lastSonarIndex >= activeSonarList.length) {
				lastSonarIndex = 0;
			}
			lastPingTime = System.currentTimeMillis();
			
			int deviceId = activeSonarList[lastSonarIndex];
			SonarDaqParams sonarParams = daqParams.getSonarParams(deviceId);

			if (pingCount++ < maxPingReports) {
				Thread currentThread = Thread.currentThread();
				System.out.printf("Ping sonar %d in thread %s Id %d\n", deviceId, currentThread.getName(), currentThread.getId());
			}

			try {
				//			System.out.println("ping sonar " + sonars[lastSonarIndex]);
				svs5Commands.gemxSetPingMode(deviceId, 0);
//				svs5Commands.setHighResolution(sonarParams.isHighResolution(), deviceId);
				svs5Commands.gemxAutoPingConfig(deviceId, sonarParams.getRange(), 
						sonarParams.getGain(), (float) sonarParams.getFixedSoundSpeed());
//				svs5Commands.gemxSetRangeCompression(deviceId, 8, 1);
				svs5Commands.gemxSendGeminiPingConfig(activeSonarList[lastSonarIndex]);
			} catch (Svs5Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Loop called in a separate thread which continuously pings the
	 * next sonar soon after the previous ping was received. 
	 */
	private void pingLoop() {
		System.out.println("Enter continuous ping loop");
		while (keepPinging) {
			if (doPingNow()) {
				pingActive = true;
				pingNextSonar();
			}
			else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					//				e.printStackTrace();
				}
			}
		}
		System.out.println("Leave continuous ping loop");
	}
	
	/**
	 * Is it time to ping ? Will be true if there isn't a currently
	 * active ping request in the system or if a sonar hasn't sent data
	 * for more than 500ms. 
	 * @return
	 */
	private boolean doPingNow() {
		if (pingActive == false) {
			return true;
		}
		long pingGap = System.currentTimeMillis() - lastPingTime;
		if (pingGap > 500) {
			int sonarID = -1;
			synchronized (pingSynch) {
				if (activeSonarList != null && lastSonarIndex >= 0 && lastSonarIndex < activeSonarList.length) {
					sonarID = activeSonarList[lastSonarIndex];
				}
			}
			System.out.printf("%dms passed since last sonar %d was pinged\n", pingGap, sonarID);
			return true;
		}
		
		return false;
	}

	/**
	 * Send the next ping in a new thread with a delay set in milliseconds.  
	 * @param delayMillis
	 */
	private void pingWithDelay(long delayMillis) {
		if (delayMillis == 0) {
			pingNextSonar();
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delayMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
				pingNextSonar();
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}

	@Override
	public boolean prepareProcess() {
		
		unprepareProcess();

		boolean ok = prepareAcquisition();
		
		lastSonarIndex = -1;
		keepPinging = true;
		Runnable threadRun = new Runnable() {
			
			@Override
			public void run() {
				pingLoop();
			}
		};
		pingThread = new Thread(threadRun);
		pingThread.start();
		
		return ok;
	}
	
	

	@Override
	public boolean prepareDevice(int deviceId) {
		int err = 0;
		try {
		
		SonarDaqParams sonarParams = tritechAcquisition.getDaqParams().getSonarParams(deviceId);

		GeminiRange range = new GeminiRange(sonarParams.getRange());
		err = svs5Commands.setConfiguration(range, deviceId);
		//		err += svs5Commands.setConfiguration(range, 1);
//		System.out.println("setRange returned " + err);
		err = setRange(sonarParams.getRange(), deviceId);
		
		setGain(sonarParams.getGain(), deviceId);
		
		err = svs5Commands.setPingMode(false, (short) 0);
//		svs5Commands.gemxSetPingMode(deviceId, 0);
//		svs5Commands.gemxAutoPingConfig(deviceId, sonarParams.getRange(), 
//				sonarParams.getGain(), (float) sonarParams.getFixedSoundSpeed());
//		System.out.println("setConfiguration pingMode returned " + err);
		
		err = svs5Commands.setSoSConfig(sonarParams.isUseFixedSoundSpeed(), sonarParams.getFixedSoundSpeed(), deviceId);
		
		ChirpMode chirpMode = new ChirpMode(sonarParams.getChirpMode());
		err = svs5Commands.setConfiguration(chirpMode, deviceId);
//		System.out.println("setConfiguration chirpMode returned " + err);

		err = svs5Commands.setHighResolution(sonarParams.isHighResolution(), deviceId);

		RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
		rfConfig.m_frequency = sonarParams.getRangeConfig();
		err = svs5Commands.setConfiguration(rfConfig);
//		System.out.println("setConfiguration returned " + err);
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

		ConfigOnline cOnline = new ConfigOnline(sonarParams.isSetOnline());
		err = svs5Commands.setConfiguration(cOnline, deviceId);
		//		cOnline.value = false;
		//		err += svs5Commands.setConfiguration(cOnline, 0);
//		System.out.println("setOnline returned " + err);



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
		if (pingThread != null) {
			keepPinging = false;
			try {
				pingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pingThread = null;
		}
	}

	public void preProcessSv5Callback(int msgType, long size, Pointer pointer) {
		if (msgType ==  Svs5MessageType.GLF_LIVE_TARGET_IMAGE) {
			imageFrameReceived();
		}
	}
	
	public void postProcessSv5Callback(int msgType, long size, Pointer pointer) {

		if (msgType ==  Svs5MessageType.GLF_LIVE_TARGET_IMAGE) {
//			System.out.println("postProcessSv5Callback");
//			pingNextSonar();
//			pingWithDelay(0);
		}
//		else {
//			switch (msgType) {
//			case 0: // status
//			case 10: // frame rate
//			case 4: // file status
//				break;
//			default:
//				System.out.println("Other SVS5 message type " + msgType);
//			}
//		}
	}
	
	private void imageFrameReceived() {
		if (pingCount < maxPingReports) {
		Thread currentThread = Thread.currentThread();
		System.out.printf("Frame Received in thread %s Id %d\n", currentThread.getName(), currentThread.getId());
		}
		pingActive = false;
		pingThread.interrupt();
	}

	@Override
	public boolean stop() {
		keepPinging = false;
		return super.stop();
	}


}
