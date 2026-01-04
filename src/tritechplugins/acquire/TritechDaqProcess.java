package tritechplugins.acquire;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.TimeZone;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import PamController.DataInputStore;
import PamController.InputStoreInfo;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.worker.PamWorkMonitor;
import PamguardMVC.PamProcess;
import geminisdk.OutputFileInfo;
import geminisdk.Svs5Exception;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechgemini.fileio.GLFCatalogCheck;
import tritechgemini.fileio.GLFFileCatalog;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.imagedata.GLFStatusData;
import tritechplugins.acquire.swing.DaqDialog;
import tritechplugins.acquire.swing.SonarImageOverlay;

/**
 * Tritech DAQ will acquire from and control the Gemini's. Because we're still not sure if
 * it will work best with JNA or JNI all calls to the sonars will be behind an interface so 
 * its easy to switch between them, though at time of writing, there is only a JNA.  
 * @author dg50
 *
 */
public class TritechDaqProcess extends PamProcess implements TritechRunMode, ConfigurationObserver {
	
	private ImageDataBlock imageDataBlock;
	private TritechAcquisition tritechAcquisition;
	private boolean isAcquire;
		
	private ArrayList<SonarStatusObserver> statusObservers = new ArrayList();
	
	private SonarStatusDataBlock sonarStatusDataBlock;
	/**
	 * @return the sonarStatusDataBlock
	 */
	public SonarStatusDataBlock getSonarStatusDataBlock() {
		return sonarStatusDataBlock;
	}

	/**
	 * This is a variety of ways of getting data in - from real time using svs5 via JNA
	 * to my own pure Java file reader. 
	 */
	private TritechDaqSystem tritechDaqSystem;
	private OutputFileInfo lastFileInfo;
	
	private Timer logCheckTimer;
	private boolean pamStarted;
	
	private long statusLogIntervalS = 60;
	
