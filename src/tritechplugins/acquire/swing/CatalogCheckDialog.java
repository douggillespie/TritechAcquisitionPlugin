package tritechplugins.acquire.swing;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import PamUtils.FileList;
import PamUtils.SelectFolder;
import PamView.CancelObserver;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tritechgemini.fileio.GLFCatalogCheck;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.offline.TritechOffline;

public class CatalogCheckDialog extends PamDialog implements CancelObserver {
	
	private SelectFolder selectFolder;
	private JProgressBar progressBar;
	private JLabel infoText;
	private JTextField totalFiles, addedFast, addedMain;
	private TritechOffline tritechOffline;
	
	private volatile boolean keepRunning;

	public CatalogCheckDialog(Window parentFrame, TritechOffline tritechOffline) {
		super(parentFrame, "Check Gemini file catalogues", false);
		this.tritechOffline = tritechOffline;
		
		selectFolder = new SelectFolder("Root folder", 60, true);
		progressBar = new JProgressBar();
		totalFiles = new JTextField(8);
		totalFiles.setEditable(false);
		addedFast = new JTextField(8);
		addedFast.setEditable(false);
		addedMain = new JTextField(8);
		addedMain.setEditable(false);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		mainPanel.add(selectFolder.getFolderPanel(), c);
		
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 2;
		mainPanel.add(infoText = new JLabel(" Select folder and hit OK to catalogue files "), c);
		
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy ++;
		mainPanel.add(new JLabel("Progress:", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(progressBar, c);
		
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Total files: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(totalFiles, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Added fast catalogues: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(addedFast, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Added main catalogues: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(addedMain, c);
		
		enableButtongs(false);
//		getOkButton().setText("Start");
		setDialogComponent(mainPanel);
		setCancelObserver(this);
	}

	public static void showDialog(Frame parentFrame, TritechOffline tritechOffline) {
		CatalogCheckDialog checkDialog = new CatalogCheckDialog(parentFrame, tritechOffline);
		checkDialog.setParams();
		checkDialog.pack();
		checkDialog.setVisible(true);
	}

	private void setParams() {
		TritechAcquisition tritechAcquisition = tritechOffline.getTritechAcquisition();
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		selectFolder.setFolderName(params.getOfflineFileFolder());
		selectFolder.setIncludeSubFolders(params.isOfflineSubFolders());
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void okButtonPressed() {
		/*
		 * Start worker thread to go through data and send updates back 
		 * to AWT for display. 
		 */
		TritechDaqParams daqParams = tritechOffline.getTritechAcquisition().getDaqParams();
		GeminiFileCatalog.setTimeZone(daqParams.getOfflineTimeZone());
		String path = selectFolder.getFolderName(false);
		boolean subs = selectFolder.isIncludeSubFolders();
		File folder = new File(path);
		if (folder.exists() == false) {
			showWarning("Folder does not exist: " + path);
			return;
		}
		enableButtongs(true);
		CatWorker catWorker = new CatWorker(folder, subs);
		catWorker.execute();
	}

	@Override
	public void cancelButtonPressed() {

	}
	
	@Override
	public boolean cancelPressed() {
		if (keepRunning) {
			keepRunning = false;
			return false;
		}
		else {
			return true;
		}
	}

	private void enableButtongs(boolean running) {
		getOkButton().setEnabled(running == false);
//		getCancelButton().setEnabled(running);
		getCancelButton().setText(running ? "Stop!" : "Close");
	}

	protected void setProgress(CatalogCheckUpdate catalogCheckUpdate) {
		totalFiles.setText(String.format("%d/%d", catalogCheckUpdate.processedFiles, catalogCheckUpdate.totalFiles));
		addedFast.setText(String.format("%d", catalogCheckUpdate.addedFast));
		addedMain.setText(String.format("%d", catalogCheckUpdate.addedMain));
		progressBar.setMaximum(catalogCheckUpdate.totalFiles);
		progressBar.setValue(catalogCheckUpdate.processedFiles);
		infoText.setText(catalogCheckUpdate.fileName);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class CatWorker extends SwingWorker<Integer, CatalogCheckUpdate> {

		private File folder;
		private boolean subs;

		public CatWorker(File folder, boolean subs) {
			this.folder = folder;
			this.subs = subs;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			int newFast = 0, newMain = 0;
			keepRunning = true;
			try {
				GLFCatalogCheck catalogCheck = new GLFCatalogCheck();
				String[] ends = {GeminiFileCatalog.GLFEND, GeminiFileCatalog.ECDEND, GeminiFileCatalog.DATEND};
				FileList fileList = new FileList();
				ArrayList<File> files = fileList.getFileList(folder.getAbsolutePath(), ends, subs);
				int nFiles = files.size();
				publish(new CatalogCheckUpdate(nFiles, 0, 0, 0, " "));
				for (int i = 0; i < nFiles; i++) {
					int res = catalogCheck.checkCatalogues(files.get(i).getAbsolutePath());
					if ((res & GLFCatalogCheck.ADD_GLFFASTCATALOG) > 0) {
						newFast++;
					}
					if ((res & GLFCatalogCheck.ADD_DATFILECATALOG) > 0) {
						newMain++;
					}
					publish(new CatalogCheckUpdate(nFiles, i+1, newFast, newMain, files.get(i).getName()));
					if (!keepRunning) {
						publish(new CatalogCheckUpdate(nFiles, i+1, newFast, newMain, "User stop!"));
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			return 0;
		}

		@Override
		protected void process(List<CatalogCheckUpdate> chunks) {
			CatalogCheckDialog.this.setProgress(chunks.get(chunks.size()-1));
		}

		@Override
		protected void done() {
			enableButtongs(false);
			keepRunning = false;
		}
		
	}

}
