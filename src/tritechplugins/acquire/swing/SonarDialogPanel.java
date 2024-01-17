package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import geminisdk.Svs5Exception;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.RangeFrequencyConfig;
import tritechplugins.acquire.SonarDaqParams;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqProcess;
import tritechplugins.acquire.TritechDaqSystem;
import tritechplugins.acquire.TritechJNADaq;

public class SonarDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int sonarId;
//	private DaqDialog daqDialog;
	
	private JPanel mainPanel;
	private ValueSlider gainSlider;
	private ValueSlider rangeSlider;
	private TritechDaqProcess daqProcess;

	private JCheckBox online;

	private JComboBox<String> chirpMode;

	private JComboBox<String> rangeFreq;
	
	private JCheckBox highRangeResolution;
	
	private JCheckBox useFixedSoundSpeed;
	
	private JTextField fixedSoundSpeed;

	public SonarDialogPanel(int sonarId, TritechDaqProcess daqProcess) {
		super();
		this.sonarId = sonarId;
		this.daqProcess = daqProcess;
		
		mainPanel = this;
		makeBorder();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		online = new JCheckBox("Online (use sonar)");

		gainSlider = new ValueSlider("Gain", "%", daqProcess.getGainRange());
		rangeSlider = new ValueSlider("Range", "m", daqProcess.getRangeRange());
		chirpMode = new JComboBox<String>();
		rangeFreq = new JComboBox<String>();
		
//		if (sonarId != 0) {
			c.gridwidth = 2;
			c.gridx = 1;
			mainPanel.add(online, c);
			c.gridy++;
//		}
		c.gridwidth = 1;
		c.gridx = 0;
		mainPanel.add(new JLabel("Gain ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 5;
		mainPanel.add(gainSlider.getComponent(), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Range ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 5;
		mainPanel.add(rangeSlider.getComponent(), c);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy ++;
		mainPanel.add(new JLabel("Chirp mode ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(chirpMode, c);
//		c.gridx = 0;
//		c.gridy++;
		c.gridx+=2;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Frequency mode ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(rangeFreq, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(highRangeResolution = new JCheckBox("Use high range resolution"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		mainPanel.add(useFixedSoundSpeed = new JCheckBox("Use fixed sound speed "), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(fixedSoundSpeed = new JTextField(5), c);

		int[] modes = ChirpMode.getModes();
		for (int i = 0; i < modes.length; i++) {
			chirpMode.addItem(ChirpMode.getModeName(modes[i]));
		}
		
		modes = RangeFrequencyConfig.getModes();
		for (int i = 0; i < modes.length; i++) {
			rangeFreq.addItem(RangeFrequencyConfig.getModeName(modes[i]));
		}

		gainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gainChanged();
			}
		});
		
		useFixedSoundSpeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fixSoundSpeedAction();
			}
		});
		
	}

	protected void fixSoundSpeedAction() {
		fixedSoundSpeed.setEnabled(useFixedSoundSpeed.isSelected());
	}

	private void makeBorder(){
		String borderTitle;
		if (sonarId == TritechDaqParams.DEFAULT_SONAR_PARAMETERSET) {
			borderTitle = "All sonars";
		}
		else {
			borderTitle = "Sonar " + sonarId;
		}
		mainPanel.setBorder(new TitledBorder(borderTitle));
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(TritechDaqParams daqParams) {
		SonarDaqParams sonarParams = daqParams.getSonarParams(sonarId);
		rangeSlider.setValue(sonarParams.getRange());
		gainSlider.setValue(sonarParams.getGain());
		chirpMode.setSelectedIndex(sonarParams.getChirpMode());
		rangeFreq.setSelectedIndex(sonarParams.getRangeConfig());
		highRangeResolution.setSelected(sonarParams.isHighResolution());
		useFixedSoundSpeed.setSelected(sonarParams.isUseFixedSoundSpeed());
		fixedSoundSpeed.setText(String.format("%3.1f", sonarParams.getFixedSoundSpeed()));
		online.setSelected(sonarParams.isSetOnline() || sonarId <= 0);
	}

	public boolean getParams(TritechDaqParams daqParams) {
		SonarDaqParams sonarParams = daqParams.getSonarParams(sonarId);
		
		sonarParams.setRange(rangeSlider.getValue());
		sonarParams.setGain(gainSlider.getValue());
		sonarParams.setChirpMode(chirpMode.getSelectedIndex());
		sonarParams.setRangeConfig(rangeFreq.getSelectedIndex());
		sonarParams.setHighResolution(highRangeResolution.isSelected());
		sonarParams.setUseFixedSoundSpeed(useFixedSoundSpeed.isSelected());
		if (useFixedSoundSpeed.isSelected()) {
			try {
				double fss = Double.valueOf(fixedSoundSpeed.getText());
				sonarParams.setFixedSoundSpeed(fss);
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(null, "Invalid Parameter", "Invalid sound speed value");
			}
		}
		sonarParams.setSetOnline(online.isSelected() || sonarId <= 0);
		
		return true;
	}

	protected void gainChanged() {
		int gain = gainSlider.getValue();
		TritechDaqSystem tritechDaq = daqProcess.getTritechDaqSystem();
		if (tritechDaq instanceof TritechJNADaq == false) {
			return;
		}
		TritechJNADaq jnaDaq = (TritechJNADaq) tritechDaq;
		int id = sonarId == TritechDaqParams.DEFAULT_SONAR_PARAMETERSET ? 0 : sonarId; 
		try {
			jnaDaq.setGain(gain, id);
		} catch (Svs5Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}

	public void setSonarId(int sonarId) {
		this.sonarId = sonarId;
		// and make the border ...
		makeBorder();
		enableControls();
	}

	private void enableControls() {
		online.setEnabled(sonarId > 0);
//		online.setVisible(sonarId > 0);
	}

}
