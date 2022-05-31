package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import geminisdk.Svs5Exception;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqProcess;
import tritechplugins.acquire.TritechJNADaq;
import tritechplugins.display.swing.SonarDisplayDecoration;

public class DaqControlPanel implements SonarDisplayDecoration {

	private JPanel mainPanel;
	private TritechAcquisition tritechAcquisition;
	private TritechDaqProcess tritechProcess;
	
	private ValueSlider gainSlider;
	private ValueSlider rangeSlider;
	private TritechJNADaq jnaAcquisition;
	private HidingPanel hidingPanel;
		
	public DaqControlPanel(TritechAcquisition tritechAcquisition, TritechJNADaq jnaAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.jnaAcquisition = jnaAcquisition;
		tritechProcess = tritechAcquisition.getTritechDaqProcess();
		
		mainPanel = new PamPanel();
		mainPanel.setBorder(new TitledBorder("Acquisition Control"));
		
		gainSlider = new ValueSlider("Gain", "%", tritechProcess.getGainRange());
		rangeSlider = new ValueSlider("Range", "m", tritechProcess.getRangeRange());
		
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.add(new PamLabel("Gain"), c);
		c.gridx++;
		mainPanel.add(gainSlider.getComponent(), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new PamLabel("Range"), c);
		c.gridx++;
		mainPanel.add(rangeSlider.getComponent(), c);

		hidingPanel = new HidingPanel(null, mainPanel,
				HidingPanel.HORIZONTAL, false, "Online controls", tritechAcquisition.getUnitName() + " Controls");
				
		gainSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gainChanged();
			}
		});
		
		rangeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				rangeChanged();
			}
		});
		
		setParams();
	}
	
	private void setParams() {
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		rangeSlider.setValue(daqParams.getRange());
		gainSlider.setValue(daqParams.getGain());
		
	}

	protected void rangeChanged() {
//		System.out.printf("New range value = %dm\n", rangeSlider.getValue());
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		daqParams.setRange(rangeSlider.getValue());
		try {
			jnaAcquisition.setRange(daqParams.getRange(), 0);
		} catch (Svs5Exception e) {
			e.printStackTrace();
		}
	}

	protected void gainChanged() {
//		System.out.printf("New gain value = %d%%\n", gainSlider.getValue());
		TritechDaqParams daqParams = tritechAcquisition.getDaqParams();
		daqParams.setGain(gainSlider.getValue());
		try {
			jnaAcquisition.setGain(daqParams.getGain(), 0);
		} catch (Svs5Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JComponent getComponent() {
		return hidingPanel;
	}

	@Override
	public void destroyComponent() {
		// TODO Auto-generated method stub
		
	}

}
