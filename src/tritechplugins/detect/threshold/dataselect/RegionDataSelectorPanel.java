package tritechplugins.detect.threshold.dataselect;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class RegionDataSelectorPanel implements PamDialogPanel {

	private RegionDataSelector regionDataSelector;
	
	private JPanel mainPanel;
	
	private JTextField minSize, maxSize, minOccupancy;
	
	public RegionDataSelectorPanel(RegionDataSelector regionDataSelector) {
		super();
		this.regionDataSelector = regionDataSelector;
		
		minSize = new JTextField(4);
		maxSize = new JTextField(4);
		minOccupancy = new JTextField(4);
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.add(new JLabel("Minimum size ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minSize,c);
		c.gridx++;
		mainPanel.add(new JLabel(" m", JLabel.LEFT), c);
		
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Maximum size ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(maxSize,c);
		c.gridx++;
		mainPanel.add(new JLabel(" m", JLabel.LEFT), c);
		
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum occupancy ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minOccupancy,c);
		c.gridx++;
		mainPanel.add(new JLabel(" %", JLabel.LEFT), c);
				
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		RegionDataSelectorParams params = regionDataSelector.getRegionDataSelectorParams();
		minSize.setText(String.format("%3.1f", params.minSize));
		maxSize.setText(String.format("%3.1f", params.maxSize));
		minOccupancy.setText(String.format("%3.1f", params.minOccupancy));
	}

	@Override
	public boolean getParams() {
		RegionDataSelectorParams params = regionDataSelector.getRegionDataSelectorParams();
		try {
			params.minSize = Double.valueOf(minSize.getText());
			params.maxSize = Double.valueOf(maxSize.getText());
			params.minOccupancy = Double.valueOf(minOccupancy.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Data Selector", "Invalid parameter in region data selector settings");
		}
		return true;
	}


}
