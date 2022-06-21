package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
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
import tritechplugins.acquire.TritechDaqParams;

public class DaqDialog extends PamDialog {
	
	private static DaqDialog singleInstance = null;
	
	private TritechDaqParams daqParams;
	
	private SelectFolder outputFolder;

	private JComboBox<String> chirpMode;

	private JComboBox<String> rangeFreq;
	
	private JRadioButton[] runModes;

	private DaqDialog(Window parentFrame, String title) {
		super(parentFrame, title, true);
		
		outputFolder = new SelectFolder("Output folder", 40, true);
		chirpMode = new JComboBox<String>();
		rangeFreq = new JComboBox<String>();
		int[] rModes = TritechDaqParams.getRunModes();
		runModes = new JRadioButton[rModes.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0;  i < rModes.length; i++) {
			bg.add(runModes[i] = new JRadioButton(TritechDaqParams.getRunModeName(rModes[i])));
		}
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Tritch Setup"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.add(new JLabel("File Location"), c);
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(outputFolder.getFolderPanel(), c);
//		JPanel bitsPanel = new JPanel(new GridBagLayout());
//		mainPanel.add(bitsPanel, BorderLayout.CENTER);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy ++;
		for (int i = 0; i < runModes.length; i++) {
			mainPanel.add(runModes[i], c);
			c.gridx++;
		}
		
		c.gridx = 0;
		c.gridy ++;
		mainPanel.add(new JLabel("Chirp mode ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(chirpMode, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Range mode ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(rangeFreq, c);
		
		int[] modes = ChirpMode.getModes();
		for (int i = 0; i < modes.length; i++) {
			chirpMode.addItem(ChirpMode.getModeName(modes[i]));
		}
		
		modes = RangeFrequencyConfig.getModes();
		for (int i = 0; i < modes.length; i++) {
			rangeFreq.addItem(RangeFrequencyConfig.getModeName(modes[i]));
		}
		
		setDialogComponent(mainPanel);
	}

	public static TritechDaqParams showDialog(Window parentFrame, TritechDaqParams daqParams) {
//		if (singleInstance == null ||singleInstance.getOwner() != parentFrame) {
			singleInstance = new DaqDialog(parentFrame, "Tritech Acquisition");
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
		chirpMode.setSelectedIndex(daqParams.getChirpMode());
		rangeFreq.setSelectedIndex(daqParams.getRangeConfig());
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
		daqParams.setChirpMode(chirpMode.getSelectedIndex());
		daqParams.setRangeConfig(rangeFreq.getSelectedIndex());
		return true;
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
