package tritechplugins.display.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.ColourComboBox;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;

public class DisplayControlPanel {

	private SonarsOuterPanel sonarsOuterPanel;
	
	private ColourComboBox colourComboBox;
	
	private JSlider gainSlider;
	
	private JLabel gainText;
	
	private JCheckBox showGrid;
	
	private JCheckBox flipLeftRight;

	private JPanel mainPanel;

	private SonarsPanel sonarsPanel;
	
	private JCheckBox removeBackground;
	
	private JTextField backgroundScale;
	
//	private HidingPanel hidingPanel;
	
	public DisplayControlPanel(SonarsOuterPanel sonarsOuterPanel, SonarsPanel sonarsPanel) {
		this.sonarsOuterPanel = sonarsOuterPanel;
		this.sonarsPanel = sonarsPanel;
		mainPanel = new PamPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Display options"));
		
		GeneralAction generalAction = new GeneralAction();
		colourComboBox = new ColourComboBox(200, 15);
		
		gainSlider = new JSlider(1, 10);

		c.gridwidth = 1;
		mainPanel.add(gainText = new PamLabel("gain: x2 "), c);
		c.gridx+=2;
		c.gridwidth = 2;
		mainPanel.add(showGrid = new PamCheckBox("Show grid  "), c);
		showGrid.addActionListener(generalAction);
		c.gridx+=c.gridwidth;
		mainPanel.add(flipLeftRight = new PamCheckBox("Flip image"), c);
		flipLeftRight.addActionListener(generalAction);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 6;
		mainPanel.add(gainSlider, c);
		gainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gainChange();
			}
		});
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new PamLabel("Colour ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 5;
		mainPanel.add(colourComboBox, c);
		colourComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colourChange();
			}
		});
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		mainPanel.add(removeBackground = new JCheckBox("Remove background "), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(new PamLabel("  scale "), c);
		c.gridx += c.gridwidth;
		mainPanel.add(backgroundScale = new PamTextField(2), c);
		
		removeBackground.addActionListener(generalAction);
		backgroundScale.addActionListener(generalAction);
		
		
		gainSlider.setToolTipText("Amplify the image");
		colourComboBox.setToolTipText("Select colour scheme");
		showGrid.setToolTipText("Overlay grid");
		flipLeftRight.setToolTipText("Flip images left-right");
		removeBackground.setToolTipText("Automatically remove stationary background from image");
		backgroundScale.setToolTipText("Background scale factor: big numbers = slow background update, small = fast update");
//		gainSlider.setMajorTickSpacing(5);
//		gainSlider.setMinorTickSpacing(1);
//		gainSlider.setPaintTicks(true);
//		c.gridy++;
		
//		hidingPanel = new HidingPanel(sonarsPanel, mainPanel, HidingPanel.HORIZONTAL, false);
		
	}

	private class GeneralAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			parameterChange();
		}
	}
	

	private void parameterChange() {
		getParams();
		sonarsPanel.remakeImages();
	}
	
	private void getParams() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		params.showGrid = showGrid.isSelected();
		params.flipLeftRight = flipLeftRight.isSelected();
		params.subtractBackground = removeBackground.isSelected();
		try {
			double update = Double.valueOf(backgroundScale.getText());
			int iUpdate = (int) Math.round(update);
			if (iUpdate > 1) {
				params.backgroundScaleFactor = iUpdate;
			}
//			System.out.println("Background scale updated to " + iUpdate);
		}
		catch (NumberFormatException e) {
			
		}
	}

	protected void colourChange() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		params.colourMap = colourComboBox.getSelectedColourMap();
		sonarsPanel.updateColourMap(params.colourMap);
	}

	protected void gainChange() {
		int newGain = gainSlider.getValue();
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		params.displayGain = newGain;
		sayGain();
		sonarsPanel.remakeImages();
	}
	
	private void sayGain() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		gainText.setText(String.format("Gain: x%d ", Math.max(1, params.displayGain)));
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void setParams() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		showGrid.setSelected(params.showGrid);
		flipLeftRight.setSelected(params.flipLeftRight);
		colourComboBox.setSelectedColourMap(params.colourMap);
		gainSlider.setValue(params.displayGain);
		removeBackground.setSelected(params.subtractBackground);
		backgroundScale.setText(String.format("%d", params.backgroundScaleFactor));
		sayGain();
	}

}
