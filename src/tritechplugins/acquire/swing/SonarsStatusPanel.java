package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import geminisdk.OutputFileInfo;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.Svs5JNADaqSystem;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqProcess;
import tritechplugins.display.swing.SonarDisplayDecoration;

/**
 * Status panel for all sonars. Contains 0-many SonarStatusPanels. 
 * @author dg50
 *
 */
public class SonarsStatusPanel implements SonarStatusObserver, SonarDisplayDecoration {

	private TritechAcquisition tritechAcquisition;
	
	private TritechDaqProcess tritechDaqProcess;
	
	private JPanel mainPanel;
	
	private JPanel sonarsPanel;
	
	private StatusTopPanel topPanel;
	
	private HashMap<Integer, SonarStatusPanel> statusPanels= new HashMap<>();

	private Svs5JNADaqSystem svs5DaqSystem;

	private HidingPanel hidingPanel;

	public SonarsStatusPanel(TritechAcquisition tritechAcquisition, Svs5JNADaqSystem svs5DaqSystem) {
		this.tritechAcquisition = tritechAcquisition;
		this.svs5DaqSystem = svs5DaqSystem;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		
		mainPanel = new CornerPanel(new BorderLayout());
		sonarsPanel = new CornerPanel();
		sonarsPanel.setLayout(new BoxLayout(sonarsPanel, BoxLayout.X_AXIS));
		PamAlignmentPanel lap = new PamAlignmentPanel(sonarsPanel, BorderLayout.WEST);
		mainPanel.add(lap, BorderLayout.CENTER);
		
		topPanel = new StatusTopPanel(tritechAcquisition, svs5DaqSystem);
		mainPanel.add(topPanel.getComponent(), BorderLayout.NORTH);

		tritechDaqProcess.addStatusObserver(this);

		hidingPanel = new HidingPanel(null, mainPanel,
				HidingPanel.HORIZONTAL, false, "Sonar Online Status", tritechAcquisition.getUnitName() + " Controls");
				
		
//		mainPanel.setOpaque(false);
//		sonarsPanel.setOpaque(false);
		
//		addStatusPanel(678); used to test layout in absence of a sonar!
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
	public void updateFrameRate(int frameRate, double trueFPS) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFileIndex(int fileIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getComponent() {
		return hidingPanel;
	}

	@Override
	public void destroyComponent() {
		tritechDaqProcess.removeStatusObserver(this);
	}

	@Override
	public void updateQueueSize(int queueSize) {
		// TODO Auto-generated method stub
		
	}

}
