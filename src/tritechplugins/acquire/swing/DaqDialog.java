package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqProcess;
import tritechplugins.acquire.TritechDaqSystem;
import tritechplugins.display.swing.SonarsPanel;

public class DaqDialog extends PamDialog {
	
	private static DaqDialog singleInstance = null;
	
	private TritechDaqParams daqParams;
	
	private SelectFolder outputFolder;

//	private JComboBox<String> chirpMode;
//
//	private JComboBox<String> rangeFreq;
//	
	private JRadioButton[] runModes;
	
	private JCheckBox allTheSame;
	
	private JPanel sonarsPanel;

	private TritechDaqProcess tritechDaqProcess;

	private DaqDialog(Window parentFrame, TritechDaqProcess tritechDaqProcess, String title) {
		super(parentFrame, title, true);
		this.tritechDaqProcess = tritechDaqProcess;
		
		outputFolder = new SelectFolder("Output folder", 40, true);
//		chirpMode = new JComboBox<String>();
//		rangeFreq = new JComboBox<String>();
		allTheSame = new JCheckBox("Use same settings on all sonars");
		int[] rModes = TritechDaqParams.getRunModes();
		runModes = new JRadioButton[rModes.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0;  i < rModes.length; i++) {
			bg.add(runModes[i] = new JRadioButton(TritechDaqParams.getRunModeName(rModes[i])));
			runModes[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeAllSame();
				}
			});
		}
		JPanel outerPanel = new JPanel(new BorderLayout());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Tritch Setup"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.add(new JLabel("File Location"), c);
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(outputFolder.getFolderPanel(), c);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy ++;
		for (int i = 0; i < runModes.length; i++) {
			mainPanel.add(runModes[i], c);
			c.gridx++;
		}
		
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy++;
		mainPanel.add(allTheSame, c);
		
		c.gridwidth = 1;
		
//		int[] modes = ChirpMode.getModes();
//		for (int i = 0; i < modes.length; i++) {
//			chirpMode.addItem(ChirpMode.getModeName(modes[i]));
//		}
//		
//		modes = RangeFrequencyConfig.getModes();
//		for (int i = 0; i < modes.length; i++) {
//			rangeFreq.addItem(RangeFrequencyConfig.getModeName(modes[i]));
//		}
		
		outerPanel.add(BorderLayout.NORTH, mainPanel);
		sonarsPanel = new JPanel();
		sonarsPanel.setLayout(new GridBagLayout());
		outerPanel.add(BorderLayout.CENTER, sonarsPanel);
		
		
		setDialogComponent(outerPanel);
		
		allTheSame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeAllSame();
			}
		});
		
