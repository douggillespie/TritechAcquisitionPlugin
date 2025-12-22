package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;

public class SonarPositionDialog extends PamDialog implements SonarRemoveObserver {

	private static final long serialVersionUID = 1L;

	private TritechAcquisition tritechDaq;
	
	private TritechDaqParams daqParams;
	
	private static SonarPositionDialog singleInstance;
	
	private JPanel sonarsPanel;
	
	private JButton addButton;
	
	private JPanel ctrlPanel;
		
//	private GeoReferencePanel refPanel;

	private SonarPositionDialog(Window parentFrame, TritechAcquisition tritechDaq) {
		super(parentFrame, "sonar Positions", true);
		this.tritechDaq = tritechDaq;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		ctrlPanel = new JPanel(new GridBagLayout());
		PamAlignmentPanel pap;
		mainPanel.add(BorderLayout.NORTH, pap = new PamAlignmentPanel(ctrlPanel, BorderLayout.EAST));
		pap.setBorder(new TitledBorder("Manage sonars"));
		addButton = new JButton("Add");
		GridBagConstraints c = new PamGridBagContraints();
		ctrlPanel.add(addButton, c);
		c.gridx++;
		
		sonarsPanel = new JPanel();
		sonarsPanel.setLayout(new BoxLayout(sonarsPanel, BoxLayout.Y_AXIS));
		mainPanel.add(BorderLayout.CENTER, sonarsPanel);
		
		addButton.setToolTipText("Add a sonar (if not present in sonars listings)");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSonar();
			}
		});
		
		
		setDialogComponent(mainPanel);
	}
	
	protected void addSonar() {
	// TODO Auto-generated method stub
	
	}

	public static TritechDaqParams showDialog(Window parentFrame, TritechAcquisition tritechAcquisition) {
//		if (singleInstance == null) {
			singleInstance = new SonarPositionDialog(parentFrame, tritechAcquisition);
//		}
		singleInstance.setParams(tritechAcquisition);
		singleInstance.setVisible(true);
		return singleInstance.daqParams;
	}

	private void setParams(TritechAcquisition tritechAcquisition) {
		int[] sonars = tritechAcquisition.getSonarIds();
		int nSonar = 0;
		if (sonars != null) {
			nSonar = sonars.length;
		}
		daqParams = tritechAcquisition.getDaqParams();
		setGeoReference(daqParams);
		
		sonarsPanel.removeAll();
		for (int i = 0; i < nSonar; i++) {
			SonarPositionPanel spp = new SonarPositionPanel(sonars[i], this);
			sonarsPanel.add(spp);
			spp.setParams(daqParams);
			sonarsPanel.setToolTipText("Positions are relative to the PAMGuard Hydropone array position information");
		}
		pack();
	}

	@Override
	public boolean getParams() {
		int nSonar = sonarsPanel.getComponentCount();
		TritechDaqParams params = tritechDaq.getDaqParams();
		
		if (getGeoReference(params) == false) {
			return false;
		}
		
		for (int i = 0; i < nSonar; i++) {
			SonarPositionPanel spp = (SonarPositionPanel) sonarsPanel.getComponent(i);
			int sonarId = spp.getSonarId();
			boolean ok = spp.getParams(params);
			if (ok == false) {
				return false;
			}
		}
		return true;
	}

	private void setGeoReference(TritechDaqParams params) {
		// TODO Auto-generated method stub
		
	}

	private boolean getGeoReference(TritechDaqParams params) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSonar(int sonarId) {
		String msg = String.format("Are you sure you want to remove sonar %d from the list ?", sonarId);
		int ans = WarnOnce.showWarning(tritechDaq.getGuiFrame(), "Sonar Removal", msg, WarnOnce.YES_NO_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		WarnOnce.showWarning(tritechDaq.getGuiFrame(), "Sonar Removal", "Note that the sonar will reappear if it's visible in online or offline data", WarnOnce.WARNING_MESSAGE);;
		getParams();
		daqParams.removeSonarParams(sonarId);
		setParams(tritechDaq);
	}

}
