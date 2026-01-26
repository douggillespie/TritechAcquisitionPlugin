package tritechplugins.echogram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import tritechgemini.detect.BackgroundSub;
import tritechgemini.echogram.EchoLineDef;
import tritechgemini.echogram.EchoLineStore;
import tritechgemini.echogram.EchogramLine;
import tritechgemini.echogram.EchogramLineMaker;
import tritechgemini.echogram.StandardEchogramLineMaker;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.echogram.fx.EchogramPlotProviderFX;

public class EchogramProcess extends PamProcess{

	private EchogramDataBlock echogramDataBlock;
	private TritechAcquisition tritechAcquisition;
	private ImageDataBlock imageDataBlock;

	private EchogramLineMaker echogramLineMaker;
	private TritechOffline tritechOffline;
	private EchogramPlotProviderFX echogramPlotProvider;

	private BackgroundSub[] backgroundSubtractors = new BackgroundSub[1]; 

	public EchogramProcess(TritechAcquisition tritechAcquisition) {
		super(tritechAcquisition, null, "Echogram Process");
		this.tritechAcquisition = tritechAcquisition;

		createLineMaker();

		echogramDataBlock = new EchogramDataBlock(this);
		addOutputDataBlock(echogramDataBlock);
		echogramPlotProvider = new EchogramPlotProviderFX(this, echogramDataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(echogramPlotProvider);

		imageDataBlock = tritechAcquisition.getImageDataBlock();
		setParentDataBlock(imageDataBlock);

	}

	private void createLineMaker() {
		tritechOffline = tritechAcquisition.getTritechOffline();
		MultiFileCatalog fileCat = null;
		if (tritechOffline != null) {
			fileCat = tritechOffline.getMultiFileCatalog();
		}
		echogramLineMaker = new StandardEchogramLineMaker(fileCat);
	}

	@Override
	public void pamStart() {

	}

	@Override
	public void pamStop() {

	}

	/**
	 * @return the tritechAcquisition
	 */
	public TritechAcquisition getTritechAcquisition() {
		return tritechAcquisition;
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == imageDataBlock) {
			if (echogramDataBlock.countObservers() == 0) {
				//				 don't do anything if nothing is using these data. 
				return;
			}
			newImageData((ImageDataUnit) arg);
		}

	}

	private void newImageData(ImageDataUnit imageDataUnit) {

		int[] sonarIds = tritechAcquisition.getSonarIds();


		// otherwise will need to make as many echolines lines and data units as required
		int nBands = 1; // assume only one band for now
		GeminiImageRecordI image = imageDataUnit.getGeminiImage();
		int nBeam = image.getnBeam();
		int sonarInd = getSonarIndex(image.getDeviceId());
		EchoLineDef[] echoLineDefs = getEchoLineDefs(nBeam);
		int nSequence = echoLineDefs.length * sonarIds.length;
		int chanMap = PamUtils.makeChannelMap(sonarIds.length);
		int sequenceMap = PamUtils.makeChannelMap(nSequence);
		
		
		echogramDataBlock.setChannelMap(chanMap);
		echogramDataBlock.setSequenceMap(sequenceMap);

		BackgroundSub backgroundSub = getBackgroundSubtractor(sonarInd);
		backgroundSub.setTimeConstant(20);

		/**
		 * This all goes wrong because the tempImage gets to store the echogram line
		 * instead of the image record from the datablock. So need to do everythng here
		 */
		//		EchoLineDef es = new EchoLineDef(0, nBeam);
		EchogramLine echoLine = null;
		EchoLineStore elStore = image.getEchoLineStore();
		boolean immediateClear = false; // does this work in normal ? 
		
		for (int iLine = 0; iLine < echoLineDefs.length; iLine++) {
			EchoLineDef es = echoLineDefs[iLine];
			int sequenceIndex = sonarInd * echoLineDefs.length + iLine;
			if (elStore != null) {
				echoLine = elStore.getEchoLine(es);
			}
			if (echoLine == null) {
				// need to do the full loading here, not in the line maker so that
				// we can do the background subtraction
				if (image.isFullyLoaded() == false) {
					if (tritechAcquisition.getTritechOffline() != null) {
						MultiFileCatalog mfc = tritechAcquisition.getTritechOffline().getMultiFileCatalog();
						immediateClear = true;
						GeminiFileCatalog cat = mfc.findRecordCatalog(image);
						if (cat == null) {
							return;
						}
						try {
							cat.loadFullRecord(image);
						} catch (IOException e) {
							return;
						}
					}
				}
				if (image.getImageData() == null) {
					return;
				}
				GeminiImageRecordI tempImage = backgroundSub.removeBackground(image, true);
				echoLine = echogramLineMaker.makeEchogramLine(tempImage, es);
			}
			if (echoLine != null) {
				double maxRange = Math.max(echogramDataBlock.getMaxDataValue(), image.getMaxRange());
				echogramDataBlock.setMaxDataValue(maxRange);
				echogramDataBlock.setDataWidth(sonarInd, echoLine.getData().length);
				echoLine.setGeminiRecord(image);
				if (elStore != null && backgroundSub.getUpdateCount() > 50) {
					/**
					 * Only save if update count is reasonable - by not saving the first
					 * records, they will be recalculated when the display scrolls so will get
					 * a better background sub than they did the first time around.
					 */
					elStore.setEchoLine(echoLine);
					// see if they come back out witha similar linedef
					//					EchogramLine exLine = elStore.getEchoLine(new EchoLineDef(0, bearingTable.length));
					//					System.out.println("Existing echoline " + exLine);
				}
				EchogramDataUnit echoDataUnit = new EchogramDataUnit(echoLine);
				echoDataUnit.setChannelBitmap(1<<sonarInd);
				echoDataUnit.setSequenceBitmap(1<<sequenceIndex);
				echogramDataBlock.addPamData(echoDataUnit);
			}
		}
		if (immediateClear) {
			image.freeImageData();
		}

	}

