package tritechplugins.detect;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import tritechplugins.detect.swing.ThresholdDialog;

public class ThresholdDetector extends PamControlledUnit implements PamSettings {
	
	private ThresholdProcess thresholdProcess;
	
	private ThresholdParams thresholdParams = new ThresholdParams();

	public static final String unitType = "Gemini Threshold Detector";
	public ThresholdDetector(String unitName) {
		super(unitType, unitName);
		
		thresholdProcess = new ThresholdProcess(this);
		addPamProcess(thresholdProcess);
		
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			thresholdProcess.prepareProcess();
			break;
		}
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
		}
	}

	/**
	 * Run offline processes in viewer mode. 
	 * @param parentFrame
	 */
	protected void runOffline(Frame parentFrame) {
		ThresholdOfflineTask thresholdOfflineTask = new ThresholdOfflineTask(this, thresholdProcess, thresholdProcess.getSourceDataBlock());
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
		return true;
	}

}
