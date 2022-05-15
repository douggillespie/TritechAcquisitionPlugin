package tritechplugins.acquire.swing;

import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.PamSlider;
import PamView.dialog.PamTextField;
import PamView.panel.PamPanel;

public class ValueSlider {

	private String name;
	private String units;
	private int[] limits;
	private JSlider slider;
	private PamTextField value;
	private JSpinner spinner;
	private SpinnerModel spinnerModel;
	private JPanel mainPanel;

	private ArrayList<ChangeListener> changeListeners = new ArrayList<>();
	
	public ValueSlider(String name, String units, int[] limits) {
		this.name = name;
		this.units = units;
		this.limits = limits;
		slider = new PamSlider(limits[0], limits[1]);
		value = new PamTextField(5);
		value.setEditable(false);
		spinnerModel = new SpinnerNumberModel(limits[0], limits[0], limits[1], 1);
		spinner = new JSpinner(spinnerModel);
		spinner.setEditor(value);
		
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sliderChange(e);
			}
		});
		
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				spinnerChange(e);
			}
		});
		
		mainPanel = new PamPanel(new FlowLayout());
		mainPanel.add(slider);
		mainPanel.add(spinner);
	}
	
	/**
	 * Called when the slider changes
	 * @param e
	 */
	protected void sliderChange(ChangeEvent e) {
		int sliderVal = slider.getValue();
		spinner.setValue(sliderVal);
		sayTextValue(sliderVal);
		notifyListeners(e);
	}

	/**
	 * Called when the spinner changes. 
	 * @param e
	 */
	private void spinnerChange(ChangeEvent e) {
		int spinnerVal = (int) spinner.getValue();
		slider.setValue(spinnerVal);
		sayTextValue(spinnerVal);
		notifyListeners(e);
	}

	public JComponent getComponent() {
		return mainPanel;
	}
	
	public void setValue(int value) {
		slider.setValue(value);
		sayTextValue(value);
	}
	
	public int getValue() {
		return slider.getValue();
	}
	
	private void sayTextValue(int value) {
		this.value.setText(String.format("%d %s", value, units));
	}
	
	/**
	 * Add a change listener to get notifications when values change
	 * @param changeListener
	 */
	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	/**
	 * Notify listeners of changes. 
	 * @param e
	 */
	private void notifyListeners(ChangeEvent e) {
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}
}