	public TritechDaqProcess(TritechAcquisition tritechAcquisition) {
		super(tritechAcquisition, null);
		this.tritechAcquisition = tritechAcquisition;
		imageDataBlock = new ImageDataBlock(this);
		imageDataBlock.setOverlayDraw(new SonarImageOverlay(tritechAcquisition, imageDataBlock));
		addOutputDataBlock(imageDataBlock);
		sonarStatusDataBlock = new SonarStatusDataBlock(this);
		addOutputDataBlock(sonarStatusDataBlock);
		sonarStatusDataBlock.SetLogging(new SonarStatusLogging(this, sonarStatusDataBlock));
		
		sortDaqSystem();
		
		
		logCheckTimer = new Timer(5000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runLogFileCheck();
			}
		});
		
		tritechAcquisition.addConfigurationObserver(this);
	}
	
	/**
	 * Call to sort out what type of daq system we actually want to use 
	 */
	private boolean sortDaqSystem() {
		isAcquire = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		if (isAcquire == false) {
			return false;
		}
		Class want = neededDaqClass();
		if (tritechDaqSystem != null && tritechDaqSystem.getClass() != want) {
			tritechDaqSystem.unprepareProcess();
			tritechDaqSystem.uninitialise();
			tritechDaqSystem = null;
		}
		if (tritechDaqSystem == null) {
			if (want == TritechJNADaqS.class) {
				tritechDaqSystem = new TritechJNADaqS(tritechAcquisition, this);
			}
			if (want == TritechJNADaqG.class) {
				tritechDaqSystem = new TritechJNADaqG(tritechAcquisition, this);
			}
			if (want == TritechJNAPlayback.class) {
				tritechDaqSystem = new TritechJNAPlayback(tritechAcquisition, this);
			}
			if (want == JavaFileAcquisition.class) {
				tritechDaqSystem = new JavaFileAcquisition(tritechAcquisition, this);
			}
		}
		return tritechDaqSystem != null;
	}
	
	/**
	 * get the class of the daq system we want to use. 
	 * @return
	 */
	private Class neededDaqClass() {
		isAcquire = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		if (params.getRunMode() == TritechDaqParams.RUN_ACQUIRE) {
			return TritechJNADaqG.class;
		}
		else {
			return JavaFileAcquisition.class;
//			return TritechJNAPlayback.class;
		}
		
	}
	
	/**
	 * Get the range of gain values. This is a percentage between 1 and 100.  <br>
	 *  see Sv5JavaInterface.h 
	 * @return range of gain values.
	 */
	public int[] getGainRange() {
		int[] range = {1, 100};
		return range;
	}
	/**
	 *  Range of range values. note that these are actually sent to
	 *  the sonar as double precision values.<br>
	 *  see Sv5JavaInterface.h 
	 * @return range of gain values. 
	 */
	public int[] getRangeRange() {
		int[] range = {1, 120};
		return range;
	}

	
	
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		if (daqParams.getRunMode() == TritechDaqParams.RUN_ACQUIRE) {
			/*
			 * If acquiring, we always want to set the default time zone for 
			 * this PC since that's what the Gemini data will be in, so this
			 * will correctly convert Gemini times to UTC. 
			 */
			GeminiFileCatalog.setTimeZone(TimeZone.getDefault());
		}
		else {
			String tzn = daqParams.getOfflinetimeZoneId();
			TimeZone tz = TimeZone.getTimeZone(tzn);
			if (tz == null) {
				tz = TimeZone.getDefault();
			}
			GeminiFileCatalog.setTimeZone(tz);
		}
		
		sortDaqSystem();
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		boolean realTime =  (params.getRunMode() == TritechDaqParams.RUN_ACQUIRE);
		if (tritechDaqSystem != null) {
			tritechDaqSystem.prepareProcess();
			/*
			 * If it's a real time system then start immediately so that the sonar
			 * shows on the display. 
			 * however, DON't do this when processing files or it reads every 
			 * file twice (which is annoying!)
			 */
			if (PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING  & realTime) {
				tritechDaqSystem.start();
			}
		}
	}

	@Override
	public void pamStart() {
//		shouldLogGLF = tritechAcquisition.getDaqParams().getRunMode() == TritechDaqParams.RUN_ACQUIRE;
		pamStarted = true;
		if (tritechDaqSystem != null) {
			tritechDaqSystem.start();
		}
		logCheckTimer.start();
		logCurrentStatus();
	}

	@Override
	public void pamStop() {
//		shouldLogGLF = false;
		pamStarted = false;
		logCheckTimer.stop();
		if (tritechDaqSystem != null) {
			tritechDaqSystem.stop();
		}
		logCurrentStatus();
	}

	/**
	 * Called when PAMGuard is really closing ...
	 */
	public void pamClose() {
		if (tritechDaqSystem != null) {
			tritechDaqSystem.uninitialise();
		}
		
	}

//	@Override
//	public int getNumSonars() {
//		if (tritechDaqSystem != null) {
//		return tritechDaqSystem.getNumSonars();
//		}
//		else {
//			return 0;
//		}
//	}
//
//	@Override
//	public int[] getSonarIDs() {
//		if (tritechDaqSystem != null) {
//			return tritechDaqSystem.getSonarIDs();
//		}
//		else {
//			return null;
//		}
//	}

	public JMenuItem createDaqMenu(Frame parentFrame) {
		JMenu menu = new JMenu(tritechAcquisition.getUnitName());
		JMenuItem menuItem;
		menuItem = new JMenuItem("Settings ...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});

		menuItem = new JMenuItem("Sonar positions ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tritechAcquisition.showGeometryDialog(parentFrame);
			}
		});
		menu.add(menuItem);
//		tritechDaqSystem.
		menuItem = new JMenuItem("Reboot");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tritechDaqSystem.rebootSonar(0);
			}
		});
		
		return menu;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		TritechDaqParams newParams = DaqDialog.showDialog(parentFrame, this, tritechAcquisition.getDaqParams());
		if (newParams != null) {
			tritechAcquisition.setDaqParams(newParams);
			prepareProcess();
			tritechAcquisition.configurationChanged();
		}
	}


