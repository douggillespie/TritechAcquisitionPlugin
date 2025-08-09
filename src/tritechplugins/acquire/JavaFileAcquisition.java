package tritechplugins.acquire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import Acquisition.FolderInputSystem;
import PamController.DataInputStore;
import PamController.InputStoreInfo;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.worker.PamWorkMonitor;
import fileOfflineData.OfflineFileList;
import pamguard.GlobalArguments;
import tritechgemini.fileio.CatalogException;
import tritechgemini.fileio.CatalogStreamObserver;
import tritechgemini.fileio.CatalogStreamSummary;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.offline.TritechFileFilter;
import tritechplugins.acquire.swing.JavaFileStatusBar;
import tritechplugins.display.swing.SonarDisplayDecoration;
import tritechplugins.display.swing.SonarDisplayDecorations;

/**
 * Acquisition system to use the pure java file reader which can work without
 * any use of the svs5 library. 
 * @author dg50
 *
 */
public class JavaFileAcquisition extends TritechDaqSystem  implements CatalogStreamObserver, ConfigurationObserver, DataInputStore {
	
	/**
	 * will need a few changes to GeminiFileCatalog to enable two functions
	 * 1. StartFullRead(a callback for data)
	 * 2. Stop
	 * Currently the catalogs are cataloging data but not actually reading it and 
	 * data are then accessed at random. For this we just want to read records in turn
	 * and send them back here through the callback, along with other information such as
	 * status records and EOF. 
	 */

	/**
	 * List of all selected files. Will work through in order. 
	 */
	private String[] allFiles;
	
	private ArrayList<JavaFileObserver> javaFileObservers = new ArrayList<>();
	
	private GeminiFileCatalog<GeminiImageRecordI> currentCatalog;

	private volatile boolean continueStream;
	
	private StreamingThread streamThread;
	
	private Object synchObject = new Object();

	private int currentFile = 0;

	private Long lastRecordTime;

	private JavaFileDecorations javaFileDecorations;

	private long lastCallbackTime, lastFrameTime;
	
	/**
	 * information used in a restart situation if there was a gap 
	 * within or between image files. 
	 */
	private int recordsSkipAtStart = 0;
	private long restartFirstRecordTime = 0;

	private volatile boolean stopPressed;
	
	public JavaFileAcquisition(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		tritechAcquisition.addConfigurationObserver(this);
	}

	@Override
	public boolean prepareProcess() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		
		checkCommandLineFolder(params);
		
