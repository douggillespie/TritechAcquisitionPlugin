package tritechplugins.acquire;

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
public class JavaFileAcquisition extends TritechDaqSystem  implements CatalogStreamObserver {
	
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


	private JavaFileDecorations javaFileDecorations;

	private long lastCallbackTime, lastFrameTime;
	
	public JavaFileAcquisition(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareProcess() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		OfflineFileList fileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		allFiles = fileList.asStringList();
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

		private int currentFile = 0;

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
					currentCatalog.streamCatalog(JavaFileAcquisition.this);
					currentFile++;
				}
				catch (CatalogException e) {
					System.out.println("Catalog error " + e.getMessage());
					System.out.println("In file " + fileList[currentFile]);
				}
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
	public void newImageRecord(GeminiImageRecordI glfImage) {
		ImageDataBlock datablock = tritechProcess.getImageDataBlock();
		ImageDataUnit imageDataUnit = new ImageDataUnit(glfImage.getRecordTime(), 0, glfImage);
		
		delayPlayback(glfImage.getRecordTime());
		
		datablock.addPamData(imageDataUnit);
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
	public void newStatusData(GLFStatusData statusData) {
		// this will arrive in the swing worker thread - that's fine. 
		System.out.println("Status data");
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
	
}
