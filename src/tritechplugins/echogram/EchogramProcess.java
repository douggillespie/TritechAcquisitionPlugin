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
			newImageData((ImageDataUnit) arg);
		}
		
	}

	private void newImageData(ImageDataUnit imageDataUnit) {
//		if (echogramDataBlock.countObservers() == 0) {
			// don't do anything if nothing is using these data. 
//			return;
//		}

		int[] sonarIds = tritechAcquisition.getSonarIds();
		int chanMap = PamUtils.makeChannelMap(sonarIds.length);
		echogramDataBlock.setChannelMap(chanMap);
		
		// otherwise will need to make as many echolines lines and data units as required
		int nBands = 1; // assume only one band for now
		GeminiImageRecordI image = imageDataUnit.getGeminiImage();
		double[] bearingTable = image.getBearingTable();
		if (bearingTable == null) {
			return;
		}
		int sonarInd = getSonarIndex(image.getDeviceId());
		
		BackgroundSub backgroundSub = getBackgroundSubtractor(sonarInd);
		// need to do the full loading here, not in the line maker so that
		// we can do the background subtraction
		boolean immediateClear = false; // does this work in normal ? 
		if (tritechAcquisition.getTritechOffline() != null) {
			MultiFileCatalog mfc = tritechAcquisition.getTritechOffline().getMultiFileCatalog();
			if (image.isFullyLoaded() == false) {
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
		backgroundSub.setTimeConstant(20);
		/**
		 * This all goes wrong because the tempImage gets to store the echogram line
		 * instead of the image record from the datablock. So need to do everythng here
		 */
		EchoLineDef es = new EchoLineDef(0, bearingTable.length);
		EchogramLine echoLine = null;
		EchoLineStore elStore = image.getEchoLineStore();
		if (elStore != null) {
			echoLine = elStore.getEchoLine(es);
		}
		if (echoLine == null) {
			GeminiImageRecordI tempImage = backgroundSub.removeBackground(image, true);
			echoLine = echogramLineMaker.makeEchogramLine(tempImage, es);
			if (echoLine != null) {
				echoLine.setGeminiRecord(image);
				if (elStore != null) {
					elStore.setEchoLine(echoLine);
					// see if they come back out witha similar linedef
//					EchogramLine exLine = elStore.getEchoLine(new EchoLineDef(0, bearingTable.length));
//					System.out.println("Existing echoline " + exLine);
				}
			}
		}
		
//		EchogramLine echoLine = echogramLineMaker.getEchogramLine(tempImage, es);
		if (echoLine != null) {
			EchogramDataUnit echoDataUnit = new EchogramDataUnit(echoLine);
			echoDataUnit.setChannelBitmap(1<<sonarInd);
			echogramDataBlock.setDataWidth(sonarInd, echoLine.getData().length);
			echogramDataBlock.addPamData(echoDataUnit);
		}
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
				break;
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
	}

}
