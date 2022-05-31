package tritechplugins.acquire;

import java.awt.Frame;
import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.OfflineDataStore;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.display.swing.SonarPanelProvider;
import tritechplugins.display.swing.SonarsPanelParams;
import userDisplay.UserDisplayControl;

public class TritechAcquisition extends PamControlledUnit implements PamSettings, OfflineDataStore {

	public static final String unitType = "Tritech Acquisition";
	
	private TritechDaqParams daqParams = new TritechDaqParams();
	
	private TritechOffline tritechOffline;
	
	private ArrayList<ConfigurationObserver> configurationObservers = new ArrayList();
	
	public TritechOffline getTritechOffline() {
		return tritechOffline;
	}

	private TritechRunMode tritechRunMode;
	
	private TritechDaqProcess tritechDaqProcess;
	
	public TritechDaqProcess getTritechDaqProcess() {
		return tritechDaqProcess;
	}

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
		
		UserDisplayControl.addUserDisplayProvider(new SonarPanelProvider(this));
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
			tritechDaqProcess.prepareProcess();
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
		if (tritechOffline != null) {
			tritechOffline.createOfflineDataMap(parentFrame);
		}		
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

}
