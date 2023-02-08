package tritechplugins.display.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.ColourComboBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.WestAlignedPanel;

public class MoreDisplayParamsDialog extends PamDialog{

	private static MoreDisplayParamsDialog singleInstance;
	
	private SonarsPanelParams sonarParams;
	
	private JComboBox<String> resolution;

	private ColourComboBox colourComboBox;

	private SonarsPanel sonarsPanel;
	
	private JRadioButton tipText, tipImage, tipBoth;
	
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
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel stylePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		stylePanel.setBorder(new TitledBorder("Image style"));
		stylePanel.add(new JLabel("Image resolution", JLabel.RIGHT), c);
		c.gridx ++;
		stylePanel.add(resolution, c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		stylePanel.add(new PamLabel("Colour", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		stylePanel.add(colourComboBox, c);
		colourComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colourChange();
			}
		});
		
		JPanel tipPanel = new WestAlignedPanel(new GridBagLayout());
		tipPanel.setBorder(new TitledBorder("Tool Tip options"));
		tipText = new JRadioButton("Show text");
		tipImage = new JRadioButton("Show image");
		tipBoth = new JRadioButton("Show both");
		ButtonGroup bg = new ButtonGroup();
		bg.add(tipText);
		bg.add(tipImage);
		bg.add(tipBoth);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tipPanel.add(tipText, c);
		c.gridy++;
		tipPanel.add(tipImage, c);
		c.gridy++;
		tipPanel.add(tipBoth, c);
		c.gridy++;
		tipPanel.setToolTipText("When viewing, tap 't' to cycle through these options");
		
		
		mainPanel.add(stylePanel);
		mainPanel.add(tipPanel);
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
		int tipType = sonarParams.getToolTipType();
		tipText.setSelected(tipType == SonarsPanelParams.TOOLTIP_TEXT);
		tipImage.setSelected(tipType == SonarsPanelParams.TOOLTIP_IMAGE);
		tipBoth.setSelected(tipType == SonarsPanelParams.TOOLTIP_BOTH);

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

		if (tipText.isSelected()) {
			sonarParams.setToolTipType(SonarsPanelParams.TOOLTIP_TEXT);
		}
		else if (tipImage.isSelected()) {
			sonarParams.setToolTipType(SonarsPanelParams.TOOLTIP_IMAGE);
		}
		else if (tipBoth.isSelected()) {
			sonarParams.setToolTipType(SonarsPanelParams.TOOLTIP_BOTH);
		}
		
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
