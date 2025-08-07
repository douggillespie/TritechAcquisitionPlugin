package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.ScrollingPamLabel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import geminisdk.OutputFileInfo;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechplugins.acquire.ConfigurationObserver;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqProcess;

public class TritechSidePanel implements PamSidePanel, ConfigurationObserver, SonarStatusObserver {
	
	private JPanel mainPanel;
	private TritechAcquisition tritechAcquisition;
	
	private JCheckBox logGLF;
	private ScrollingPamLabel glfFile;
	private TritechDaqProcess tritechDaqProcess;

	public TritechSidePanel(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.tritechDaqProcess = tritechAcquisition.getTritechDaqProcess();
		mainPanel = new PamPanel(new BorderLayout());
		JPanel inPanel = new PamPanel(new GridBagLayout());
		mainPanel.add(inPanel, BorderLayout.WEST);
//		mainPanel.setLayout(new GridBagLayout());
		rename(tritechAcquisition.getUnitName());
		logGLF = new PamCheckBox("Log data to GLF Files");
		glfFile = new ScrollingPamLabel(25, " ");
		logGLF.setToolTipText("Write all sonar data to GLF Files using the internal Tritech writer");
		glfFile.setToolTipText("Current log file name");
		GridBagConstraints c = new PamGridBagContraints(null, 0, 0);
		inPanel.add(logGLF, c);
		c.gridy++;
		inPanel.add(glfFile, c);
		tritechAcquisition.addConfigurationObserver(this);
		tritechAcquisition.getTritechDaqProcess().addStatusObserver(this);
		
		enableControls();
		setData();
		
		logGLF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logGLFAction();
			}
		});
	}

	protected void logGLFAction() {
		boolean rec = logGLF.isSelected();
		tritechAcquisition.getDaqParams().setStoreGLFFiles(rec);
		// not needed since it will come through as a configuration notification
//		tritechAcquisition.getTritechDaqProcess().setGLFLogging();
		tritechAcquisition.notifyConfigurationObservers();
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		mainPanel.setBorder(new TitledBorder(newName));
	}

	@Override
	public void configurationChanged() {
		enableControls();
		setData();
	}

	private void enableControls() {
		boolean en = tritechAcquisition.getDaqParams().getRunMode() == TritechDaqParams.RUN_ACQUIRE;
		logGLF.setEnabled(en);
	}
	private void setData() {
		logGLF.setSelected(tritechAcquisition.getDaqParams().isStoreGLFFiles());
		if (tritechDaqProcess.shouldLogging() == false) {
			updateOutputFileInfo(null);
		}
	}

	@Override
	public void updateStatus(SonarStatusData sonarStatusData) {
		if (tritechDaqProcess.shouldLogging() == false) {
			updateOutputFileInfo(null);
		}
	}

	@Override
	public void errorMessage(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateQueueSize(int queueSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFrameRate(int frameRate, double trueFPS) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo) {
		if (outputFileInfo == null || tritechDaqProcess.shouldLogging() == false) {
			glfFile.setText("Not logging");
		}
		else {
			if (logGLF.isSelected() == false) {
				System.out.println("After stop");
			}
			File aFile = new File(outputFileInfo.getM_strFileName());
			glfFile.setText(aFile.getName());
		}
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
