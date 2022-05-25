package tritechplugins.detect;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamController;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

public class ThresholdDetector extends PamControlledUnit {
	
	private ThresholdProcess thresholdProcess;
	
	private ThresholdParams thresholdParams = new ThresholdParams();

	public static final String unitType = "Gemini Threshold Detector";
	public ThresholdDetector(String unitName) {
		super(unitType, unitName);
		
		thresholdProcess = new ThresholdProcess(this);
		addPamProcess(thresholdProcess);
		
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
		if (isViewer()) {
			JMenu menu = new JMenu(getUnitName());
			JMenuItem menuItem = new JMenuItem("Run offline ...");
			menu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					runOffline(parentFrame);
				}
			});
			return menu;
		}
		return null;
	}

	/**
	 * Run offline processes in viewer mode. 
	 * @param parentFrame
	 */
	protected void runOffline(Frame parentFrame) {
		ThresholdOfflineTask thresholdOfflineTask = new ThresholdOfflineTask(thresholdProcess, thresholdProcess.getSourceDataBlock());
		OfflineTaskGroup taskGroup = new OfflineTaskGroup(this, getUnitName());
		taskGroup.addTask(thresholdOfflineTask);
		OLProcessDialog processDialog = new OLProcessDialog(parentFrame, taskGroup, getUnitType());
		processDialog.setVisible(true);
	}

}
