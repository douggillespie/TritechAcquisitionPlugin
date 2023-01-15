package tritechplugins.display.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
import PamView.dialog.SettingsButton;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;

public class DisplayControlPanel {

	private SonarsOuterPanel sonarsOuterPanel;
	
//	private ColourComboBox colourComboBox;
	
	private JSlider gainSlider;
	
	private JLabel gainText;
	
	private JCheckBox showGrid;
	
	private JCheckBox flipLeftRight;

	private JPanel mainPanel;

	private SonarsPanel sonarsPanel;
	
	private JCheckBox removeBackground;
	
	private JTextField backgroundTimeFac;
	
	private JTextField backgroundScaleFac;
	
	private JCheckBox usePersistence;
	
	private JTextField persistentFrames;
	
	private JCheckBox rescalePersistence;
	
//	private HidingPanel hidingPanel;
	
	public DisplayControlPanel(SonarsOuterPanel sonarsOuterPanel, SonarsPanel sonarsPanel) {
		this.sonarsOuterPanel = sonarsOuterPanel;
		this.sonarsPanel = sonarsPanel;
		mainPanel = new PamPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Display options"));
		
		GeneralAction generalAction = new GeneralAction();
		GeneralFocus generalFocus = new GeneralFocus();
		
//		colourComboBox = new ColourComboBox(200, 15);
		
		gainSlider = new JSlider(1, 10);

		c.gridwidth = 1;
		mainPanel.add(gainText = new PamLabel("gain: x2 "), c);
		c.gridx+=1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		mainPanel.add(showGrid = new PamCheckBox("Grid"), c);
		c.anchor = GridBagConstraints.WEST;
		showGrid.addActionListener(generalAction);
		c.gridx+=c.gridwidth;
		c.gridwidth = 2;
		mainPanel.add(flipLeftRight = new PamCheckBox("Flip image"), c);
		flipLeftRight.addActionListener(generalAction);
		c.gridx+=c.gridwidth+1;
		c.gridwidth = 1;
		SettingsButton moreButton = new SettingsButton();
		mainPanel.add(moreButton, c);
		moreButton.setToolTipText("Resolution and Colour)");
		moreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moreSettings();
			}
		});
		
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
//		c.gridy++;
//		c.gridwidth = 1;
//		mainPanel.add(new PamLabel("Colour", JLabel.RIGHT), c);
//		c.gridx++;
//		c.gridwidth = 5;
//		mainPanel.add(colourComboBox, c);
//		colourComboBox.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				colourChange();
//			}
//		});
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		mainPanel.add(removeBackground = new JCheckBox("Cut background "), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(new PamLabel(" Time"), c);
		c.gridx += c.gridwidth;
		mainPanel.add(backgroundTimeFac = new PamTextField(2), c);
		c.gridx++;
		mainPanel.add(new PamLabel(" Scale"), c);
		c.gridx += c.gridwidth;
		mainPanel.add(backgroundScaleFac = new PamTextField(2), c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		mainPanel.add(usePersistence = new JCheckBox("Persistant image "), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(new PamLabel(" Time"), c);
		c.gridx += c.gridwidth;
		mainPanel.add(persistentFrames = new PamTextField(2), c);
		c.gridx++;
		mainPanel.add(new PamLabel(" (frames)"), c);;
		c.gridx++;
		mainPanel.add(rescalePersistence = new JCheckBox("Rescale"), c);
		
		removeBackground.addActionListener(generalAction);
		backgroundTimeFac.addActionListener(generalAction);
		backgroundScaleFac.addActionListener(generalAction);
		usePersistence.addActionListener(generalAction);
		persistentFrames.addActionListener(generalAction);
		rescalePersistence.addActionListener(generalAction);
		backgroundTimeFac.addFocusListener(generalFocus);
		backgroundScaleFac.addFocusListener(generalFocus);
		persistentFrames.addFocusListener(generalFocus);
		
		gainSlider.setToolTipText("Amplify the image");
//		colourComboBox.setToolTipText("Select colour scheme");
		showGrid.setToolTipText("Overlay grid");
		flipLeftRight.setToolTipText("Flip images left-right");
		removeBackground.setToolTipText("Automatically remove stationary background from image");
		backgroundTimeFac.setToolTipText("Background scale factor: big numbers = slow background update, small = fast update");
		usePersistence.setToolTipText("Generate a persistent image: the average or sum of multiple frames");
		persistentFrames.setToolTipText("Number of frames in persistent image");
		rescalePersistence.setToolTipText("Rescale data (divide by number of frames)");
//		gainSlider.setMajorTickSpacing(5);
//		gainSlider.setMinorTickSpacing(1);
//		gainSlider.setPaintTicks(true);
//		c.gridy++;
		
//		hidingPanel = new HidingPanel(sonarsPanel, mainPanel, HidingPanel.HORIZONTAL, false);
		
	}

	protected void moreSettings() {
		SonarsPanelParams newParams = MoreDisplayParamsDialog.showDialog(null, sonarsPanel);
		if (newParams != null) {
			sonarsPanel.setSonarsPanelParams(newParams);
			setParams();
			sonarsPanel.remakeImages();

			sonarsPanel.updateColourMap(newParams.colourMap);
		}
		
	}

	private class GeneralAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			parameterChange();
		}
	}
	
	private class GeneralFocus implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {			
		}

		@Override
		public void focusLost(FocusEvent e) {
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
		params.usePersistence = usePersistence.isSelected();
		params.rescalePersistence = rescalePersistence.isSelected();
		try {
			double update = Double.valueOf(backgroundTimeFac.getText());
			int iUpdate = (int) Math.round(update);
			if (iUpdate > 1) {
				params.backgroundTimeFactor = iUpdate;
			}
//			System.out.println("Background scale updated to " + iUpdate);
			if (params.usePersistence) {
				params.persistentFrames = Integer.valueOf(persistentFrames.getText());
			}
		}
		catch (NumberFormatException e) {
		}
		try {
			double fac = Double.valueOf(backgroundScaleFac.getText());
			params.backgroundScale = fac;			
		}
		catch (NumberFormatException e) {
			
		}
	}

	protected void colourChange() {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
//		params.colourMap = colourComboBox.getSelectedColourMap();
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
//		colourComboBox.setSelectedColourMap(params.colourMap);
		gainSlider.setValue(params.displayGain);
		removeBackground.setSelected(params.subtractBackground);
		backgroundTimeFac.setText(String.format("%d", params.backgroundTimeFactor));
		backgroundScaleFac.setText(String.format("%3.2f", params.backgroundScale));
		usePersistence.setSelected(params.usePersistence);
		persistentFrames.setText(String.format("%d", params.persistentFrames));
		rescalePersistence.setSelected(params.rescalePersistence);
		sayGain();
	}

}