		OfflineFileList fileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders() | true);
		fileList.sortByFileName();
		allFiles = fileList.asStringList();
		// set this to null or it fires off every restart at line 254
		lastRecordTime = null;
		/*
		 * Need to get the time from the first file and set the calendar time from it. Will also need to do this from every stream !
		 * this first one needs to be done right at start, before binary stores are created or all goes wrong. 
		 */
		for (int i = currentFile; i < allFiles.length; i++) {
			/*
			 * Chance that very first file may be corrupt for some reason, so be 
			 * pragmatic and keep going until a file has a time. 
			 */
			if (extractStartTime(allFiles[i])) {
				File aFile = new File(allFiles[i]);
				System.out.printf("Preparing Tritech File Acquisition on file %d %s start time %s\n", 
						i, aFile.getName(), PamCalendar.formatDBDateTime(PamCalendar.getSessionStartTime()));
				return true;
			};
		}
		return allFiles.length>0;
	}

	/**
	 * Check there isn't a command line folder. This will come from the 
	 * batch processor and will be the GlobaWafFolderArg. 
	 * @param params
	 */
	private boolean checkCommandLineFolder(TritechDaqParams params) {
		String globalFolder = GlobalArguments.getParam(FolderInputSystem.GlobalWavFolderArg);
		if (globalFolder == null) {
			return false;
		}
		params.setOfflineFileFolder(globalFolder);
		return true;
	}

	@Override
	public void configurationChanged() {
		currentFile = 0;
		lastRecordTime = null;
		restartFirstRecordTime = 0;
	}

	/**
	 * Get the time of the first record from a file and set it as the session start time. 
	 * @param filePath
	 * @return true if record found and time set. 
	 */
	private boolean extractStartTime(String filePath) {
		/**
		 * If PAmCalendar.setSoundFile is true, then all calls to PAMCalendat.getTime
		 * will get sessionStartTime + soundFileTimeInMillis. No plans to set soundFileTimeMillis
		 * so can do all timing by continually setting sessionStartTime here at the start and for 
		 * every subsequent record. 
		 */
		File glfFile = new File(filePath);
		if (glfFile.exists() == false) {
			return false;
		}
		try {
			GeminiFileCatalog<GeminiImageRecordI> fileCatalog = GeminiFileCatalog.getFileCatalog(filePath, true);
			long recordTime = fileCatalog.getFirstRecordTime();
			if (recordTime != Long.MIN_VALUE) {
				PamCalendar.setSoundFile(true);
				PamCalendar.setSessionStartTime(Math.max(recordTime, restartFirstRecordTime));
				PamCalendar.setSoundFileTimeInMillis(0);
				return true;
			}
			
		} catch (CatalogException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean start() {
		stopPressed = false;
		continueStream = true;
		lastCallbackTime = System.currentTimeMillis();
		lastFrameTime = 0;
		synchronized (synchObject) {
			streamThread = new StreamingThread(allFiles);
			streamThread.execute();
		}
		return true;
	}

	@Override
	public boolean stop() {
		stopPressed = true;
		continueStream = false;
		synchronized (synchObject) {
			if (currentCatalog != null) {
				currentCatalog.stopCatalogStream();
			}
		}
		// wait for up to a couple of seconds.
//		int waitCount = 0;
//		while (waitCount++ < 20) {
//			// might get stuck here if the callback is modifying AWT
//			if (streamThread.isAlive() == false) {
//				break;
//			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		// then wait for thread to exit ?
		return true;
	}

	@Override
	public boolean isRealTime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unprepareProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uninitialise() {
		// TODO Auto-generated method stub
		
	}
	
	private class StreamingThread extends SwingWorker<Integer, JavaFileStatus> {

		private String[] fileList;

		public StreamingThread(String[] allFiles) {
			this.fileList = allFiles;
		}
		
		@Override
		protected Integer doInBackground() throws Exception {
			if (fileList == null) {
				return 0;
			}
			while (currentFile < fileList.length && continueStream) {
				JavaFileStatus fileStatus = new JavaFileStatus(fileList.length, currentFile, fileList[currentFile]);
				this.publish(fileStatus);

//				try {
//					File aFile = new File(fileList[currentFile]);
//					System.out.println("Starting stream of file " + aFile.getName());
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
				
				try {
					synchronized (synchObject) {
						currentCatalog = GeminiFileCatalog.getFileCatalog(fileList[currentFile], false);
					}
//					long firstRecordTime = currentCatalog.getFirstRecordTime();
//					try {
					
					CatalogStreamSummary streamSummary = currentCatalog.streamCatalog(JavaFileAcquisition.this);
					if (streamSummary.endReason == CatalogStreamSummary.PROCESSSTOP && stopPressed == false) {
						/*
						 *  this get's called when there is a gap in the file. This may be within a file
						 *  or, more often, between files. If it's the first record of a new file, then all
						 *  we need to do is break and issue a restart. 
						 *  If it's a higher record number, then we need to move the counter on to the next file. 
						 */
						restartFirstRecordTime = streamSummary.lastRecordTime;
						if (streamSummary.recordsStreamed == 0) {
							// there seems to be an error in that file, so skip to
							// the next
							currentFile++;
						}
						else {
							recordsSkipAtStart = streamSummary.recordsStreamed-1;
						}
						PamController.getInstance().pamStop();
						restartLater();
						break;
					}
					recordsSkipAtStart = 0;
//					if (carryOn == false) {
//						currentFile++;
//						break;
//					}
//					lastRecordTime = currentCatalog.getLastRecordTime();
//					}
					// don't need since outer catch changed from CatalogException to Exception
//					catch (Exception e) {
//						System.out.println("Error streaming file " + fileList[currentFile]);
//						System.out.println(e.getMessage());
//					}
				}
				catch (Exception e) {
					System.out.println("Catalog error " + e.getMessage());
					System.out.println("In file " + fileList[currentFile]);
//					break;
				}
				currentFile++;
			}
			return currentFile;
		}

		@Override
		protected void process(List<JavaFileStatus> chunks) {
			for (JavaFileStatus status : chunks) {
				notifyObservers(status);
			}
		}

		@Override
		protected void done() {
			// stop after one extra loop around AWT queue
			PamController.getInstance().stopLater();
		}
		
	}
	
	int gapCount = 0;
 
	@Override
	public boolean newImageRecord(GeminiImageRecordI glfImage) {
		/*
		 * Check the restartFirstRecordTime value. This will mostly be
		 * zero, but if there has been a restart after a gap, then it will
		 * be non-zero and all records prior to that time should be ignored
		 */
		if (glfImage.getRecordTime() <= restartFirstRecordTime) {
			return true;
		}
		
		/*
		 *  see if there is a bit gap between this and the last record which may be
		 *  caused by a gap in file data, in which case we may want to nudge a restart
		 *  to reset the binary store. 
		 */
		if (lastRecordTime != null) {
			long gap = glfImage.getRecordTime() - lastRecordTime;
			if (gap > 10000L) {  // 10seconds
				if (++gapCount > 10) {
					System.out.println("Getting into lots of gap counts");
				}
				System.out.printf("GLF Cataloges have a %d day %s second gap between files at %s\n", 
						gap/(3600L*24L*1000L), PamCalendar.formatTime(gap), PamCalendar.formatDBDateTime(glfImage.getRecordTime()));
				System.out.printf("Record time %s, current PAM time %s\n", PamCalendar.formatDBDateTime(glfImage.getRecordTime()),
						PamCalendar.formatDBDateTime(PamCalendar.getTimeInMillis()));
				// so tell pamguard to restart and return false to stop this catalogue. 
				lastRecordTime = null;
				return false;
			}
			else if (gap < -10000L) {
				System.out.printf("GLF Cataloges going backwards in time %d day %s second gap between files at %s\n", 
						gap/(3600L*24L*1000L), PamCalendar.formatTime(gap), PamCalendar.formatDBDateTime(glfImage.getRecordTime()));
			}
		}
		lastRecordTime = glfImage.getRecordTime();
		
		super.newGLFLiveImage((GLFImageRecord) glfImage);
//		
//		ImageDataBlock datablock = tritechProcess.getImageDataBlock();
//		ImageDataUnit imageDataUnit = new ImageDataUnit(glfImage.getRecordTime(), 0, glfImage);
//		
		PamCalendar.setSoundFileTimeInMillis(glfImage.getRecordTime()-PamCalendar.getSessionStartTime());
		delayPlayback(glfImage.getRecordTime());
//		
//		datablock.addPamData(imageDataUnit);
		return true;
	}
	
	private void restartLater() {

		System.out.println("Tritch Acquisition launch restart thread at " + PamCalendar.formatDBDateTime(System.currentTimeMillis()));
		System.out.println("Current PAMGuard status is " + PamController.getInstance().getPamStatus());
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				long t = System.currentTimeMillis();
				while (System.currentTimeMillis() - t < 20000) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
						break;
					}
//					System.out.println("Current PAMGuard status is still  " + PamController.getInstance().getPamStatus());
				}
				System.out.printf("Tritch Acquisition issue restart after %dms\n", System.currentTimeMillis() - t);
				PamController.getInstance().startLater(false);
			}
		};
		
		new Thread(r).start();
	}

	/**
	 * think about delaying playback if speed is set. 
	 * @param recordTime
	 */
	private int delayPlayback(long recordTime) {
		double speed = tritechAcquisition.getDaqParams().getPlaySpeed();
		if (speed <= 0 || lastFrameTime == 0) {
			// no need to do anything apart from 
			lastFrameTime = recordTime;
			lastCallbackTime = System.currentTimeMillis();
			return 0;
		}
		long interFrameMillis = recordTime-lastFrameTime;
		long now = System.currentTimeMillis();
		long elapsedMills = now - lastCallbackTime;
		int delay = 0;
		if (interFrameMillis > elapsedMills * speed) {
			delay = (int) (interFrameMillis/speed-elapsedMills);
			delay = Math.min(1000, delay);
			if (delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		lastFrameTime = recordTime;
		lastCallbackTime = System.currentTimeMillis();
		return delay;
	}

	@Override
	public boolean newStatusData(GLFStatusData statusData) {
		// this will arrive in the swing worker thread - that's fine. 
//		System.out.println("Status data OOW  " + statusData.);
		super.newStatusPacket(statusData);
		return true;
	}

	@Override
	public SonarDisplayDecorations getSwingDecorations() {
		if (javaFileDecorations == null) {
			javaFileDecorations = new JavaFileDecorations();
		}
		return javaFileDecorations;
	}
	
	class JavaFileDecorations extends SonarDisplayDecorations {

		@Override
		public SonarDisplayDecoration getTopBar() {
			return new JavaFileStatusBar(tritechAcquisition, JavaFileAcquisition.this);
		}

//		@Override
//		public SonarDisplayDecoration getNorthEastInset() {
//			return new 
//		}
		
	}
	
	private void notifyObservers(JavaFileStatus javaFileStatus) {
		for (JavaFileObserver obs: javaFileObservers) {
			obs.update(javaFileStatus);
		}
	}

	/**
	 * Add an observer to get updates when moving to next file. 
	 * @param javaFileObserver
	 */
	public void addObserver(JavaFileObserver javaFileObserver) {
		javaFileObservers.add(javaFileObserver);
	}
	/**
	 * Remove an observer from updates when moving to next file. 
	 * @param javaFileObserver
	 */
	public void removeObserver(JavaFileObserver javaFileObserver) {
		javaFileObservers.remove(javaFileObserver);
	}

	@Override
	protected void rebootSonar(int deviceId) {
		// nothing to do for file analysis
	}

	@Override
	protected void newSonar(SonarStatusData sonarData) {
		// TODO Auto-generated method stub
		
	}

//	@Override
	public InputStoreInfo getStoreInfo(boolean detail) {
		if (allFiles == null || allFiles.length == 0) {
			return null;
		}
		int nFiles = allFiles.length;
		GeminiFileCatalog firstCat;
		try {
			firstCat = GeminiFileCatalog.getFileCatalog(allFiles[0], true);
			GeminiFileCatalog lastCat = GeminiFileCatalog.getFileCatalog(allFiles[nFiles-1], true);

			InputStoreInfo storeInfo = new InputStoreInfo(tritechAcquisition, nFiles, firstCat.getFirstRecordTime(), lastCat.getFirstRecordTime(), lastCat.getLastRecordTime());
			if (detail) {
				long[] allStarts = new long[nFiles];
				long[] allEnds = new long[nFiles];
				for (int i = 0; i < nFiles; i++) {
					GeminiFileCatalog fileCat = GeminiFileCatalog.getFileCatalog(allFiles[i], true);
					if (fileCat == null) {
						continue;
					}
					allStarts[i] = fileCat.getFirstRecordTime();
					allEnds[i] = fileCat.getLastRecordTime();
				}
				storeInfo.setFileStartTimes(allStarts);
				storeInfo.setFileEndTimes(allEnds);
				/*
				 * Run some checks to check that everything is in order. 
				 * 
				 */
//				for (int i = 0; i < nFiles; i++) {
//					if (allEnds[i] < allStarts[i]) {
//						System.out.printf("file %s has a negative duration of %d millis\n", allFiles[i], allEnds[i]-allStarts[i]);
//					}
//					if (i > 0) {
//						long gap = allStarts[i] - allEnds[i-1];
//						if (gap > 10000 || gap < 0) {
//							System.out.printf("%3.1f second gap between files %s and %s\n", 
//									(double)gap/1000., allFiles[i-1], allFiles[i]);
//						}
//						
//					}
//				}
				
			}
			return storeInfo;

		} catch (CatalogException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean setAnalysisStartTime(long startTime) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getBatchStatus() {
		int nFiles = 0;
		if (allFiles == null) {
			return null;
		}
		nFiles = allFiles.length;
		int generalStatus = PamController.getInstance().getPamStatus();
		String currFile;
		if (currentFile < allFiles.length) {
			currFile = allFiles[currentFile];
		}
		else {
			currFile = "Processing complete";
		}
		String bs = String.format("%d,%d,%d,%s", nFiles,currentFile,generalStatus,currFile);
//		System.out.println("Tritech batch status: " + bs);
		return bs;
	}

	@Override
	public InputStoreInfo getStoreInfo(PamWorkMonitor workerMonitor, boolean detail) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
