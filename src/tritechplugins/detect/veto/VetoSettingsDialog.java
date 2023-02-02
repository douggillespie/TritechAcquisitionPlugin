package tritechplugins.detect.veto;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class VetoSettingsDialog extends PamDialog {

	private SpatialVeto spatialVeto;
	private PamDialogPanel dialogPanel;
	private boolean settingsOk;
	
	private JTextField sonarId;
	private JCheckBox allSonars;

	private VetoSettingsDialog(Window parentFrame, SpatialVeto spatialVeto, PamDialogPanel dialogPanel) {
		super(parentFrame, spatialVeto.getName(), false);
		this.spatialVeto = spatialVeto;
		this.dialogPanel = dialogPanel;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(new TitledBorder("Sonars"));
		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, dialogPanel.getDialogComponent());
		
		allSonars = new JCheckBox("All sonars");
		sonarId = new JTextField(4);
		GridBagConstraints c = new PamGridBagContraints();
		topPanel.add(allSonars,c);
		c.gridy++;
		topPanel.add(new JLabel("Specific sonar ", JLabel.RIGHT), c);
		c.gridx++;
		topPanel.add(sonarId);
		
		allSonars.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	protected void enableControls() {
		sonarId.setEnabled(allSonars.isSelected() == false);
	}

	public static boolean showDialog(Window parentFrame, SpatialVeto spatialVeto, PamDialogPanel dialogPanel) {
		VetoSettingsDialog dialog = new VetoSettingsDialog(parentFrame, spatialVeto, dialogPanel);
		dialog.setParams();
		dialog.setVisible(true);
		
		return dialog.settingsOk;
	}

	private void setParams() {
		SpatialVetoParams params = spatialVeto.getParams();
		allSonars.setSelected(params.sonarId == 0);
		sonarId.setText(String.format("%d", params.sonarId));
		dialogPanel.setParams();
		enableControls();
	}

	@Override
	public boolean getParams() {
		settingsOk = false;
		SpatialVetoParams params = spatialVeto.getParams();
		if (allSonars.isSelected()) {
			params.sonarId = 0;
		}
		else {
			try {
				params.sonarId = Integer.valueOf(sonarId.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid sonar Id, must be integer");
			}
		}
		return settingsOk = dialogPanel.getParams();
	}

	@Override
	public void cancelButtonPressed() {
		settingsOk = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
