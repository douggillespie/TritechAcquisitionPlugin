package tritechplugins.detect.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import tritechgemini.detect.RegionDetector;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.threshold.ThresholdParams;
import tritechplugins.detect.track.TrackLinkParameters;
import tritechplugins.detect.track.TrackLinkProcess;

public class ThresholdDialog extends PamDialog {

	private static ThresholdDialog singleInstance;

	private ThresholdParams thresholdParams;
	
	private TrackLinkParameters trackLinkParameters;

	private ThresholdDetector thresholdDetector;
	
	private SourcePanel sourcePanel;

	private JTextField thresholdOn, thresholdOff;

	private JTextField backgroundTime, backgroundScale;
	
	private JTextField backgroundRecordS;
	
	private JTextField minObjectSize, maxObjectSize;

	private JComboBox<String> connectionType;
	
	// fields for tracker
	private JTextField maxTimeStepS;
	
	private JTextField maxSpeed;
	
	private JTextField minTrackPoints;
	
	private JTextField maxSizeRatio;
	
	private JTextField minLength, minStraightLength;
	
	private PamDialogPanel vetoPanel;


	private ThresholdDialog(Window parentFrame, ThresholdDetector thresholdDetector) {
		super(parentFrame, thresholdDetector.getUnitName() + " config ...", true);
		this.thresholdDetector = thresholdDetector;

		sourcePanel = new SourcePanel(this, ImageDataUnit.class, false, true);
		thresholdOn = new JTextField(3);
		thresholdOff = new JTextField(3);
		backgroundTime = new JTextField(3);
		backgroundScale = new JTextField(3);
		backgroundRecordS = new JTextField(3);
		minObjectSize = new JTextField(3);
		maxObjectSize = new JTextField(3);
		connectionType = new JComboBox<String>();

		JPanel thresholdPanel = new JPanel(new GridBagLayout());
		thresholdPanel.setBorder(new TitledBorder("Threshold detector"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 5;
		thresholdPanel.add(new JLabel("Data source", JLabel.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		thresholdPanel.add(sourcePanel.getPanel(), c);
		c.gridwidth = 1;
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel("Background time scale ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		thresholdPanel.add(backgroundTime, c);
		c.gridx+=c.gridwidth;
		thresholdPanel.add(new JLabel(" frames ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel("Background removal scale ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		thresholdPanel.add(backgroundScale, c);
		c.gridx+=c.gridwidth;
		thresholdPanel.add(new JLabel(" multiplier ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel("Record background every ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		thresholdPanel.add(backgroundRecordS, c);
		c.gridx+=c.gridwidth;
		thresholdPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel(" Detection on threshold ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		thresholdPanel.add(thresholdOn, c);
		c.gridx+=c.gridwidth;
		thresholdPanel.add(new JLabel(" counts ", JLabel.LEFT), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel(" Detection off threshold ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		thresholdPanel.add(thresholdOff, c);
		c.gridx+=c.gridwidth;
//		c.gridwidth = 1;
		thresholdPanel.add(new JLabel(" counts ", JLabel.LEFT), c);


		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel(" Connection type ", JLabel.RIGHT), c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 3;
		thresholdPanel.add(connectionType, c);
		
		int[] conTypes = RegionDetector.getConnectionTypes();
		for (int i = 0; i < conTypes.length; i++) {
			connectionType.addItem(String.format("Connect %d", conTypes[i]));
		}
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		thresholdPanel.add(new JLabel("Size range ", JLabel.RIGHT), c);
		c.gridx++;
		thresholdPanel.add(minObjectSize, c);
		c.gridx++;
		thresholdPanel.add(new JLabel(" to ", JLabel.CENTER), c);
		c.gridx++;
		thresholdPanel.add(maxObjectSize, c);
		c.gridx++;
		thresholdPanel.add(new JLabel(" m ", JLabel.LEFT), c);
		
		JPanel trackPanel = new JPanel(new GridBagLayout());
		trackPanel.setBorder(new TitledBorder("Tracking"));
		c = new PamGridBagContraints();
		trackPanel.add(new JLabel("Max time step ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(maxTimeStepS = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Max speed ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(maxSpeed = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" m/s", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Max size ratio ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(maxSizeRatio = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Min num points ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(minTrackPoints = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Min total length ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(minLength = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" m", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Min straight length ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(minStraightLength = new JTextField(4), c);
		c.gridx++;
		trackPanel.add(new JLabel(" m", JLabel.LEFT), c);
		
		vetoPanel = thresholdDetector.getSpatialVetoManager().getDialogPanel(this);

		JTabbedPane tabbedPanel = new JTabbedPane();
		tabbedPanel.add(thresholdPanel, "Threshold Detector");
		tabbedPanel.add(vetoPanel.getDialogComponent(), "Spatial Vetos");
		tabbedPanel.add(trackPanel, "Tracking");
		setDialogComponent(tabbedPanel);
	}

	public static ThresholdParams showDialog(Window parentFrame, ThresholdDetector thresholdDetector) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.thresholdDetector != thresholdDetector) {
			singleInstance = new ThresholdDialog(parentFrame, thresholdDetector);
//		}
		singleInstance.setParams(thresholdDetector.getThresholdParams());
		singleInstance.setVisible(true);
		return singleInstance.thresholdParams;
	}

	private void setParams(ThresholdParams thresholdParams) {
		this.thresholdParams = thresholdParams;
		sourcePanel.setSource(thresholdParams.imageDataSource);
		backgroundTime.setText(String.format("%d", thresholdParams.backgroundTimeConst));
		backgroundScale.setText(String.format("%3.2f", thresholdParams.backgroundScale));
		backgroundRecordS.setText(String.format("%d", thresholdParams.backgroundIntervalSecs));
		thresholdOn.setText(String.format("%d", thresholdParams.highThreshold));
		thresholdOff.setText(String.format("%d", thresholdParams.lowThreshold));
		minObjectSize.setText(String.format("%3.1f", thresholdParams.minSize));
		maxObjectSize.setText(String.format("%3.1f", thresholdParams.maxSize));

		int[] conTypes = RegionDetector.getConnectionTypes();
		for (int i = 0; i < conTypes.length; i++) {
			if (thresholdParams.connectionType == conTypes[i]) {
				connectionType.setSelectedIndex(i);
			}
		}
		
		TrackLinkParameters trackParams = thresholdDetector.getTrackLinkProcess().getTrackLinkParams();
		maxTimeStepS.setText(String.format("%3.2f", trackParams.maxTimeSeparation/1000.));
		maxSpeed.setText(String.format("%3.2f", trackParams.maxSpeed));
		maxSizeRatio.setText(String.format("%3.2f", trackParams.maxSizeRatio));
		minTrackPoints.setText(String.format("%d", trackParams.minTrackPoints));
		minLength.setText(String.format("%3.2f", trackParams.minWobblyLength));
		minStraightLength.setText(String.format("%3.2f", trackParams.minStraightLength));
		
		vetoPanel.setParams();
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
			thresholdParams.backgroundIntervalSecs = Integer.valueOf(backgroundRecordS.getText());
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
		try {
			thresholdParams.minSize = Double.valueOf(minObjectSize.getText());
			thresholdParams.maxSize = Double.valueOf(maxObjectSize.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid size value");
		}

		TrackLinkParameters trackParams = thresholdDetector.getTrackLinkProcess().getTrackLinkParams().clone();
		try {
			trackParams.maxTimeSeparation = (long) (Double.valueOf(maxTimeStepS.getText()) * 1000.);
			trackParams.maxSpeed = Double.valueOf(maxSpeed.getText());
			trackParams.maxSizeRatio = Double.valueOf(maxSizeRatio.getText());
			trackParams.minTrackPoints = Integer.valueOf(minTrackPoints.getText());
			trackParams.minWobblyLength = Double.valueOf(minLength.getText());
			trackParams.minStraightLength = Double.valueOf(minStraightLength.getText());
		}
		catch (NumberFormatException exc) {
			return showWarning("Invalid tracking parameter");
		}
		thresholdDetector.getTrackLinkProcess().setTrackLinkParams(trackParams);
//		maxTimeStepS.setText(String.format("%3.2f", trackParams.maxSpeed/1000.));
//		maxSpeed.setText(String.format("%3.2f", trackParams.maxSpeed));
//		maxSizeRatio.setText(String.format("%3.2f", trackParams.maxSizeRatio));
//		minTrackPoints.setText(String.format("%e", trackParams.minTrackPoints));
//		minLength.setText(String.format("%3.2f", trackParams.minWobblyLength));
//		minStraightLength.setText(String.format("%3.2f", trackParams.minStraightLength));
		
		vetoPanel.getParams();
		
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
