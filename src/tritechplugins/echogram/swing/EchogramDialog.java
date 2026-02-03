package tritechplugins.echogram.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import tritechplugins.echogram.EchogramSettings;
import tritechplugins.echogram.EchogramSettings.ValueType;

public class EchogramDialog extends PamView.dialog.PamDialog {

	private static EchogramDialog singleInstance;
	private EchogramSettings echogramSettings;
	
	private JRadioButton mean, max;
	private JTextField nBands;
	private JTextField maxOf;
	
	private EchogramDialog(Window parentFrame) {
		super(parentFrame, "Echogram settings", true);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Echogram configuration"));
		
		
		mean = new JRadioButton("Use Mean value");
		max = new JRadioButton("Use Mean over largest ");
		nBands = new JTextField(2);
		maxOf = new JTextField(2);
		ButtonGroup bg = new ButtonGroup();
		bg.add(max);
		bg.add(mean);
		max.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		mean.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		
		mainPanel.add(new JLabel("Divide beams into ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(nBands);
		c.gridx++;
		mainPanel.add(new JLabel(" regions", JLabel.LEFT), c);
		
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(mean,c);
		c.gridy++;
		mainPanel.add(max,c);
		c.gridx++;
		mainPanel.add(maxOf,c);
		c.gridx++;
		mainPanel.add(new JLabel(" values", JLabel.LEFT), c);
		
		nBands.setToolTipText("Split data and generate multiple echograms for different regions");
		mean.setToolTipText("Take value as mean across all beams");
		max.setToolTipText("Take value as max over all beams, or mean of the maximum n values");
		maxOf.setToolTipText("Take value as the mean of this number of the largest values");
		setDialogComponent(mainPanel);
	}
	
	public static EchogramSettings showDialog(Window frame, EchogramSettings echogramSettings) {
		if (singleInstance == null) {
			singleInstance = new EchogramDialog(frame);
		}
		singleInstance.setParams(echogramSettings);
		singleInstance.setVisible(true);
		return singleInstance.echogramSettings;
	}

	private void setParams(EchogramSettings echogramSettings) {
		this.echogramSettings = echogramSettings;
		
		nBands.setText(String.format("%d", echogramSettings.getnBands()));
		mean.setSelected(echogramSettings.getValueType() == ValueType.MEAN);
		max.setSelected(echogramSettings.getValueType() == ValueType.MAX);
		maxOf.setText(String.format("%d", echogramSettings.getMaxOf()));
		
		enableControls();		
	}
	
	private void enableControls() {
		maxOf.setEnabled(max.isSelected());
	}

	@Override
	public boolean getParams() {
		
		int nBand, maxOf;
		try {
			nBand = Integer.valueOf(nBands.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid value for number of bands");
		}
		if (nBand <= 0) {
			return showWarning("Number of bands must be > 0");
		}
		echogramSettings.setnBands(nBand);
		
		if (mean.isSelected()) {
			echogramSettings.setValueType(ValueType.MEAN);
		}
		else {
			echogramSettings.setValueType(ValueType.MAX);
		}
		try {
			maxOf = Integer.valueOf(this.maxOf.getText());
			echogramSettings.setMaxOf(maxOf);
		}
		catch (NumberFormatException e) {
			if (max.isSelected()) {
				return showWarning("Invalid value for number to measure maximum");
			}
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		this.echogramSettings = null;		
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new EchogramSettings());
	}

}
