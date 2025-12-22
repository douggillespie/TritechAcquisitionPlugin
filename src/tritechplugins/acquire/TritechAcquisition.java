package tritechplugins.acquire;

import java.awt.Frame;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import javax.swing.JMenuItem;

import PamController.DataInputStore;
import PamController.InputStoreInfo;
import PamController.OfflineDataStore;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.RawInputControlledUnit;
import PamUtils.worker.PamWorkMonitor;
import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import backupmanager.BackupInformation;
import binaryFileStorage.BinaryStore;
import dataGram.DatagramManager;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechplugins.acquire.backup.GLFBackup;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.acquire.swing.SonarPositionDialog;
import tritechplugins.acquire.swing.TritechSidePanel;
import tritechplugins.acquire.swing.framerate.FrameRateDisplayProvider;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.track.TrackLinkDataBlock;
import tritechplugins.detect.track.TrackLinkProcess;
import tritechplugins.display.swing.SonarPanelProvider;
import tritechplugins.display.swing.SonarsPanelParams;
import tritechplugins.mark.SonarMarker;
import userDisplay.UserDisplayControl;

public class TritechAcquisition extends RawInputControlledUnit implements PamSettings, OfflineDataStore, DataInputStore {

	public static final String unitType = "Tritech Acquisition";
	
	private TritechDaqParams daqParams = new TritechDaqParams();
	
	private TritechOffline tritechOffline;
	
	private ArrayList<ConfigurationObserver> configurationObservers = new ArrayList();
	
	private TritechRunMode tritechRunMode;
	
	private TritechDaqProcess tritechDaqProcess;
	
	private BackupInformation backupInformation;
	
	private SonarMarker sonarMarker;
	
	private TritechSidePanel daqSidePanel;
	
	public TritechAcquisition(String unitName) {
		super(unitType, unitName);
		PamSettingManager.getInstance().registerSettings(this);
		
		tritechDaqProcess = new TritechDaqProcess(this);
		addPamProcess(tritechDaqProcess);
		
		if (isViewer()) {
			tritechRunMode = tritechOffline = new TritechOffline(this);
		}
		else {
			tritechRunMode = tritechDaqProcess;
		}
		
		backupInformation = new BackupInformation(new GLFBackup(this));
		
		sonarMarker = new SonarMarker(this);
		
		UserDisplayControl.addUserDisplayProvider(new SonarPanelProvider(this));
		UserDisplayControl.addUserDisplayProvider(new FrameRateDisplayProvider(this));
	}

	public TritechDaqProcess getTritechDaqProcess() {
		return tritechDaqProcess;
	}

	public TritechOffline getTritechOffline() {
		return tritechOffline;
	}

	@Override
	public void pamClose() {
		super.pamClose();
		tritechDaqProcess.pamClose();
	}

	public TritechDaqParams getDaqParams() {
		return daqParams;
	}

	public void setDaqParams(TritechDaqParams daqParams) {
		this.daqParams = daqParams;
		GeminiFileCatalog.setTimeZone(null);
	}

