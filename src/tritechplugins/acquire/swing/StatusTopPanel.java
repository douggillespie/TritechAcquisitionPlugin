package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import geminisdk.OutputFileInfo;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqProcess;

public class StatusTopPanel implements SonarStatusObserver {

	private TritechAcquisition tritechAcquisition;
	private TritechDaqProcess tritechDaqProcess;
	private JPanel topPanel;
	private InfoStrip versionStrip;
	private InfoStrip errorStrip;
	private InfoStrip frameRate;
	private boolean haveVersion = false;

	public StatusTopPanel(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		
		topPanel = new PamPanel(new GridBagLayout());
		topPanel.setBorder(new TitledBorder("Sonar Status"));
		topPanel.setOpaque(false);
		GridBagConstraints c = new PamGridBagContraints();
		
		versionStrip = InfoStrip.addInfoStrip("svs5 Version", topPanel, c);
		errorStrip = InfoStrip.addInfoStrip("Error", topPanel, c);
		frameRate = InfoStrip.addInfoStrip("Frame Rate", topPanel, c);
		
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
	
	public void sayVersion() {
		String v = tritechDaqProcess.getLibVersion();
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
	public void updateFrameRate(int frameRate) {
		this.frameRate.setText(String.format("%d fps", frameRate));
	}

	@Override
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo) {
		errorStrip.setName("File");
		errorStrip.setText(outputFileInfo.getM_strFileName());
	}
	

}
