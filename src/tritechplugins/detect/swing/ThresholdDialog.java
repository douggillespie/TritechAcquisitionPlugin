package tritechplugins.detect.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import tritechgemini.detect.RegionDetector;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.detect.RegionDataUnit;
import tritechplugins.detect.ThresholdDetector;
import tritechplugins.detect.ThresholdParams;

public class ThresholdDialog extends PamDialog {

	private static ThresholdDialog singleInstance;

	private ThresholdParams thresholdParams;

	private ThresholdDetector thresholdDetector;

	private SourcePanel sourcePanel;

	private JTextField thresholdOn, thresholdOff;

	private JTextField backgroundTime, backgroundScale;

	private JComboBox<String> connectionType;


	private ThresholdDialog(Window parentFrame, ThresholdDetector thresholdDetector) {
		super(parentFrame, thresholdDetector.getUnitName() + " config ...", true);
		this.thresholdDetector = thresholdDetector;

		sourcePanel = new SourcePanel(this, ImageDataUnit.class, false, true);
		thresholdOn = new JTextField(3);
		thresholdOff = new JTextField(3);
		backgroundTime = new JTextField(3);
		backgroundScale = new JTextField(3);
		connectionType = new JComboBox<String>();

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Threshold detector"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		mainPanel.add(new JLabel("Data source", JLabel.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(sourcePanel.getPanel(), c);
		c.gridwidth = 1;
		
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("Background time scale ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(backgroundTime, c);
		c.gridx++;
		mainPanel.add(new JLabel(" frames ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("Background removal scale ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(backgroundScale, c);
		c.gridx++;
		mainPanel.add(new JLabel(" multiplier ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel(" Detection on threshold ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(thresholdOn, c);
		c.gridx++;
		mainPanel.add(new JLabel(" counts ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel(" Detection off threshold ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(thresholdOff, c);
		c.gridx++;
		mainPanel.add(new JLabel(" counts ", JLabel.LEFT), c);


		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel(" Connection type ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(connectionType, c);

		int[] conTypes = RegionDetector.getConnectionTypes();
		for (int i = 0; i < conTypes.length; i++) {
			connectionType.addItem(String.format("Connect %d", conTypes[i]));
		}

		setDialogComponent(mainPanel);
	}

	public static ThresholdParams showDialog(Window parentFrame, ThresholdDetector thresholdDetector) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.thresholdDetector != thresholdDetector) {
			singleInstance = new ThresholdDialog(parentFrame, thresholdDetector);
		}
		singleInstance.setParams(thresholdDetector.getThresholdParams());
		singleInstance.setVisible(true);
		return singleInstance.thresholdParams;
	}

	private void setParams(ThresholdParams thresholdParams) {
		this.thresholdParams = thresholdParams;
		sourcePanel.setSource(thresholdParams.imageDataSource);
		backgroundTime.setText(String.format("%d", thresholdParams.backgroundTimeConst));
		backgroundScale.setText(String.format("%3.2f", thresholdParams.backgroundScale));
		thresholdOn.setText(String.format("%d", thresholdParams.highThreshold));
		thresholdOff.setText(String.format("%d", thresholdParams.lowThreshold));

		int[] conTypes = RegionDetector.getConnectionTypes();
		for (int i = 0; i < conTypes.length; i++) {
			if (thresholdParams.connectionType == conTypes[i]) {
				connectionType.setSelectedIndex(i);
			}
		}
	}

	@Override
	public boolean getParams() {
		PamDataBlock imSource =  sourcePanel.getSource();
		if (imSource == null) {
			return showWarning("No selected data source");
		}
		thresholdParams.imageDataSource = imSource.getLongDataName();
		try {
			thresholdParams.backgroundTimeConst = Integer.valueOf(backgroundTime.getText());
			thresholdParams.highThreshold = Integer.valueOf(thresholdOn.getText());
			thresholdParams.lowThreshold = Integer.valueOf(thresholdOff.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Threshold and time constant parameters must be integer");
		}
		try {
			thresholdParams.backgroundScale = Double.valueOf(backgroundScale.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid scale value");
		}

		return true;
	}

	@Override
	public void cancelButtonPressed() {
		thresholdParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new ThresholdParams());
	}

}