//	public void setRange(int range) {
//		if (jnaDaq == null) {
//			return;
//		}
//		try {
//			jnaDaq.setRange(range, 0);
//		} catch (Svs5Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void setGain(int gain) {
//		if (jnaDaq == null) {
//			return;
//		}
//		try {
//			jnaDaq.setGain(gain, 0);
//		} catch (Svs5Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * New status data from a sonar. These come thick and fast whether acquiring or not
	 * @param sonarStatusData
	 */
	public void updateStatusData(SonarStatusData sonarStatusData) {
		// run some checks on the status to see if it looks OK. 
		
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateStatus(sonarStatusData);
		}
	}
	
	public void updateFileName(OutputFileInfo outputFileInfo) {
		
		checkLogging(outputFileInfo);
		
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateOutputFileInfo(outputFileInfo);
		}		
	}

	/**
	 * check that the logging is going OK and if it isn't reset it. 
	 * @param outputFileInfo
	 */
	private void checkLogging(OutputFileInfo outputFileInfo) {
		if (shouldLogging() == false) {
			return;
		}
		if (outputFileInfo == null) {
			return;
		}
		
		if (tritechAcquisition.getDaqParams().isAutoCatalogue()) {
			String completeFile = isFileComplete(lastFileInfo, outputFileInfo);
			if (completeFile != null) {
				catalogueOnlineFile(completeFile);
			}
		}
		
		if (outputFileInfo.equals(lastFileInfo)) {
			// seems to have got stuck
			System.out.println("Stuck file output " + outputFileInfo.toString());
		}
		lastFileInfo = outputFileInfo;
	}
	
	private void catalogueOnlineFile(String completeFile) {
		String glfFileName = completeFile.replace("data_", "log_");
		glfFileName = glfFileName.replace(".dat", ".glf");
		System.out.println("Time to catalogue file " + glfFileName);
		File glfFile = new File(glfFileName);
		if (glfFile.exists() == false) {
			System.out.println("But the file does not exist !!!!  " + glfFileName);
			return;
		}
		if (OnlineGLFCataloguer.getCurrentJobs() > 2) {
			System.out.println("Can't catalogue GLF file since too many jobs in queue");
			return;
		}
		OnlineGLFCataloguer catWorker = new OnlineGLFCataloguer(tritechAcquisition, glfFile);
		catWorker.execute();
	}

	/**
	 * Check to see if the file in lastfileInfo has been completed. This 
	 * is the case if the file in outputfileInfo is different or null. 
	 * @param lastFileInfo
	 * @param outputFileInfo
	 * @return
	 */
	private String isFileComplete(OutputFileInfo lastFileInfo, OutputFileInfo outputFileInfo) {
		if (lastFileInfo == null) {
			return null;
		}
		String lastFile = lastFileInfo.getM_strFileName();
		if (lastFile == null) {
			return null;
		}
		if (outputFileInfo == null) {
			return lastFile; // may happen at end
		}
		if (lastFile.equals(outputFileInfo.getM_strFileName()) == false) {
			return lastFile;
		}
		return null;
	}

	/**
	 * Check on a timer that something is updating. 
	 */
	private void runLogFileCheck() {
		if (shouldLogging() == false) {
			return;
		}
		
		boolean logErr = (lastFileInfo == null || System.currentTimeMillis() - lastFileInfo.getCreationTime() > 5000);
		if (logErr) {
			if (lastFileInfo == null) {
//				if (get)
			}
			else {
				long dt = (System.currentTimeMillis() - lastFileInfo.getCreationTime())/1000;
				System.out.printf("Log file info has not updated for %d seconds: %s\n", dt, lastFileInfo);
			}
		}
	}
	
	/**
	 * check whether or not we should be logging data at this point. 
	 * @return true if should be logging. 
	 */
	public boolean shouldLogging() {
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		return pamStarted && daqParams.isStoreGLFFiles() && daqParams.getRunMode() == TritechDaqParams.RUN_ACQUIRE;
	}
	
	/**
	 * Log whatever the current sonar status is (if it exists)
	 * called from pamStart and pamStop
	 */
	private void logCurrentStatus() {
		// do for all existing sonars.
		if (tritechDaqSystem == null) {
			return;
		}
		int[] sonarIds = tritechDaqSystem.getSonarIds();
		if (sonarIds == null) {
			return;
		}
		for (int i = 0; i < sonarIds.length; i++) {
			SonarStatusData status = tritechDaqSystem.getSonarStatusData(sonarIds[i]);
			saveStatusData(status.getStatusPacket());
		}
	}
	/**
	 * Save status data to database (i.e. put it in data unit and datablock
	 * and it will automatically save). 
	 * @param statusData
	 */
	public void saveStatusData(GLFStatusData statusData) {
		if (statusData == null) {
			return;
		}
////		long millis = GLFFileCatalog.cDateToMillis(statusData.m_fpgaTime);
//		long millis = PamCalendar.getTimeInMillis();
		long millis = GLFFileCatalog.cDateToMillis(statusData.genericHeader.m_timestamp);
		SonarStatusDataUnit ssdu = new SonarStatusDataUnit(millis, pamStarted, statusData);
		sonarStatusDataBlock.addPamData(ssdu);
	}

	public void updateFrameRate(int frameRate, double trueFPS) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFrameRate(frameRate, trueFPS);
		}
	}
	
	public void updateQueueSize(int svs5QueueSize) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateQueueSize(svs5QueueSize);
		}
		
	}

	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateLoggerPlayback(loggerPlaybackUpdate);
		}
	}

	public void updateFileIndex(int fileIndex) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFileIndex(fileIndex);
		}
	}
	
