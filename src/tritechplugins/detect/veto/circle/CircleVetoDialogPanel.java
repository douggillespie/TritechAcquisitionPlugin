package tritechplugins.detect.veto.circle;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class CircleVetoDialogPanel implements PamDialogPanel {

	private JPanel mainPanel;
	
	private JTextField centreX, centreY, radius;
	private JRadioButton vetoInside, vetoOutside;

	private CircleVeto circleVeto;
	
	public CircleVetoDialogPanel(CircleVeto circleVeto) {
		this.circleVeto = circleVeto;
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		centreX = new JTextField(4);
		centreY = new JTextField(4);
		radius = new JTextField(4);
		vetoInside = new JRadioButton("Veto inside circle");
		vetoOutside = new JRadioButton("Veto outside circle");
		ButtonGroup bg = new ButtonGroup();
		bg.add(vetoInside);
		bg.add(vetoOutside);
		
		mainPanel.add(new JLabel("Centre: x,y", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(centreX, c);
		c.gridx++;
		mainPanel.add(new JLabel(",", JLabel.CENTER), c);
		c.gridx++;
		mainPanel.add(centreY, c);
		c.gridx++;
		mainPanel.add(new JLabel(" m", JLabel.CENTER), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel(" Radius", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(radius, c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(new JLabel(" m", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(vetoInside, c);
		c.gridy++;
		mainPanel.add(vetoOutside, c);
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		CircleVetoParams circleParams = circleVeto.getParams();
		centreX.setText(String.format("%3.1f", circleParams.centreX));
		centreY.setText(String.format("%3.1f", circleParams.centreY));
		radius.setText(String.format("%3.1f", circleParams.radius));
		vetoInside.setSelected(circleParams.vetoInside);
		vetoOutside.setSelected(!circleParams.vetoInside);
	}

	@Override
	public boolean getParams() {
		CircleVetoParams circleParams = circleVeto.getParams();
		try {
			circleParams.centreX = Double.valueOf(centreX.getText());
			circleParams.centreY = Double.valueOf(centreY.getText());
			circleParams.radius = Double.valueOf(radius.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Invalid parameter", "Invalid number in Circle Veto parameters");
		}
		circleParams.vetoInside = vetoInside.isSelected();
		return true;
	}

}