//		setResizable(true);
	}

	public static TritechDaqParams showDialog(Window parentFrame, TritechDaqProcess tritechDaqProcess, TritechDaqParams daqParams) {
//		if (singleInstance == null ||singleInstance.getOwner() != parentFrame) {
			singleInstance = new DaqDialog(parentFrame, tritechDaqProcess, "Tritech Acquisition");
//		}
		singleInstance.setParams(daqParams);
		singleInstance.setVisible(true);
		return singleInstance.daqParams;
	}
	
	private void setParams(TritechDaqParams daqParams) {
		this.daqParams = daqParams.clone();
		outputFolder.setFolderName(daqParams.getOfflineFileFolder());
		outputFolder.setIncludeSubFolders(daqParams.isOfflineSubFolders());
		int[] rModes = TritechDaqParams.getRunModes();
		for (int i = 0; i < runModes.length; i++) {
			runModes[i].setSelected(daqParams.getRunMode() == rModes[i]);
		}
//		chirpMode.setSelectedIndex(daqParams.getChirpMode());
//		rangeFreq.setSelectedIndex(daqParams.getRangeConfig());
		allTheSame.setSelected(daqParams.isAllTheSame());
		
		createSonarsPanel();
		
		setSonarsParams(daqParams);

		enableControls();
	}

	/**
	 * Set params for sonars panels. 
	 * @param daqParams
	 */
	private void setSonarsParams(TritechDaqParams daqParams) {
		int n = sonarsPanel.getComponentCount();
		for (int i = 0; i < n; i++) {
			SonarDialogPanel sonarPanel = (SonarDialogPanel) sonarsPanel.getComponent(i);
			sonarPanel.setParams(daqParams);
		}
		
	}

	private void changeAllSame() {
		
		createSonarsPanel();
		
		setSonarsParams(daqParams);
		
		enableControls();
	}
	
	private void enableControls() {
		
		allTheSame.setEnabled(runModes[TritechDaqParams.RUN_ACQUIRE].isSelected());
		
		outputFolder.setShowSubFolderOption(runModes[TritechDaqParams.RUN_REPROCESS].isSelected());
	}

	private void createSonarsPanel() {
		boolean allSame = allTheSame.isSelected();
		int[] toShow = {TritechDaqParams.DEFAULT_SONAR_PARAMETERSET};
		if (allSame == false) {
			TritechDaqSystem daqSystem = tritechDaqProcess.getTritechDaqSystem();
			if (daqSystem != null) {
//				int n = daqSystem.getNumSonars();
//				daqSystem.
				toShow = daqSystem.getSonarIds();
//				for (int i = 0; i < n; i++) {
//					SonarStatusData statusData = daqSystem.getSonarStatusData(i);
//					int sonarId = statusData.getDeviceId();
//					toShow[i] = sonarId;
//				}
			}
		}
		
		if (runModes[TritechDaqParams.RUN_REPROCESS].isSelected()) {
			toShow = new int[0];
		}
		
		boolean changeLayout = false;
		while (toShow.length < sonarsPanel.getComponentCount()) {
			sonarsPanel.remove(0);
			changeLayout = true;
		}
		sonarsPanel.setLayout(new BoxLayout(sonarsPanel, BoxLayout.Y_AXIS));
		int nComp = sonarsPanel.getComponentCount();
		while (toShow.length > sonarsPanel.getComponentCount()) {
			sonarsPanel.add(new SonarDialogPanel(0, tritechDaqProcess));
		}
		for (int i = 0; i < toShow.length; i++) {
			SonarDialogPanel sonarPanel = (SonarDialogPanel) sonarsPanel.getComponent(i);
			sonarPanel.setSonarId(toShow[i]);
			changeLayout = true;
		}

		if (changeLayout) {
			sonarsPanel.invalidate();
			pack();
		}
	}
	
	private boolean wantSonarPanel(int[] toShow, int sonarId) {
		if (toShow == null) {
			return false;
		}
		for (int i = 0; i < toShow.length; i++) {
			if (toShow[i] == sonarId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean getParams() {
		int[] rModes = TritechDaqParams.getRunModes();
		for (int i = 0; i < runModes.length; i++) {
			if (runModes[i].isSelected()) {
				daqParams.setRunMode(rModes[i]);
				break;
			}
		}
		daqParams.setOfflineFileFolder(outputFolder.getFolderName(daqParams.getRunMode() == TritechDaqParams.RUN_ACQUIRE));
		daqParams.setOfflineSubFolders(outputFolder.isIncludeSubFolders());
//		daqParams.setChirpMode(chirpMode.getSelectedIndex());
//		daqParams.setRangeConfig(rangeFreq.getSelectedIndex());
		daqParams.setAllTheSame(allTheSame.isSelected());
		int n = sonarsPanel.getComponentCount();
		boolean ok = true;
		for (int i = 0; i < n; i++) {
			SonarDialogPanel sonarPanel = (SonarDialogPanel) sonarsPanel.getComponent(i);
			ok |= sonarPanel.getParams(daqParams);
		}
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		daqParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new TritechDaqParams());
	}

}
