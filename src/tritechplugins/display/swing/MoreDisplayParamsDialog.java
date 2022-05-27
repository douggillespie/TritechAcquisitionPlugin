package tritechplugins.display.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

public class MoreDisplayParamsDialog extends PamDialog{

	private static MoreDisplayParamsDialog singleInstance;
	
	private SonarsPanelParams sonarParams;
	
	private JComboBox<String> resolution;
	
	private MoreDisplayParamsDialog(Window parentFrame) {
		super(parentFrame, "Sonar display", true);
		
		resolution = new JComboBox<String>();
		int[] ress = SonarsPanelParams.getResolutionValues();
		for (int i = 0; i < ress.length; i++) {
			resolution.addItem(SonarsPanelParams.getResolutionName(ress[i]));
		}
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.setBorder(new TitledBorder("Image resolution"));
		mainPanel.add(new JLabel("Image resolution", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(resolution, c);
		
		setDialogComponent(mainPanel);
	}
	
	public static SonarsPanelParams showDialog(Window parent, SonarsPanelParams params) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new MoreDisplayParamsDialog(parent);
		}
		singleInstance.setParams(params);
		singleInstance.setVisible(true);
		return singleInstance.sonarParams;
	}

	private void setParams(SonarsPanelParams params) {
		this.sonarParams = params;
		int[] ress = SonarsPanelParams.getResolutionValues();
		for (int i = 0; i < ress.length; i++) {
			if (sonarParams.resolution == ress[i]) {
				resolution.setSelectedIndex(i);
			}
		}
	}

	@Override
	public boolean getParams() {
		int[] ress = SonarsPanelParams.getResolutionValues();
		int ind = resolution.getSelectedIndex();
		int res = ress[ind];
		if (res == SonarsPanelParams.RESOLUTION_BEST && PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			int ans = WarnOnce.showWarning("Image resolution", "High resolution images may impact performance and are not recommended for real time operation", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		sonarParams.resolution = res;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		sonarParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new SonarsPanelParams());
	}

}
