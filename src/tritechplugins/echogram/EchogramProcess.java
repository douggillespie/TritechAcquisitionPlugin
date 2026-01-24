package tritechplugins.echogram;

import java.util.ArrayList;

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
import tritechgemini.echogram.EchoLineDef;
import tritechgemini.echogram.EchogramLine;
import tritechgemini.echogram.EchogramLineMaker;
import tritechgemini.echogram.StandardEchogramLineMaker;
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
		int sonarInd = getSonarIndex(image.getDeviceId());
		EchoLineDef es = new EchoLineDef(0, bearingTable.length);
		EchogramLine echoLine = echogramLineMaker.getEchogramLine(image, es);
		if (echoLine != null) {
			EchogramDataUnit echoDataUnit = new EchogramDataUnit(echoLine);
			echoDataUnit.setChannelBitmap(1<<sonarInd);
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
		}
		return OfflineDataLoading.REQUEST_DATA_LOADED;
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
		echogramDataBlock.setSampleRate(rate, true);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			prepareProcess();
		}
	}

}
