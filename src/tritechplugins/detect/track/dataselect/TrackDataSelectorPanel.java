package tritechplugins.detect.track.dataselect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class TrackDataSelectorPanel implements PamDialogPanel {

	private JPanel mainPanel;
	
	private TrackDataSelector trackDataSelector;
	
	private JTextField minLength, minStraightness, minDuration, minPoints, maxPerFrame, minPointRate;
	private JTextField minScore;
	private JCheckBox vetoX0;
	
	public TrackDataSelectorPanel(TrackDataSelector trackDataSelector) {
		this.trackDataSelector = trackDataSelector;
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		minLength = new JTextField(4);
		minLength.setToolTipText("Minimum end to end length of track");
		minStraightness = new JTextField(4);
		minStraightness.setToolTipText("Min track straightness: Ratio of end to end length / total length");
		minDuration = new JTextField(4);
		minDuration.setToolTipText("Minimum track duration in seconds");
		minPoints = new JTextField(4);
		minPoints.setToolTipText("Minimum number of frames in track");
		maxPerFrame = new JTextField(4);
		maxPerFrame.setToolTipText("Maximum number of detected regions per frame");
		vetoX0 = new JCheckBox("Veto 'tracks' only on x = 0 centre line");
		vetoX0.setToolTipText("Veto tracks caused by additional noise on x=0 centre line");
		minPointRate = new JTextField(4);
		minPointRate.setToolTipText("Minimum average rate of points within track (per second)");
		minScore = new JTextField(4);
		minScore.setToolTipText("Track evenness score. Should generally be > .5");
		
		mainPanel.add(new JLabel("Minimum length "), c);
		c.gridx++;
		mainPanel.add(minLength, c);
		c.gridx++;
		mainPanel.add(new JLabel(" m"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum straightness "), c);
		c.gridx++;
		mainPanel.add(minStraightness, c);
		c.gridx++;
		mainPanel.add(new JLabel(" 0 - 1"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum points "), c);
		c.gridx++;
		mainPanel.add(minPoints, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum duration "), c);
		c.gridx++;
		mainPanel.add(minDuration, c);
		c.gridx++;
		mainPanel.add(new JLabel(" s"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum point rate "), c);
		c.gridx++;
		mainPanel.add(minPointRate, c);
		c.gridx++;
		mainPanel.add(new JLabel(" /s"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Maximum points per frame "), c);
		c.gridx++;
		mainPanel.add(maxPerFrame, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum strack score "), c);
		c.gridx++;
		mainPanel.add(minScore, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(vetoX0, c);
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		TrackDataSelectorParams params = trackDataSelector.getParams();
		minLength.setText(String.format("%3.1f", params.minLength));
		minStraightness.setText(String.format("%3.2f", params.minStraightness));
		minDuration.setText(String.format("%3.1f", params.minDuration));
		minPoints.setText(String.format("%d", params.minPoints));
		maxPerFrame.setText(String.format("%d", params.maxPointsPerFrame));
		minPointRate.setText(String.format("%3.2f", params.minPointRate));
		minScore.setText(String.format("%3.2f", params.minTrackScore));
		vetoX0.setSelected(params.vetoXzero);		
	}

	@Override
	public boolean getParams() {
		TrackDataSelectorParams params = trackDataSelector.getParams();
		try {
			params.minLength = Double.valueOf(minLength.getText());
			params.minStraightness = Double.valueOf(minStraightness.getText());
			params.minDuration = Double.valueOf(minDuration.getText());
			params.minPoints = Integer.valueOf(minPoints.getText());
			params.maxPointsPerFrame = Integer.valueOf(maxPerFrame.getText());
			params.minPointRate = Double.valueOf(minPointRate.getText());
			params.minTrackScore = Double.valueOf(minScore.getText());
			params.vetoXzero = vetoX0.isSelected();
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Parameter error", "Invalid parameter value");
		}
		if (params.minStraightness < 0 || params.minStraightness > 1) {
			return PamDialog.showWarning(null, "Straightness", "Track strightness must be between 0 and 1.0");
		}
		return true;
	}

}
