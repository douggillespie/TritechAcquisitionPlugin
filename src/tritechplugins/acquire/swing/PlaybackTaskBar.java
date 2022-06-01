package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import PamView.dialog.PamLabel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import geminisdk.OutputFileInfo;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.SonarStatusObserver;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechJNAPlayback;
import tritechplugins.display.swing.GeminiTaskBar;

public class PlaybackTaskBar implements GeminiTaskBar, SonarStatusObserver {

	public JPanel mainPanel;
	private TritechAcquisition tritechAcquisition;
	private JLabel currentFile;
	private JLabel percentProcessed; 
	private JComboBox<String> playSpeed;
	private PamAlignmentPanel leftAlighnedPanel;
	private TritechJNAPlayback jnaPlayback;
	private double[] playSpeeds;
	
	public PlaybackTaskBar(TritechAcquisition tritechAcquisition, TritechJNAPlayback jnaPlayback) {
		this.tritechAcquisition = tritechAcquisition;
		this.jnaPlayback = jnaPlayback;
		mainPanel = new PamPanel(new FlowLayout());
		leftAlighnedPanel = new PamAlignmentPanel(mainPanel, BorderLayout.WEST);
		leftAlighnedPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		mainPanel.add(new PamLabel("File playback: play speed "));
		mainPanel.add(playSpeed = new JComboBox<String>());
		mainPanel.add(new PamLabel(" current file "));
		mainPanel.add(currentFile = new JLabel());
		mainPanel.add(percentProcessed = new JLabel());
		playSpeeds = TritechDaqParams.playSpeeds;
		for (int i = 0; i < playSpeeds.length; i++) {
			double speed = playSpeeds[i];
			if (i == 0) {
				playSpeed.addItem("Free run");
			}
			else {
				playSpeed.addItem(String.format("x%3.1f", speed));
			}
		}
		playSpeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newPlaySpeed();
			}
		});
		tritechAcquisition.getTritechDaqProcess().addStatusObserver(this);
		
		setParams();
	}

	private void setParams() {
		double selSpeed = tritechAcquisition.getDaqParams().getPlaySpeed();
		for (int i = 0; i < playSpeeds.length; i++) {
			if (playSpeeds[i] == selSpeed) {
				playSpeed.setSelectedIndex(i);
			}
		}
	}

	protected void newPlaySpeed() {
		int ind = playSpeed.getSelectedIndex();
		if (ind < 0) {
			return;
		}
		double speed = playSpeeds[ind];
		tritechAcquisition.getDaqParams().setPlaySpeed(speed);
		jnaPlayback.setPlaybackSpeed(speed);
	}

	@Override
	public JComponent getComponent() {
		return leftAlighnedPanel;
	}

	@Override
	public void closeTaskBar() {
		tritechAcquisition.getTritechDaqProcess().removeStatusObserver(this);
	}

	@Override
	public void updateStatus(SonarStatusData sonarStatusData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errorMessage(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFrameRate(int frameRate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate) {
		if (loggerPlaybackUpdate == null) {
			return;
		}
		percentProcessed.setText(String.format("File %d frame %d, %d%%", loggerPlaybackUpdate.m_uiNumberOfFiles, 
				loggerPlaybackUpdate.m_uiNumberOfRecords, loggerPlaybackUpdate.m_uiPercentProcessed));
		if (loggerPlaybackUpdate.fileNames.size() > 0) {
			currentFile.setText(loggerPlaybackUpdate.fileNames.get(0));
		}
	}

	@Override
	public void updateFileIndex(int fileIndex) {
		// TODO Auto-generated method stub
		
	}

}
