package tritechplugins.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.ThreadedObserver;
import PamguardMVC.dataSelector.DataSelector;
import difar.DifarParameters.DifarTriggerParams;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.record.logging.GLFRecorderLogging;

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

	private ArrayList<TriggerMonitor> triggerMonitors = new ArrayList<>();

	private long recordEndTime;

	/**
	 * GLF Recorder bookkeeping
	 */
	private GLFRecorderDataBlock recorderDataBlock;

	private GLFRecorderDataUnit currentDataUnit;

	public GLFRecorderProcess(GLFRecorderCtrl glfRecorderCtrl) {
		super(glfRecorderCtrl, null);
		this.recorderCtrl = glfRecorderCtrl;
		dataBuffer = new ImageDataBlock(null, "GLF Recording Buffer");
		finalBuffer = new ImageDataBlock(null, "GLF Record output");
		finalBuffer.setNaturalLifetime(Integer.MAX_VALUE);
		finalBuffer.addObserver(glfWriter = new GLFWriter(finalBuffer), true);
		ThreadedObserver threadObs = finalBuffer.findThreadedObserver(glfWriter);
		if (threadObs != null) {
//			threadObs.setMaxJitter(30000);
		}
		recorderDataBlock = new GLFRecorderDataBlock(glfRecorderCtrl, this);
		recorderDataBlock.SetLogging(new GLFRecorderLogging(recorderCtrl, recorderDataBlock));
		addOutputDataBlock(recorderDataBlock);
		
		addOutputDataBlock(dataBuffer);
		addOutputDataBlock(finalBuffer);
	}

	@Override
	public void pamStart() {
		recordEndTime = 0;
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		if (params.initialState == GLFRecorderParams.START_RECORD) {
			startRecording();
		}
	}

	@Override
	public void pamStop() {
		stopRecording();
	}

	public boolean startRecording() {
		return startRecording(PamCalendar.getTimeInMillis(), Long.MAX_VALUE);
	}

	/**
	 * Get information about the data buffer. 
	 * @return
	 */
	public BufferState getBufferState() {
		long firstTime = 0, lastTime = 0;
		ImageDataUnit fu = dataBuffer.getFirstUnit();
		ImageDataUnit lu = dataBuffer.getLastUnit();
		int[] typesCount = dataBuffer.getImageTypesCount();
//		int nu = dataBuffer.getUnitsCount();
		if (fu != null && lu != null) {
			firstTime = fu.getTimeMilliseconds();
			lastTime = lu.getTimeMilliseconds();
		}
		GLFRecorderParams params = recorderCtrl.getRecorderParams();

		return new BufferState(firstTime, lastTime, typesCount, params.bufferSeconds);		
	}

	/**
	 * Get the current file being recorded. 
	 * @return
	 */
	public File getCurrentFile() {
		return glfWriter.getCurrentGLFFile();
	}

	/**
	 * Get information about if recording (or if should be recording). 
	 * @return
	 */
	public boolean getRecordState() {
		return shouldRecord;
	}

	/**
	 * Start a recording that will go forever. If possible, take data from buffer. 
	 * @param start
	 */
	public boolean startRecording(long start) {
		return startRecording(start, Long.MAX_VALUE);
	}

	/**
	 * Start recording, using the buffer as necessary to take data 
	 * from the given start time. 
	 * @param startTime start time for recording - should be now or in the very recent past. 
	 * @return true if started. 
	 */
	public boolean startRecording(long startTime, long endTime) {
		return startRecording(startTime, endTime, null);
	}

	/**
	 * Start a recording from given start time, to given end time. 
	 * @param startTime
	 * @param endTime
	 * @param triggerData passed through so can add trigger info to database record. 
	 * @return true on success
	 */
	public boolean startRecording(long startTime, long endTime, GLFTriggerData triggerData) {
		recordEndTime = Math.max(recordEndTime, endTime);
		if (shouldRecord) {
			return true; // no need to do anything 
		}
		synchronized (combinedBufferSynch) {
			shouldRecord = true;
			// prepare a data unit. 
			currentDataUnit = new GLFRecorderDataUnit(startTime, endTime, triggerData);

			// and copy content from dataBuffer to finalBuffer
			ArrayList<ImageDataUnit> copy = null;
			synchronized (dataBuffer.getSynchLock()) {
				copy = dataBuffer.getDataCopy();
				dataBuffer.clearAll();
			}
			/*
			 *  can let go the synch on the input buffer so new data can be added to it. This 
			 *  process won't read out of it any more though since there is a lock below on 
			 *  newData.  
			 *  This doesn't open a file or anything, it just diverts data into the 
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
	public void stopRecording() {
		synchronized (combinedBufferSynch) {
			shouldRecord = false; // data will divert back to the storage buffer
			// this may cause some delay, but I don't see a way around it. Need 
			// to complete clear the finalBuffer before we can put anything else into it. 

			glfWriter.flushAndClose();
			recordEndTime = 0;

			ImageDataUnit lastRec = glfWriter.getLastWrittenRecord();
			if (lastRec != null && currentDataUnit != null) {
				currentDataUnit.setEndTime(lastRec.getTimeMilliseconds());
				recorderDataBlock.addPamData(currentDataUnit);
				currentDataUnit = null;
			}
		}
	}

	/**
	 * Setup however many triggers there are that will initiate automatic recording. 
	 */
	public void setupTriggers() {
		clearTriggers();
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		Set<String> keys = params.getTriggerHashKeys();
		for (String aKey : keys) {
			GLFTriggerData trigParams = params.getTriggerData(aKey, false);
			if (trigParams == null || trigParams.enabled == false) {
				continue;
			}
			PamDataBlock dataBlock = recorderCtrl.getPamConfiguration().getDataBlockByLongName(aKey);
			if (dataBlock != null) {
				TriggerMonitor trigMon = new TriggerMonitor(dataBlock, trigParams);
				dataBlock.addObserver(trigMon, false);
				triggerMonitors.add(trigMon);
			}
			else {
				System.out.println("GLF Recorder trigger missing datablock: " + aKey);
			}
		}
	}

	/**
	 * Clear all trigger monitors prior to rebuilding
	 */
	public void clearTriggers() {
		for (TriggerMonitor aMonitor : triggerMonitors) {
			clearTrigger(aMonitor);
		}
		triggerMonitors.clear();
	}

	private void clearTrigger(TriggerMonitor aMonitor) {
		aMonitor.dataBlock.deleteObserver(aMonitor);
	}

	/**
	 * fire a trigger when a new dataunit arrives. 
	 * @param dataBlock
	 * @param pamDataUnit
	 * @param trigParams
	 */
	public void fireTrigger(PamDataBlock dataBlock, PamDataUnit pamDataUnit, GLFTriggerData trigParams) {
		/*
		 * Check the dataselector for this data
		 */
		DataSelector ds = dataBlock.getDataSelector(GLFRecorderCtrl.DATASELECTNAME, false);
		if (ds != null) {
			double score = ds.scoreData(pamDataUnit);
			if (score <= 0) {
				return;
			}
		}
		// if recording is already running, then pass to continuetrigger to push back the end time. 
		synchronized (combinedBufferSynch) {
			//			if (shouldRecord) {
			//				// probably already recording, so push back
			//				continueTrigger(dataBlock, pamDataUnit, trigParams);
			//			}
			//			else {
			/**
			 * Can just make the call to startRecording since it will extend to the end
			 * time and will already just continue if it's running a recording. 
			 */
			long s = pamDataUnit.getTimeMilliseconds() - trigParams.preSeconds*1000;
			long e = pamDataUnit.getEndTimeInMilliseconds() + trigParams.postSeconds*1000;
			startRecording(s, e, trigParams);
			//			}
		}

	}

	//	/**
	//	 * continue a trigger when a new dataunit arrives. 
	//	 * @param dataBlock
	//	 * @param pamDataUnit
	//	 * @param trigParams
	//	 */
	//	public void continueTrigger(PamDataBlock dataBlock, PamDataUnit pamDataUnit, GLFTriggerData trigParams) {
	//		
	//	}

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
//		GeminiImageRecordI imageRecord = imageData.getGeminiImage();
//		if (imageRecord instanceof GLFImageRecord == false) {
//			return; // can't do anything with this at the moment. 
//		}
		synchronized (combinedBufferSynch) {
			/*
			 * Whenever recording is started, we set an end time. If we're now
			 * beyond this, then we should stop recording. 
			 */
			if (imageData.getTimeMilliseconds() > recordEndTime) {
				if (shouldRecord) {
					stopRecording();
				}
				shouldRecord = false;
			}
			/*
			 * If we're already recording, then copy the data straight into the final buffer.
			 * If we're not yet recording, then copy the data into the intermediate buffer
			 */
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

	private class TriggerMonitor extends PamObserverAdapter {

		private PamDataBlock dataBlock;
		private GLFTriggerData trigParams;

		public TriggerMonitor(PamDataBlock dataBlock, GLFTriggerData trigParams) {
			super();
			this.dataBlock = dataBlock;
			this.trigParams = trigParams;
		}

		@Override
		public String getObserverName() {
			return recorderCtrl.getUnitName() + " trigger monitor";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			fireTrigger(dataBlock, pamDataUnit, trigParams);
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			fireTrigger(dataBlock, pamDataUnit, trigParams);
		}

	}

	@Override
	public void prepareProcess() {
		preparedOK = true;
		super.prepareProcess();
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		PamDataBlock sourceImages = recorderCtrl.getPamConfiguration().getDataBlockByLongName(params.imageDataSource);
		setParentDataBlock(sourceImages, false);
		if (sourceImages == null) {
			preparedOK = false;
		}
		dataBuffer.setNaturalLifetime(params.bufferSeconds);
		preparedOK &= recorderCtrl.checkOutputFolder(true);
		glfWriter.setMaxFileSize(params.maxSizeMegabytes);
		glfWriter.setRootFolder(params.outputFolder);
		setupTriggers();
	}

	@Override
	public boolean prepareProcessOK() {
		prepareProcess();
		return preparedOK;
	}


}
