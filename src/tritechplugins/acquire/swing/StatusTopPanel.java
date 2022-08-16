package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import geminisdk.OutputFileInfo;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.Svs5JNADaqSystem;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqProcess;

public class StatusTopPanel implements SonarStatusObserver {

	private TritechAcquisition tritechAcquisition;
	private TritechDaqProcess tritechDaqProcess;
	private JPanel topPanel;
	private InfoStrip versionStrip;
	private InfoStrip errorStrip;
	private InfoStrip frameRate, queueSize;;
	private boolean haveVersion = false;
	private Svs5JNADaqSystem svs5DaqSystem;

	public StatusTopPanel(TritechAcquisition tritechAcquisition, Svs5JNADaqSystem svs5DaqSystem) {
		this.tritechAcquisition = tritechAcquisition;
		this.svs5DaqSystem = svs5DaqSystem;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		
		topPanel = new PamPanel(new GridBagLayout());
		topPanel.setBorder(new TitledBorder("Sonar Status"));
		topPanel.setOpaque(false);
		GridBagConstraints c = new PamGridBagContraints();
		
		versionStrip = InfoStrip.addInfoStrip("svs5 Version", topPanel, c);
		errorStrip = InfoStrip.addInfoStrip("Error", topPanel, c);
		frameRate = InfoStrip.addInfoStrip("Frame Rate", topPanel, c);
		queueSize = InfoStrip.addInfoStrip("Svs5 queue size", topPanel, c);
		
		tritechDaqProcess.addStatusObserver(this);
		
		sayVersion();
	}

	public JComponent getComponent() {
		return topPanel;
	}
	
	@Override
	public void updateStatus(SonarStatusData sonarStatusData) {
		// TODO Auto-generated method stub
		
		if (haveVersion == false) {
			sayVersion();
		}
	}

	@Override
	public void errorMessage(String errorMessage) {
		errorStrip.setText(errorMessage);
		if (haveVersion == false) {
			sayVersion();
		}
	}
	
	public void updateQueueSize(int queueLen) {
		queueSize.setText(String.format("%d items", queueLen));
	}
	
	public void sayVersion() {
		String v = svs5DaqSystem.getLibVersion();
		if (v == null) {
			versionStrip.setText("Can't get library version");
		}
		else {
//			int cpr = v.
			versionStrip.setText(v);
			haveVersion = true;
		}
	}

	@Override
	public void updateFrameRate(int frameRate, double trueFPS) {
//		String str = String.format("%d fps, Queue %d items", frameRate, )
		this.frameRate.setText(String.format("%2d/%4.1f fps", frameRate, trueFPS));
	}

	@Override
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo) {
		errorStrip.setName("File");
		errorStrip.setText(outputFileInfo.getM_strFileName());
	}

	@Override
	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFileIndex(int fileIndex) {
		// TODO Auto-generated method stub
		
	}
	

}