	private EchoLineDef[] getEchoLineDefs(int nBearing) {
		int nDef = 1;
		EchoLineDef[] defs = new EchoLineDef[nDef];
		int bStep = nBearing / nDef;
		for (int i = 0; i < nDef; i++) {
			int bin1 = i * bStep;
			int bin2 = bin1 + bStep;
			defs[i] = new EchoLineDef(bin1, bin2);
		}
		return defs;
	}

	private int getSonarIndex(int sonarId) {
		int[] sonarIds = tritechAcquisition.getSonarIds();
		for (int i = 0; i < sonarIds.length; i++) {
			if (sonarIds[i] == sonarId) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadInfo) {
		// work through any existing image units and create output from there. 
		echogramDataBlock.clearAll();
		long t1 = offlineLoadInfo.getStartMillis();
		long t2 = offlineLoadInfo.getEndMillis();
		ArrayList<ImageDataUnit> imageData = imageDataBlock.getDataCopy(t1, t2, true);
		for (ImageDataUnit imageUnit : imageData) {
			newImageData(imageUnit);
			if (offlineLoadInfo.cancel) {
				return OfflineDataLoading.REQUEST_INTERRUPTED;
			}
		}
		return OfflineDataLoading.REQUEST_DATA_LOADED;
	}

	private BackgroundSub getBackgroundSubtractor(int sonarIndex) {
		if (sonarIndex >= backgroundSubtractors.length) {
			backgroundSubtractors = Arrays.copyOf(backgroundSubtractors, sonarIndex+1);
		}
		if (backgroundSubtractors[sonarIndex] == null) {
			backgroundSubtractors[sonarIndex] = new BackgroundSub();
		}
		return backgroundSubtractors[sonarIndex];
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		int[] sonarIds = tritechAcquisition.getSonarIds();
		int nSonar = Math.max(sonarIds.length, 1);
		int chanMap = PamUtils.makeChannelMap(nSonar);
		echogramDataBlock.setChannelMap(chanMap);
		float rate = (float) (1000. / params.getManualPingInterval() / nSonar);
		this.setSampleRate(rate, true);
		//		echogramDataBlock.setSampleRate(rate, true);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			prepareProcess();
		}
		if (changeType == PamController.EXTERNAL_DATA_IMPORTED) {
			// called offline when new offline catalog has beenloaded. 
//			System.out.println("Offline catalog complete");
			
		}
		if (changeType == PamController.OFFLINE_DATA_LOADED) {
//			System.out.println("Offline data loaded " + imageDataBlock.getUnitsCount());
			
		}
	}

	/**
	 * Get the apparent sample rate for loaded data. May be a very bad idea !
	 * @return
	 */
	public float getFSforLoad() {
		ImageDataUnit fU = null, lU = null;
		int nUnit = 0;
		synchronized (imageDataBlock.getSynchLock()) {
			nUnit = imageDataBlock.getUnitsCount();
			fU = imageDataBlock.getFirstUnit();
			lU = imageDataBlock.getLastUnit();
		}
		if (nUnit < 2) {
			return getSampleRate();
		}
		float fs = (float) ((float) (nUnit-1) / (float) (lU.getTimeMilliseconds()-fU.getTimeMilliseconds()) * 1000.);
		int[] sonarIds = tritechAcquisition.getSonarIds();
		int nSonar = Math.max(sonarIds.length, 1);
		fs = fs / nSonar;
		return fs;
	}

}
