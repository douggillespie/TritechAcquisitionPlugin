package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
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
	
	private JCheckBox freePingRate;
	
	private JTextField manualPingInterval;
	
	/**
	 * Automatically catalogue GLF files when acquiring online
	 */
	private JCheckBox autoCatalogue;
	
	private TimeZonePanel timeZonePanel;
	
	private JPanel sonarsPanel;

	private TritechDaqProcess tritechDaqProcess;

	private DaqDialog(Window parentFrame, TritechDaqProcess tritechDaqProcess, String title) {
		super(parentFrame, title, true);
		this.tritechDaqProcess = tritechDaqProcess;
		
		outputFolder = new SelectFolder("Output folder", 40, true);
//		chirpMode = new JComboBox<String>();
//		rangeFreq = new JComboBox<String>();
		freePingRate = new JCheckBox("Free run");
		freePingRate.setToolTipText("Sonar(s) ping as fast as they are able");
		manualPingInterval = new JTextField(4);
		manualPingInterval.setToolTipText("Minimum ping interval milliseconds");
		allTheSame = new JCheckBox("Use same settings on all sonars");
		autoCatalogue = new JCheckBox("Automatically catalogue complete GLF files");
		autoCatalogue.setToolTipText("This will speed up viewing GLF files with the PAMGuard viewer");
		timeZonePanel = new TimeZonePanel();
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
		c.gridwidth = 5;
		mainPanel.add(outputFolder.getFolderPanel(), c);
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy ++;
		for (int i = 0; i < runModes.length; i++) {
			mainPanel.add(runModes[i], c);
			c.gridx+= c.gridwidth;
		}
		c.gridx = 0;
		c.gridy ++;
		c.gridwidth = 5;
		mainPanel.add(timeZonePanel.getComponent(), c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(freePingRate, c);
		c.gridx++;
		mainPanel.add(new JLabel(" Interval", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(manualPingInterval, c);
		c.gridx++;
		mainPanel.add(new JLabel("ms", JLabel.LEFT), c);
		
		
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy++;
		mainPanel.add(autoCatalogue, c);
		c.gridy++;
		mainPanel.add(allTheSame, c);
		
		c.gridwidth = 1;
				
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
		
		freePingRate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});

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
		allTheSame.setSelected(daqParams.isAllTheSame());
		autoCatalogue.setSelected(daqParams.isAutoCatalogue());
		freePingRate.setSelected(daqParams.isManualPingRate() == false);
		manualPingInterval.setText(String.format("%d", daqParams.getManualPingInterval()));
		
		createSonarsPanel();
		
		setSonarsParams(daqParams);
		
		timeZonePanel.setTimeZone(daqParams.getOfflinetimeZoneId());

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
		
		pack();
	}
	
	private void enableControls() {
		
		boolean acquire = runModes[TritechDaqParams.RUN_ACQUIRE].isSelected();
		
		allTheSame.setEnabled(acquire);
		
		outputFolder.setShowSubFolderOption(runModes[TritechDaqParams.RUN_REPROCESS].isSelected());
		
		timeZonePanel.getComponent().setVisible(!acquire);
		
		allTheSame.setVisible(acquire);
		autoCatalogue.setVisible(acquire);
		
		freePingRate.setEnabled(acquire);
		manualPingInterval.setEnabled(acquire & freePingRate.isSelected()==false);
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
			// don't show any at all. 
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
		daqParams.setAllTheSame(allTheSame.isSelected());
		daqParams.setManualPingRate(freePingRate.isSelected() == false);
		try {
			int pingInt = Integer.valueOf(manualPingInterval.getText());
			daqParams.setManualPingInterval(pingInt);
		}
		catch (NumberFormatException ex) {
			if (freePingRate.isSelected()) {
				return showWarning("Invalid ping interval");
			}
		}
		
		int n = sonarsPanel.getComponentCount();
		boolean ok = true;
		for (int i = 0; i < n; i++) {
			SonarDialogPanel sonarPanel = (SonarDialogPanel) sonarsPanel.getComponent(i);
			ok |= sonarPanel.getParams(daqParams);
		}

		String[] zones = TimeZone.getAvailableIDs();
		String selTZ = timeZonePanel.getTimeZoneId();
		daqParams.setOfflinetimeZoneId(selTZ);
		
		if (autoCatalogue.isVisible()) {
			daqParams.setAutoCatalogue(autoCatalogue.isSelected());
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
