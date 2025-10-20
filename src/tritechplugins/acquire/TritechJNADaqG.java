package tritechplugins.acquire;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;

import javax.swing.Timer;

import com.sun.jna.Pointer;

import PamUtils.PamCalendar;
import geminisdk.Svs5Exception;
import geminisdk.Svs5MessageType;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.RangeFrequencyConfig;
import tritechgemini.fileio.GLFGenericHeader;
import tritechgemini.fileio.LittleEndianDataInputStream;
import tritechgemini.imagedata.GLFStatusData;
import warnings.PamWarning;
import warnings.WarningSystem;

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

	/**
	 * Min time before gaps are reported after program start. 
	 */
	private long minGapReportTime = 10000;

	private long creationTime = System.currentTimeMillis();

	volatile private int lastSonarIndex = -1;

	volatile private long lastPingTime;

	private volatile boolean keepPinging;

	private boolean pingActive = false;

	private Thread pingThread;

	private PamWarning pingWarning;

	private int pingsSent, pingsReceived;

	public TritechJNADaqG(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		pingWarning = new PamWarning("Tritech Acquisition", "Ping warning", 2);
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

	long prevPingtime = 0;

	private int lastSonarId;
	private void pingNextSonar() {
		//		if (pingsSent%20 == 1) {
		//			System.out.printf("Pings sent / Received = %d/%d (diff %d)\n", pingsSent, pingsReceived, pingsReceived-pingsSent);
		//			System.out.println("Manual ping interval " + (System.currentTimeMillis()-lastPingTime));
		//		}
		//		Thread t = Thread.currentThread();
		//		System.out.println("Ping in thread " + t.getName() + " Id " + t.getId());
		lastPingTime = System.currentTimeMillis();
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		prevPingtime = System.currentTimeMillis();
		synchronized (pingSynch) {
//			if (1>2) {
//				try {
//					int dev = activeSonarList[0];
//					//				svs5Commands.gemxSetPingMode(dev, 0); // not doing anything.
//					svs5Commands.setPingMode(false, (short) (00));
//					setOnline(true, dev);
//					//				setOnline(false, dev);
//					//				setOnline(false, 0);
//
//				} catch (Svs5Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return ;
//			}
//			if (activeSonarList == null) {
				activeSonarList = makeActiveList();
//			}
			if (activeSonarList == null || activeSonarList.length == 0) {
				return;
			}
			lastSonarIndex++;
			if (lastSonarIndex >= activeSonarList.length) {
				lastSonarIndex = 0;
			}
			/*
			 *  lastSonarIndex may get updated in a different thread, so copy it locally
			 *  and do a final validity check.  
			 */			
			int thisSonarIndex = lastSonarIndex;
			if (thisSonarIndex < 0) {
				System.out.printf("Unlikely sonar index %d of %d\n", lastSonarIndex, activeSonarList.length);
				return;
			}

			int deviceId = activeSonarList[thisSonarIndex];
			lastSonarId = deviceId;
			SonarDaqParams sonarParams = daqParams.getSonarParams(deviceId);

			/**
			 * Don't ping if it's out of the water.
			 */
			if (isOOW(deviceId)) { 
				//				System.out.println("Skip ping since out of water device " + deviceId);
//				setOnline(false, deviceId);
				return;
			}

			Thread currentThread = Thread.currentThread();
			if (pingCount++ < maxPingReports) {
				System.out.printf("Ping sonar %d in thread %s Id %d at %s\n", deviceId, currentThread.getName(), 
						currentThread.getId(), PamCalendar.formatTime(System.currentTimeMillis(), true));
			}

			try {
//				System.out.printf("Ping sonar %d in thread %s Id %d at %s\n", deviceId, currentThread.getName(), 
//						currentThread.getId(), PamCalendar.formatTime(System.currentTimeMillis(), true));
				//				svs5Commands.setHighResolution(sonarParams.isHighResolution(), deviceId);
				svs5Commands.gemxSetPingMode(deviceId, 0);
				svs5Commands.gemxAutoPingConfig(deviceId, sonarParams.getRange(), 
						sonarParams.getGain(), (float) sonarParams.getFixedSoundSpeed());
				//				svs5Commands.setBoolCommand(GeminiStructure.SVS5_CONFIG_ONLINE, false, deviceId);
				//				svs5Commands.gemxSetRangeCompression(deviceId, 8, 1);
				//				setOnline(false, deviceId);
				//				svs5Commands.setPingMode(false, (short) 0);
				svs5Commands.gemxSendGeminiPingConfig(deviceId);
				

//				svs5Commands.gemxSetPingMode(0, 0);
				//				setOnline(true, deviceId);
				//				try {
				//					Thread.sleep(60);
				//				} catch (InterruptedException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}
				//				setOnline(false, deviceId);
//								svs5Commands.setPingMode(false, (short) (daqParams.getManualPingInterval()*10), deviceId);
			} catch (Svs5Exception e1) {
				e1.printStackTrace();
			}
			pingsSent++;
		}
	}

	@Override
	public void oowStateChange(GLFStatusData statusData) {
		super.oowStateChange(statusData);
		if (statusData.isOutOfWater()) {
			setOnline(false, statusData.m_deviceID);
		}
		else {
			prepareDevice(statusData.m_deviceID); // go through full prep sequence
		}
//		SonarDaqParams sonarParams = tritechAcquisition.getDaqParams().getSonarParams(statusData.m_deviceID);
//		setOnline(statusData.isOutOfWater() == false & sonarParams.isSetOnline(), statusData.m_deviceID);
	}

	/**
	 * Loop called in a separate thread which continuously pings the
	 * next sonar soon after the previous ping was received. 
	 */
	private void pingLoop() {
		//		System.out.println("Enter continuous ping loop");
		while (keepPinging) {
			if (doPingNow() & 1>0) {
				// still need to see if we need a delay
				long delay = 1;
				while (delay > 0) {
					if (keepPinging == false) {
						break;
					}
					/*
					 * Bit silly, but the interrupt of the other sleep can 
					 * hit this one, so need to stay in the loop 
					 * until we're genuinely ready to ping.
					 */
					delay = getPingDelay();
					if (delay > 0) {
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
							//						e.printStackTrace();
						}
					}
				}
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
		//		System.out.println("Leave continuous ping loop");
	}

	/**
	 * Do we need to wait before the next ping ? 
	 * @return
	 */
	private long getPingDelay() {
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		if (daqParams.isManualPingRate() == false) {
			return 0;
		}
		long wait = daqParams.getManualPingInterval() - (System.currentTimeMillis()-lastPingTime);
		return wait;
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
		long warningInterval = Math.max(1000, tritechAcquisition.getDaqParams().getManualPingInterval() * 5);
		if (pingGap > warningInterval) {
			int sonarID = -1;
			synchronized (pingSynch) {
				if (activeSonarList != null && lastSonarIndex >= 0 && lastSonarIndex < activeSonarList.length) {
					sonarID = activeSonarList[lastSonarIndex];
				}
			}
			if (System.currentTimeMillis() - creationTime > minGapReportTime) {
				/*
				 * This happens quite a lot at startup while thing settle, so 
				 * don't report these for at least 10s 
				 */
				String msg = String.format("%s: %dms passed since last sonar %d was pinged\n", 
						PamCalendar.formatDBDateTime(System.currentTimeMillis()), pingGap, sonarID);
				pingWarning.setWarningMessage(msg);
				WarningSystem.getWarningSystem().addWarning(pingWarning);
			}
			return true;
		}
		else {
			WarningSystem.getWarningSystem().removeWarning(pingWarning);
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
	public synchronized boolean prepareProcess() {

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
		pingThread = new Thread(threadRun, "Sonar ping loop");
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

			err = svs5Commands.setPingMode(false, (short) 999, (short) deviceId);

			err = svs5Commands.setSoSConfig(sonarParams.isUseFixedSoundSpeed(), sonarParams.getFixedSoundSpeed(), deviceId);

			ChirpMode chirpMode = new ChirpMode(sonarParams.getChirpMode());
			err = svs5Commands.setConfiguration(chirpMode, deviceId);
			//		System.out.println("setConfiguration chirpMode returned " + err);

			err = svs5Commands.setHighResolution(sonarParams.isHighResolution(), deviceId);

			RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
			rfConfig.m_frequency = sonarParams.getRangeConfig();
			err = svs5Commands.setConfiguration(rfConfig, deviceId);
			

//			svs5Commands.
			//		
			//	
			////		SimulateADC simADC = new SimulateADC(true);
			////		err = svs5Commands.setConfiguration(simADC);
			////		System.out.println("Simulate returned " + err);
			//

			//		ConfigOnline cOnline = new ConfigOnline(sonarParams.isSetOnline());
			//		err = svs5Commands.setConfiguration(cOnline, deviceId);
			setOnline(sonarParams.isSetOnline(), deviceId);

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
			imageFrameReceived(msgType, size, pointer);
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

	private void imageFrameReceived(int msgType, long size, Pointer pointer) {
		pingsReceived++;
		long loopTime = System.currentTimeMillis() - lastPingTime;
		if (pingCount < maxPingReports) {	
			try {
				byte[] byteData = pointer.getByteArray(0, (int) size);
				LittleEndianDataInputStream is = new LittleEndianDataInputStream(new ByteArrayInputStream(byteData));
				GLFGenericHeader header = new GLFGenericHeader();
				//		GLFImageRecord glfRecord = null; //new GLFImageRecord(null, 0, 0);
				header.read(is);

				Thread currentThread = Thread.currentThread();
				System.out.printf("Frame Received in thread %s Id %d sonar %d at %s\n", 
						currentThread.getName(), currentThread.getId(), header.tm_deviceId, 
						PamCalendar.formatTime(System.currentTimeMillis(), true));
			}
			catch (Exception e) {
				
			}
		}
		pingActive = false;
//		if (pingThread != null) {
//			pingThread.interrupt();
			//			pingThread = null; // don't do this, or we end up with two competing runloops
//		}
	}

	@Override
	public boolean stop() {
		keepPinging = false;
		return super.stop();
	}



}