	public void configurationChanged() {
		notifyConfigurationObservers();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (tritechOffline != null) {
			tritechOffline.notifyModelChanged(changeType);
		}
		if (tritechDaqProcess != null && isViewer() == false) {
			if (changeType == PamController.INITIALIZATION_COMPLETE) {
				tritechDaqProcess.prepareProcess();
			}
		}
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		if (isViewer()) {
			return tritechOffline.createViewerMenu(parentFrame);
		}
		else {
			return tritechDaqProcess.createDaqMenu(parentFrame);
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return daqParams;
	}

	@Override
	public long getSettingsVersion() {
		return TritechDaqParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		daqParams = (TritechDaqParams) pamControlledUnitSettings.getSettings();
		setDaqParams(daqParams);
		return true;
	}

	/**
	 * @return the tritechRunMode
	 */
	public TritechRunMode getTritechRunMode() {
		return tritechRunMode;
	}

	@Override
	public void createOfflineDataMap(Window parentFrame) {
//		if (tritechOffline != null) {
//			tritechOffline.createOfflineDataMap(parentFrame);
//		}		
	}
	
	/**
	 * Get the image datablock from the process. 
	 * @return image datablock
	 */
	public ImageDataBlock getImageDataBlock() {
		return tritechDaqProcess.getImageDataBlock();
	}

	@Override
	public String getDataSourceName() {
		if (tritechOffline != null) {
			return tritechOffline.getDataSourceName();
		}
		else {
			return tritechDaqProcess.getImageDataBlock().getDataName();
		}
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		if (tritechOffline != null) {
			return tritechOffline.loadData(dataBlock, offlineDataLoadInfo, loadObserver);
		}
		return false;
	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		if (tritechOffline != null) {
			return tritechOffline.saveData(dataBlock);
		}
		return false;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp) {
		if (tritechOffline != null) {
			return tritechOffline.rewriteIndexFile(dataBlock, dmp);
		}
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		if (tritechOffline != null) {
			return tritechOffline.getDatagramManager();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Add observer to get notifications of major configuration changes. 
	 * @param configObserver
	 */
	public void addConfigurationObserver(ConfigurationObserver configObserver) {
		configurationObservers.add(configObserver);
	}
	/**
	 * Remove observer getting notifications of major configuration changes. 
	 * @param configObserver
	 */
	public void removeConfigurationObserver(ConfigurationObserver configObserver) {
		configurationObservers.remove(configObserver);
	}
	
	public void notifyConfigurationObservers() {
		for (ConfigurationObserver configObs : configurationObservers) {
			configObs.configurationChanged();
		}
	}

	@Override
	public BackupInformation getBackupInformation() {
		return backupInformation;
	}

	@Override
	public String getDataLocation() {
		return daqParams.getOfflineFileFolder();
	}

	@Override
	public boolean setAnalysisStartTime(long startTime) {
		return tritechDaqProcess.setAnalysisStartTime(startTime);
	}

	@Override
	public String getBatchStatus() {
		return tritechDaqProcess.getBatchStatus();
	}

	/**
	 * @return the sonarMarker
	 */
	public SonarMarker getSonarMarker() {
		return sonarMarker;
	}

	@Override
	public int getRawInputType() {
		switch (daqParams.getRunMode()) {
		case TritechDaqParams.RUN_ACQUIRE:
			return RawInputControlledUnit.RAW_INPUT_REALTIME;
		case TritechDaqParams.RUN_REPROCESS:
			return RawInputControlledUnit.RAW_INPUT_FILEARCHIVE;
		case TritechDaqParams.RUN_SIMULATE:
			return RawInputControlledUnit.RAW_INPUT_UNKNOWN;
		}
		return RawInputControlledUnit.RAW_INPUT_UNKNOWN;
	}

	@Override
	public InputStoreInfo getStoreInfo(PamWorkMonitor workerMonitor, boolean detail) {
		// TODO Auto-generated method stub
		return tritechDaqProcess.getStoreInfo(workerMonitor, detail);
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (daqSidePanel == null) {
			daqSidePanel = new TritechSidePanel(this);
		}
		return daqSidePanel;
	}
	
	public int[] getSonarIds() {
		if (isViewer()) {
			/*
			 *  need to see what's in the data map of both the binary and the GLF data and
			 *  take the union of the two.  
			 */
			int[] offlineIds = null;
			if (tritechOffline != null) {
				// might only work if GLF files are present. 
				offlineIds = tritechOffline.getSonarIDs();
			}
			int[] pIds = daqParams.getSonarIds();
			int[] ids = combineIntArrays(offlineIds, pIds);
//			int[] binaryIds = null;
//			ThresholdDetector threshDet = (ThresholdDetector) getPamConfiguration().findControlledUnit(ThresholdDetector.unitType);
//			BinaryStore binaryStore = (BinaryStore) getPamConfiguration().findControlledUnit(BinaryStore.defUnitType);
//			if (threshDet != null && binaryStore != null) {
//				TrackLinkProcess linkProcess = threshDet.getTrackLinkProcess();
//				TrackLinkDataBlock linkDataBlock = linkProcess.getTrackLinkDataBlock();
//				OfflineDataMap dataMap = linkDataBlock.getPrimaryDataMap();
//				dataMap.getAllStartsAndEnds();
//			}
			return ids;
		}
		else {
			return getTritechDaqProcess().getTritechDaqSystem().getSonarIds();
		}
	}

	private int[] combineIntArrays(int[] l1, int[] l2) {
		if (l1 == null) {
			return l2;
		}
		if (l2 == null) {
			return l1;
		}
		HashSet<Integer> allK = new HashSet<>();
		for (int i = 0; i < l1.length; i++) {
			allK.add(l1[i]);
		}
		for (int i = 0; i < l2.length; i++) {
			allK.add(l2[i]);
		}
		int[] cl = new int[allK.size()];
		int n = 0;
		for (Integer ik : allK) {
			cl[n++] = ik;
		}
		Arrays.sort(cl);
		return cl;
	}

	public void showGeometryDialog(Frame parentFrame) {
		TritechDaqParams params = SonarPositionDialog.showDialog(parentFrame, this);
		if (params != null) {
			this.setDaqParams(params);
//			notifyConfigurationObservers(); // calling this seems to mess the glf catalogue 
		}
	}

//	/**
//	 * Get a set of current sonar id's being used in this configuration. This may be
//	 * because they appear in a GLF catalog, or in the binary data map. 
//	 * @return the currentSonarIds
//	 */
//	public Set<Integer> getCurrentSonarIds() {
//		return currentSonarIds;
//	}
//	
//	/**
//	 * Add a sonar id to the map of sonar id's. 
//	 * @param sonarId
//	 */
//	public void addSonarId(int sonarId) {
//		currentSonarIds.add(sonarId);
//	}
//	
//	/**
//	 * Rebuild the map of sonar id's. 
//	 */
//	public void rebuildSonarIds() {
//		currentSonarIds.clear();
//		TritechDaqSystem daqSystem = tritechDaqProcess.getTritechDaqSystem();
//		if (daqSystem != null) {
//			int[] sonarIds = daqSystem.getSonarIds();
//			for (int i = 0; i < sonarIds.length; i++) {
//				addSonarId(sonarIds[i]);
//			}
//		}
//	}

}