//	public String getLibVersion() {
//		if (jnaDaq == null) {
//			return "Offline";
//		}
//		return jnaDaq.getLibVersion();
//	}
	
//	public int getCurrentFrameRate() {
//		if (jnaDaq == null) {
//			return 0;
//		}
//		return jnaDaq.getCurrentFrameRate();
//	}
	
	/**
	 * Add an observer which will receive updates every time new status data arrive. 
	 * @param statusObserver
	 */
	public void addStatusObserver(SonarStatusObserver statusObserver) {
		statusObservers.add(statusObserver);
	}
	
	/**
	 * Remove a status observer. 
	 * @param statusObserver
	 */
	public void removeStatusObserver(SonarStatusObserver statusObserver) {
		statusObservers.remove(statusObserver);
	}

	/**
	 * @return the tritechAcquisition
	 */
	public TritechAcquisition getTritechAcquisition() {
		return tritechAcquisition;
	}

	public InputStoreInfo getStoreInfo(PamWorkMonitor workerMonitor, boolean detail) {
		if (tritechDaqSystem instanceof DataInputStore) {
			return ((DataInputStore) tritechDaqSystem).getStoreInfo(workerMonitor, detail);
		}
		else {
			return null;
		}
	}

	public boolean setAnalysisStartTime(long startTime) {
		if (tritechDaqSystem instanceof DataInputStore) {
			return ((DataInputStore) tritechDaqSystem).setAnalysisStartTime(startTime);
		}
		else {
			return false;
		}
	}

	public String getBatchStatus() {
//		System.out.println("Getting daq status from " + tritechDaqSystem);
		if (tritechDaqSystem instanceof DataInputStore) {
			return ((DataInputStore) tritechDaqSystem).getBatchStatus();
		}
		else {
			return null;
		}
	}

	/**
	 * @return the tritechDaqSystem
	 */
	public TritechDaqSystem getTritechDaqSystem() {
		return tritechDaqSystem;
	}

	public ImageDataBlock getImageDataBlock() {
		return imageDataBlock;
	}

	/**
	 * Called from side panel when logging button has changed. 
	 * though it gets is data from the mainparams set. 
	 */
	public void setGLFLogging() {
		boolean log = shouldLogging();
		/*
		 * Then what do we do with this ? 
		 */
		if (this.tritechDaqSystem != null) {
			this.tritechDaqSystem.setRecording(log);
		}
	}

	@Override
	public void configurationChanged() {
		setGLFLogging();
	}


}
