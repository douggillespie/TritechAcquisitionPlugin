package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import tritechplugins.acquire.TritechDaqParams;

public class TritechOfflineDialog extends PamDialog {

	private static TritechOfflineDialog singleInstance = null;
	private TritechDaqParams daqParams;
	private SelectFolder selectFolder;
	
	private TritechOfflineDialog(Window parentFrame) {
		super(parentFrame, "Tritech Files", false);
		selectFolder = new SelectFolder("Tritech Offline Files", 60, true);
		selectFolder.setCreateIfNeeded(false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, selectFolder.getFolderPanel());
		mainPanel.setBorder(new TitledBorder("Source location for Tritech ecd and glf files"));
		setDialogComponent(mainPanel);
	}
	
	public static TritechDaqParams showDialog(Window parent, TritechDaqParams tritechDaqParams) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new TritechOfflineDialog(parent);
		}
		singleInstance.setParams(tritechDaqParams);
		singleInstance.setVisible(true);
		return singleInstance.daqParams;
	}

	private void setParams(TritechDaqParams tritechDaqParams) {
		this.daqParams = tritechDaqParams;
		selectFolder.setFolderName(daqParams.getOfflineFileFolder());
		selectFolder.setIncludeSubFolders(daqParams.isOfflineSubFolders());
		pack();
	}

	@Override
	public boolean getParams() {
		String folderName = selectFolder.getFolderName(false);
		if (folderName == null) {
			return showWarning("No folder selected");
		}
		File folder = new File(folderName);
		if (folder.exists() == false) {
			return showWarning(String.format("%s does not exist", folderName));
		}
		daqParams.setOfflineFileFolder(folderName);
		daqParams.setOfflineSubFolders(selectFolder.isIncludeSubFolders());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		daqParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
