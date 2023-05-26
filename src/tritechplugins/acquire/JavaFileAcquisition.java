package tritechplugins.acquire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamUtils.PamCalendar;
import fileOfflineData.OfflineFileList;
import tritechgemini.fileio.CatalogException;
import tritechgemini.fileio.CatalogStreamObserver;
import tritechgemini.fileio.GeminiFileCatalog;
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
public class JavaFileAcquisition extends TritechDaqSystem  implements CatalogStreamObserver, ConfigurationObserver {
	
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
	
	public JavaFileAcquisition(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		tritechAcquisition.addConfigurationObserver(this);
	}

	@Override
	public boolean prepareProcess() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
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
				System.out.printf("Preparing Tritech File Acquisition on file %d start time %s\n", 
						i, PamCalendar.formatDBDateTime(PamCalendar.getSessionStartTime()));
				return true;
			};
		}
		return allFiles.length>0;
	}

	@Override
	public void configurationChanged() {
		currentFile = 0;
		lastRecordTime = null;
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
				PamCalendar.setSessionStartTime(recordTime);
				PamCalendar.setSoundFile(true);
				return true;
			}
			
		} catch (CatalogException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean start() {
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
				try {
					synchronized (synchObject) {
						currentCatalog = GeminiFileCatalog.getFileCatalog(fileList[currentFile], false);
					}
//					long firstRecordTime = currentCatalog.getFirstRecordTime();
//					try {
					boolean carryOn = currentCatalog.streamCatalog(JavaFileAcquisition.this);
					if (carryOn == false) {
						break;
					}
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

	@Override
	public boolean newImageRecord(GeminiImageRecordI glfImage) {
		/*
		 *  see if there is a bit gap between this and the last record which may be
		 *  caused by a gap in file data, in which case we may want to nudge a restart
		 *  to reset the binary store. 
		 */
//		if (lastRecordTime != null) {
//			long gap = glfImage.getRecordTime() - lastRecordTime;
//			if (gap > 10000L) {
//				System.out.printf("GLF Cataloges have a %d day %s second gap between files at %s\n", 
//						gap/(3600L*24L*1000L), PamCalendar.formatTime(gap), PamCalendar.formatDBDateTime(glfImage.getRecordTime()));
//				// so tell pamguard to restart and return false to stop this catalogue. 
//				lastRecordTime = null;
//				PamController.getInstance().pamStop();
//				restartLater();
//				return false;
//			}
//		}
		lastRecordTime = glfImage.getRecordTime();
		
		
		ImageDataBlock datablock = tritechProcess.getImageDataBlock();
		ImageDataUnit imageDataUnit = new ImageDataUnit(glfImage.getRecordTime(), 0, glfImage);
		
		PamCalendar.setSessionStartTime(glfImage.getRecordTime());
		delayPlayback(glfImage.getRecordTime());
		
		datablock.addPamData(imageDataUnit);
		return true;
	}
	
	private void restartLater() {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				long t = System.currentTimeMillis();
				while (System.currentTimeMillis() - t < 10000) {
					if (PamController.getInstance().getPamStatus() != PamController.PAM_IDLE) {
						try {
							Thread.sleep(500);
							continue;
						} catch (InterruptedException e) {
						}
					}
					System.out.printf("Tritch Acquisition issue restart after %dms wait\n", System.currentTimeMillis() - t);
					PamController.getInstance().startLater();
					break;
				}
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
//		System.out.println("Status data");
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
	
}
