package tritechplugins.display.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.ColourComboBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.warn.WarnOnce;

public class MoreDisplayParamsDialog extends PamDialog{

	private static MoreDisplayParamsDialog singleInstance;
	
	private SonarsPanelParams sonarParams;
	
	private JComboBox<String> resolution;

	private ColourComboBox colourComboBox;

	private SonarsPanel sonarsPanel;
	
	private MoreDisplayParamsDialog(Window parentFrame) {
		super(parentFrame, "Sonar display", false);
		
		colourComboBox = new ColourComboBox(200, 15);
		colourComboBox.setBorder(null);
		colourComboBox.setToolTipText("Select colour scheme");
		
		resolution = new JComboBox<String>();
		int[] ress = SonarsPanelParams.getResolutionValues();
		for (int i = 0; i < ress.length; i++) {
			resolution.addItem(SonarsPanelParams.getResolutionName(ress[i]));
		}
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.setBorder(new TitledBorder("Image style"));
		mainPanel.add(new JLabel("Image resolution", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(resolution, c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new PamLabel("Colour", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(colourComboBox, c);
		colourComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colourChange();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	public static SonarsPanelParams showDialog(Window parent, SonarsPanel sonarsPanel) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new MoreDisplayParamsDialog(parent);
		}
		singleInstance.setParams(sonarsPanel);
		singleInstance.moveToMouseLocation();
		singleInstance.setVisible(true);
		return singleInstance.sonarParams;
	}

	private void setParams(SonarsPanel sonarsPanel) {
		this.sonarsPanel = sonarsPanel;
		this.sonarParams = sonarsPanel.getSonarsPanelParams();
		int[] ress = SonarsPanelParams.getResolutionValues();
		for (int i = 0; i < ress.length; i++) {
			if (sonarParams.resolution == ress[i]) {
				resolution.setSelectedIndex(i);
			}
		}

		colourComboBox.setSelectedColourMap(sonarsPanel.getSonarsPanelParams().colourMap);
	}

	protected void colourChange() {
//		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		sonarParams.colourMap = colourComboBox.getSelectedColourMap();
		if (sonarsPanel != null) {
			sonarsPanel.updateColourMap(sonarParams.colourMap);
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

		sonarParams.colourMap = colourComboBox.getSelectedColourMap();
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		sonarParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
//		setParams(new SonarsPanelParams());
	}

}
