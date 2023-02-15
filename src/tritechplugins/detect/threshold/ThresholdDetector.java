package tritechplugins.detect.threshold;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import annotation.handler.AnnotationHandler;
import annotation.handler.ManualAnnotationHandler;
import annotation.tasks.AnnotationManager;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import tritechplugins.detect.swing.DetectorHistogramProvider;
import tritechplugins.detect.swing.ThresholdDialog;
import tritechplugins.detect.track.TrackLinkProcess;
import tritechplugins.detect.veto.SpatialVetoManager;
import userDisplay.UserDisplayControl;

public class ThresholdDetector extends PamControlledUnit implements PamSettings {
	
	private ThresholdProcess thresholdProcess;
	
	/**
	 * @return the thresholdProcess
	 */
	public ThresholdProcess getThresholdProcess() {
		return thresholdProcess;
	}
	private ThresholdParams thresholdParams = new ThresholdParams();
	
	private DetectorHistogramProvider histogramProvider;
	
	private ArrayList<ThresholdObserver> thresholdObservers = new ArrayList();

	private TrackLinkProcess trackLinkProcess;
	
	private SpatialVetoManager spatialVetoManager;
	

	/**
	 * @return the trackLinkProcess
	 */
	public TrackLinkProcess getTrackLinkProcess() {
		return trackLinkProcess;
	}
	
	public static final String unitType = "Gemini Threshold Detector";
	
	public ThresholdDetector(String unitName) {
		super(unitType, unitName);
		
		thresholdProcess = new ThresholdProcess(this);
		addPamProcess(thresholdProcess);

		spatialVetoManager = new SpatialVetoManager(this);

		trackLinkProcess = new TrackLinkProcess(this, thresholdProcess);
		addPamProcess(trackLinkProcess);
		
		histogramProvider = new DetectorHistogramProvider(this);
		UserDisplayControl.addUserDisplayProvider(histogramProvider);
		
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			thresholdProcess.prepareProcess();
			trackLinkProcess.prepareProcess();
			break;
		}
		spatialVetoManager.notifyModelChanged(changeType);
	}
	
	/**
	 * @return the thresholdParams
	 */
	public ThresholdParams getThresholdParams() {
		return thresholdParams;
	}
	/**
	 * @param thresholdParams the thresholdParams to set
	 */
	public void setThresholdParams(ThresholdParams thresholdParams) {
		this.thresholdParams = thresholdParams;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem("Settings...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
//		ManualAnnotationHandler annotationHandler = trackLinkProcess.getAnnotationHandler();
//		menuItem = annotationHandler.getDialogMenuItem(parentFrame);
//		if (menuItem != null) {
//			menu.add(menuItem);
//		}
		if (isViewer()) {
			 menuItem = new JMenuItem("Run offline ...");
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runOffline(parentFrame);
				}
			});
			return menu;
		}
		return menu;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		ThresholdParams newParams = ThresholdDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			this.thresholdParams = newParams;
			thresholdProcess.prepareProcess();
			trackLinkProcess.prepareProcess();
			getSpatialVetoManager().makeVetoDataUnits();
//			sortLoggingAddons();
		}
	}
	
//	private void sortLoggingAddons() {
//		trackLinkProcess.sortSQLLogging();
//	}

	/**
	 * Run offline processes in viewer mode. 
	 * @param parentFrame
	 */
	protected void runOffline(Frame parentFrame) {
		ThresholdOfflineTask thresholdOfflineTask = new ThresholdOfflineTask(this, thresholdProcess, thresholdProcess.getImageDataBlock());
		OfflineTaskGroup taskGroup = new OfflineTaskGroup(this, getUnitName());
		taskGroup.addTask(thresholdOfflineTask);
		OLProcessDialog processDialog = new OLProcessDialog(parentFrame, taskGroup, getUnitType());
		processDialog.setVisible(true);
	}

	@Override
	public Serializable getSettingsReference() {
		return thresholdParams;
	}

	@Override
	public long getSettingsVersion() {
		return ThresholdParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		thresholdParams = (ThresholdParams) pamControlledUnitSettings.getSettings();
		if (thresholdParams.backgroundIntervalSecs == 0) {
			thresholdParams.backgroundIntervalSecs = 60;
		}
		return true;
	}

	public void addThresholdObserver(ThresholdObserver thresholdObserver) {
		thresholdObservers.add(thresholdObserver);
	}
	
	public void notifyRawUpdate(int sonarId, byte[] data) {
		for (ThresholdObserver obs : thresholdObservers) {
			obs.newRawData(sonarId, data);
		}
	}
	public void notifyTreatedUpdate(int sonarId, byte[] data) {
		for (ThresholdObserver obs : thresholdObservers) {
			obs.newTreatedData(sonarId, data);
		}
	}

	/**
	 * @return the spatialVetoManager
	 */
	public SpatialVetoManager getSpatialVetoManager() {
		return spatialVetoManager;
	}
}
