package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import PamView.panel.PamPanel;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqProcess;

/**
 * Status panel for all sonars. Contains 0-many SonarStatusPanels. 
 * @author dg50
 *
 */
public class SonarsStatusPanel implements SonarStatusObserver {

	private TritechAcquisition tritechAcquisition;
	
	private TritechDaqProcess tritechDaqProcess;
	
	private JPanel mainPanel;
	
	private JPanel sonarsPanel;
	
	private HashMap<Integer, SonarStatusPanel> statusPanels= new HashMap<>();

	public SonarsStatusPanel(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		
		mainPanel = new PamPanel(new BorderLayout());
		sonarsPanel = new PamPanel();
		sonarsPanel.setLayout(new BoxLayout(sonarsPanel, BoxLayout.X_AXIS));
		mainPanel.add(sonarsPanel, BorderLayout.CENTER);
		
		addStatusPanel(678);
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	@Override
	public void updateStatus(SonarStatusData sonarStatusData) {
		SonarStatusPanel statusPanel = statusPanels.get(sonarStatusData.getDeviceId());
		boolean isNew = false;
		if (statusPanel == null) {
			statusPanel = addStatusPanel(sonarStatusData.getDeviceId());
			isNew = true;
		}
		statusPanel.updateStatus(sonarStatusData);
		if (isNew) {
			mainPanel.invalidate();
		}
	}

	private SonarStatusPanel addStatusPanel(int deviceId) {
		SonarStatusPanel statusPanel = new SonarStatusPanel(tritechAcquisition, deviceId);
		sonarsPanel.add(statusPanel.getComponent());
		return statusPanel;
	}

}
