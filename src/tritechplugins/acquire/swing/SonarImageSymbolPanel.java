package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

import PamView.ColourComboBox;
import PamView.GeneralProjector;
import PamView.dialog.PamGridBagContraints;
import PamView.symbol.SwingSymbolOptionsPanel;

public class SonarImageSymbolPanel implements SwingSymbolOptionsPanel {

	private JPanel mainPanel;
	
	private ColourComboBox colourComboBox;
	
	private JSlider gainSlider, transparency;

	private SonarImageSymbolChooser symbolChooser;
	
	private JCheckBox showGrid;
	
	private JCheckBox showImage;
	
	public SonarImageSymbolPanel(SonarImageSymbolChooser sonarImageSymbolChooser, GeneralProjector projector) {
		this.symbolChooser = sonarImageSymbolChooser;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Sonar image options"));
		colourComboBox = new ColourComboBox(200, 15);
		colourComboBox.setBorder(null);
		colourComboBox.setToolTipText("Select colour scheme");
		gainSlider = new JSlider(1, 10);
		transparency = new JSlider(0,255);
		showImage = new JCheckBox("Show image");
		showGrid = new JCheckBox("Show grid");
		showImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 1;
		c.gridx = 1;
		mainPanel.add(showImage, c);
		c.gridy++;
		mainPanel.add(showGrid, c);
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		mainPanel.add(new JLabel("Colour ", JLabel.RIGHT), c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(colourComboBox, c);
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		mainPanel.add(new JLabel("Gain ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(gainSlider, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Transpacency ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(transparency, c);
		
	}

	protected void enableControls() {
		colourComboBox.setEnabled(showImage.isSelected());
		gainSlider.setEnabled(showImage.isSelected());
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		SonarImageSymbolOptions symbolOptions = symbolChooser.getSymbolOptions();
		colourComboBox.setSelectedColourMap(symbolOptions.colourMap);
		gainSlider.setValue(symbolOptions.displayGain);
		showGrid.setSelected(symbolOptions.showGrid);
		showImage.setSelected(symbolOptions.showImage);
		transparency.setValue(symbolOptions.transparency);
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		SonarImageSymbolOptions symbolOptions = symbolChooser.getSymbolOptions();
		symbolOptions = symbolOptions.clone();
		symbolOptions.colourMap = colourComboBox.getSelectedColourMap();
		symbolOptions.displayGain = gainSlider.getValue();
		symbolOptions.showGrid = showGrid.isSelected();
		symbolOptions.showImage = showImage.isSelected();
		symbolOptions.transparency = transparency.getValue();
		
		symbolChooser.setSymbolOptions(symbolOptions);
		return true;
	}

}
