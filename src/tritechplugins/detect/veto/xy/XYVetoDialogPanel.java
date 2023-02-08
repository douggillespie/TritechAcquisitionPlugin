package tritechplugins.detect.veto.xy;

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

public class XYVetoDialogPanel implements PamDialogPanel {

	private XYVeto xyVeto;
	
	private JPanel mainPanel;
	
	private JTextField xMin, xMax, yMin, yMax;

	public XYVetoDialogPanel(XYVeto xyVeto) {
		this.xyVeto = xyVeto;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("X,Y Box Veto"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("x ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(xMin = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" to "), c);
		c.gridx++;
		mainPanel.add(xMax = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" m "), c);
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("y ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(yMin = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" to "), c);
		c.gridx++;
		mainPanel.add(yMax = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" m "), c);
		c.gridy++;
		c.gridx = 0;
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		XYVetoParams xyParams = xyVeto.getParams();
		xMin.setText(String.format("%3.1f", xyParams.xMin));
		xMax.setText(String.format("%3.1f", xyParams.xMax));
		yMin.setText(String.format("%3.1f", xyParams.yMin));
		yMax.setText(String.format("%3.1f", xyParams.yMax));
		
	}

	@Override
	public boolean getParams() {
		XYVetoParams xyParams = xyVeto.getParams();
		try {
			xyParams.xMin = Double.valueOf(xMin.getText());
			xyParams.xMax = Double.valueOf(xMax.getText());
			xyParams.yMin = Double.valueOf(yMin.getText());
			xyParams.yMax = Double.valueOf(yMax.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Invalid parameterr", "Invalid parameter");
		}
		
		return true;
	}


}
