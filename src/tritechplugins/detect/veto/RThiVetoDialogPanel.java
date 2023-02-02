package tritechplugins.detect.veto;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class RThiVetoDialogPanel implements PamDialogPanel {

	private RThiVeto rThiVeto;
	
	private JPanel mainPanel;
	
	private JTextField rMin, rMax, angMin, angMax;

	public RThiVetoDialogPanel(RThiVeto rThiVeto) {
		this.rThiVeto = rThiVeto;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Range / Bearing Veto"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Range ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(rMin = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" to "), c);
		c.gridx++;
		mainPanel.add(rMax = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" m "), c);
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("Bearing ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(angMin = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" to "), c);
		c.gridx++;
		mainPanel.add(angMax = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" degrees "), c);
		c.gridy++;
		c.gridx = 0;
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		RThiVetoParams rThiParams = rThiVeto.getParams();
		rMin.setText(String.format("%3.1f", rThiParams.rangeMin));
		rMax.setText(String.format("%3.1f", rThiParams.rangeMax));
		angMin.setText(String.format("%3.1f", Math.toDegrees(rThiParams.angleMin)));
		angMax.setText(String.format("%3.1f", Math.toDegrees(rThiParams.angleMax)));
		
	}

	@Override
	public boolean getParams() {
		RThiVetoParams rThiParams = rThiVeto.getParams();
		try {
			rThiParams.rangeMin = Double.valueOf(rMin.getText());
			rThiParams.rangeMax = Double.valueOf(rMax.getText());
			rThiParams.angleMin = Double.valueOf(angMin.getText());
			rThiParams.angleMax = Double.valueOf(angMax.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Invalid parameterr", "Invalid parameter");
		}
		rThiParams.angleMin = Math.toRadians(rThiParams.angleMin);
		rThiParams.angleMax = Math.toRadians(rThiParams.angleMax);
		
		return true;
	}

}
