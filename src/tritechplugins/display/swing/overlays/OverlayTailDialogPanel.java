package tritechplugins.display.swing.overlays;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import tritechplugins.display.swing.SonarsPanel;
import tritechplugins.display.swing.SonarsPanelParams;

public class OverlayTailDialogPanel implements PamDialogPanel {

	private JPanel mainPanel;
	
	private JRadioButton[] optButtons;
	
	private JTextField tailLength;

	private SonarsPanel sonarsPanel;
	
	public OverlayTailDialogPanel(SonarsPanel sonarsPanel) {
		this.sonarsPanel = sonarsPanel;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Tail options"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		EnableAction ea = new EnableAction();
		
		int[] vals = SonarsPanelParams.getOverlayOptValues();
		optButtons = new JRadioButton[vals.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < vals.length; i++) {
			optButtons[i] = new JRadioButton(SonarsPanelParams.getOverlayOptName(vals[i]));
			bg.add(optButtons[i]);
			optButtons[i].addActionListener(ea);
			mainPanel.add(optButtons[i], c);
			c.gridy++;
		}
		
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Tail length ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(tailLength = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" seconds"), c);
	}

	private class EnableAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void enableControls() {
		tailLength.setEnabled(getSelectedOption() == SonarsPanelParams.OVERLAY_TAIL_TIME);
	}

	@Override
	public void setParams() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		int[] vals = SonarsPanelParams.getOverlayOptValues();
		for (int i = 0; i < vals.length; i++) {
			optButtons[i].setSelected(params.tailOption == vals[i]);
		}
		tailLength.setText(String.format("%3.1f", params.tailTime));
		enableControls();
	}
	
	private int getSelectedOption() {
		int[] vals = SonarsPanelParams.getOverlayOptValues();
		for (int i = 0; i < vals.length; i++) {
			if (optButtons[i].isSelected()) {
				return vals[i];
			}
		}
		return vals[0];
	}

	@Override
	public boolean getParams() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		double t = 0;
		try {
			t = Double.valueOf(tailLength.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		params.tailOption = getSelectedOption();
		params.tailTime = t;
		return true;
	}

}
