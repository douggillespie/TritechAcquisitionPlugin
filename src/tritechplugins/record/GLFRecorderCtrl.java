package tritechplugins.record;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSidePanel;
import PamView.dialog.warn.WarnOnce;
import tritechplugins.record.swing.GLFRecorderDialog;
import tritechplugins.record.swing.GLFRecorderSidePanel;

/**
 * PAMGuard module to provide GLF recording. Will be similar to the PAMGuard sound recorder
 * in that it can manage a buffer of incoming data, so record retrospectively. Will need
 * some very careful thread management in order to write content of buffer while at the 
 * same time receiving new frames to write to the data since the time taken to write the
 * buffer may be quite large ! 
 * @author dg50
 *
 */
public class GLFRecorderCtrl extends PamControlledUnit implements PamSettings {
	
	public static final String unitType = "GLF Recorder";
	public static final String unitName = "GLF Recorder";
	
	private GLFRecorderProcess recorderProcess;
	
	private GLFRecorderParams recorderParams = new GLFRecorderParams();
	
	private ArrayList<StateObserver> stateObservers = new ArrayList();
	private GLFRecorderSidePanel glfSidePanel; 
	
	public static final String DATASELECTNAME = "GLF Recorder";

	public GLFRecorderCtrl(PamConfiguration pamConfiguration, String unitName) {
		super(pamConfiguration, unitType, unitName);
		
		recorderProcess = new GLFRecorderProcess(this);
		addPamProcess(recorderProcess);

		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return recorderParams;
	}

	@Override
	public long getSettingsVersion() {
		return GLFRecorderParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		recorderParams = (GLFRecorderParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	public GLFRecorderParams getRecorderParams() {
		return recorderParams;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsMenu(parentFrame);
			}
		});
		return menuItem;
	}

	protected void settingsMenu(Frame parentFrame) {
		GLFRecorderParams newSettings = GLFRecorderDialog.showDialog(parentFrame, this, recorderParams);
		if (newSettings != null) {
			recorderParams = newSettings;
			recorderProcess.prepareProcess();
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			recorderProcess.prepareProcess();
			break;
		}
	}
	
	/**
	 * Check the output folder. 
	 * @param auto auto create without asking, otherwise ask first. 
	 * @return true if folder exists or is successfully created. 
	 */
	public boolean checkOutputFolder(boolean auto) {
		String path = recorderParams.outputFolder;
		if (path == null) {
			return false;
		}
		File oPath = new File(path);
		if (oPath.exists() && oPath.isDirectory()) {
			return true;
		}
		// doesn't exist
		if (auto == false) {
			int ans = WarnOnce.showWarning("GLF Output folder", "Create folder " + path + " for GLF output?", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		boolean ok = oPath.mkdirs();
		return ok;
	}
	
	public void addStateObserver(StateObserver stateObserver) {
		stateObservers.add(stateObserver);
	}
	
	public void removeStateObserver(StateObserver stateObserver) {
		stateObservers.remove(stateObserver);
	}

	public void notifiyStateObservers(int state) {
		for (StateObserver so : stateObservers) {
			so.notify(state);
		}
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (glfSidePanel == null) {
			glfSidePanel = new GLFRecorderSidePanel(this);
		}
		return glfSidePanel;
	}

	/**
	 * @return the recorderProcess
	 */
	public GLFRecorderProcess getRecorderProcess() {
		return recorderProcess;
	}
}
