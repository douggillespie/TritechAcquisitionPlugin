package tritechplugins.record;

import java.util.ArrayList;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;

public class GLFRecorderProcess extends PamProcess {

	private GLFRecorderCtrl recorderCtrl;
	
	private ImageDataBlock dataBuffer, finalBuffer;

	private boolean preparedOK;
	
	private volatile boolean shouldRecord;
	/**
	 * We're dealing with three image datablocks here. Could possibly manage with 
	 * just two ? 
	 * 1. The datablock in the sonar acquisition system
	 * 2. A datablock used to buffer data to be written
	 * 3. A datablock that holds a final queue of data to be written
	 * The three are needed so that the final writing from db3 is happening in 
	 * a different thread to that holding the original and intermediate data. This means that
	 * a lock does not need to be held on the original block while the writing takes place, all 
	 * happening in a different thread in any case.  
	 */
	
	private Object combinedBufferSynch = new Object();
	
	private GLFWriter glfWriter;

	public GLFRecorderProcess(GLFRecorderCtrl glfRecorderCtrl) {
		super(glfRecorderCtrl, null);
		this.recorderCtrl = glfRecorderCtrl;
		dataBuffer = new ImageDataBlock(null);
		finalBuffer = new ImageDataBlock(null);
		finalBuffer.setNaturalLifetime(Integer.MAX_VALUE);
		finalBuffer.addObserver(glfWriter = new GLFWriter(finalBuffer));
	}

	@Override
	public void pamStart() {
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		if (params.initialState == GLFRecorderParams.START_RECORD) {
			startRecording();
		}
	}

	@Override
	public void pamStop() {
		glfWriter.pamStop();
	}
	
	public boolean startRecording() {
		return startRecording(PamCalendar.getTimeInMillis());
	}
	
	public void stopRecording() {
		stopRecording(PamCalendar.getTimeInMillis());
	}
	
	/**
	 * Start recording, using the buffer as necessary to take data 
	 * from the given start time. 
	 * @param startTime start time for recording - should be now or in the very recent past. 
	 * @return true if started. 
	 */
	public boolean startRecording(long startTime) {
		if (shouldRecord) {
			return true; // no need to do anything 
		}
		synchronized (combinedBufferSynch) {
			shouldRecord = true;
			// and copy content from dataBuffer to finalBuffer
			ArrayList<ImageDataUnit> copy = null;
			synchronized (dataBuffer.getSynchLock()) {
				copy = dataBuffer.getDataCopy();
			}
			/*
			 *  can let go the synch on the input buffer so new data can be added to it. This 
			 *  process won't read out of it any more though since there is a lock below on 
			 *  newData.  
			 *  This doesn't open a file or anything, it just diversts data into the 
			 *  finalBuffer. The GLFWriter will automatically open a file as soon as something
			 *  arrives.  
			 */
			for (ImageDataUnit idu : copy) {
				if (idu.getTimeMilliseconds() >= startTime) {
					finalBuffer.addPamData(idu, idu.getUID());
				}
			}
		}
		// copy content of 
		return true;
	}
	
	/**
	 * Stop recording. 
	 */
	public void stopRecording(long timeMilliseconds) {
		synchronized (combinedBufferSynch) {
			shouldRecord = false; // data will divert back to the storage buffer
			// this may cause some delay, but I don't see a way around it. Need 
			// to complete clear the finalBuffer before we can put anything else into it. 
			glfWriter.flushAndClose();
		}
	}
	
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == getParentDataBlock()) {
			newParentData(arg);
		}
		
	}

	private void newParentData(PamDataUnit arg) {
		ImageDataUnit imageData = (ImageDataUnit) arg;
		/*
		 *  check it's actually got GLF data - can't handle anything else at the moment,
		 *  but hope to resolve this in future.  
		 */
		GeminiImageRecordI imageRecord = imageData.getGeminiImage();
		if (imageRecord instanceof GLFImageRecord == false) {
			return; // can't do anything with this at the moment. 
		}
		/**
		 * If we're already recording, then copy the data straight into the final buffer.
		 * If we're not yet recording, then copy the data into the intermediate buffer
		 */
		synchronized (combinedBufferSynch) {
			if (shouldRecord) {
				finalBuffer.addPamData(imageData);
			}
			else {
				dataBuffer.addPamData(imageData);
			}
		}		
	}
	


	/**
	 * Recording takes place in a separate thread using a queue of data between 
	 * the main process receiving data and the actual writer. 
	 * @author dg50
	 *
	 */
	private class RecordThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void prepareProcess() {
		preparedOK = true;
		super.prepareProcess();
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		PamDataBlock sourceImages = PamController.getInstance().getDataBlockByLongName(params.imageDataSource);
		setParentDataBlock(sourceImages);
		if (sourceImages == null) {
			preparedOK = false;
		}
		dataBuffer.setNaturalLifetime(params.bufferSeconds);
		preparedOK &= recorderCtrl.checkOutputFolder(true);
		glfWriter.setMaxFileSize(params.maxSizeMegabytes);
		glfWriter.setRootFolder(params.outputFolder);
	}

	@Override
	public boolean prepareProcessOK() {
		prepareProcess();
		return preparedOK;
	}


}
