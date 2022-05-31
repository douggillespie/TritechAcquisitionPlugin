package tritechplugins.acquire;

import javax.swing.SwingWorker;

import fileOfflineData.OfflineFileList;
import tritechgemini.fileio.CatalogStreamObserver;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.imagedata.GLFStatusData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.offline.TritechFileFilter;

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
	
	
	private GeminiFileCatalog<GeminiImageRecordI> currentCatalog;

	private volatile boolean continueStream;
	
	private StreamingThread streamThread;
	
	private Object synchObject = new Object();
	
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
				synchronized (synchObject) {
					currentCatalog = GeminiFileCatalog.getFileCatalog(fileList[currentFile], false);
				}
				currentCatalog.streamCatalog(JavaFileAcquisition.this);
				currentFile++;
			}
			return currentFile;
		}
		
	}

	@Override
	public void newImageRecord(GeminiImageRecordI glfImage) {
		// this will arrive in the swing worker thread - that's fine. 
		
	}

	@Override
	public void newStatusData(GLFStatusData statusData) {
		// this will arrive in the swing worker thread - that's fine. 
		
	}
	
//	private class StreamingThread implements Runnable {
//
//		private String[] fileList;
//
//		public StreamingThread(String[] allFiles) {
//			this.fileList = allFiles;
//			for (int )
//		}
//
//		@Override
//		public void run() {
//			
//			
//		}
//		
//	}

}
