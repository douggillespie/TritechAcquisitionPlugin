package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import PamView.panel.PamAlignmentPanel;
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
	
	private StatusTopPanel topPanel;
	
	private HashMap<Integer, SonarStatusPanel> statusPanels= new HashMap<>();

	public SonarsStatusPanel(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		
		mainPanel = new CornerPanel(new BorderLayout());
		sonarsPanel = new CornerPanel();
		sonarsPanel.setLayout(new BoxLayout(sonarsPanel, BoxLayout.X_AXIS));
		PamAlignmentPanel lap = new PamAlignmentPanel(sonarsPanel, BorderLayout.WEST);
		mainPanel.add(lap, BorderLayout.CENTER);
		
		topPanel = new StatusTopPanel(tritechAcquisition);
		mainPanel.add(topPanel.getComponent(), BorderLayout.NORTH);

		tritechDaqProcess.addStatusObserver(this);
		
//		mainPanel.setOpaque(false);
//		sonarsPanel.setOpaque(false);
		
//		addStatusPanel(678); used to test layout in absence of a sonar!
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
			statusPanels.put(sonarStatusData.getDeviceId(), statusPanel);
			isNew = true;
		}
		statusPanel.updateStatus(sonarStatusData);
		if (isNew) {
			mainPanel.invalidate();
		}
	}

	@Override
	public void errorMessage(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	private SonarStatusPanel addStatusPanel(int deviceId) {
		SonarStatusPanel statusPanel = new SonarStatusPanel(tritechAcquisition, deviceId);
		sonarsPanel.add(statusPanel.getComponent());
		return statusPanel;
	}

	@Override
	public void updateFrameRate(int frameRate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFileName(String fileName) {
		
	}

}
