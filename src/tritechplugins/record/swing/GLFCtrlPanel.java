package tritechplugins.record.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamButton;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;
import tritechplugins.record.BufferState;
import tritechplugins.record.GLFRecorderCtrl;
import tritechplugins.record.GLFRecorderParams;
import tritechplugins.record.GLFRecorderProcess;
import tritechplugins.record.StateObserver;

public class GLFCtrlPanel implements StateObserver {
	
	private JPanel mainPanel;
	private GLFRecorderCtrl recorderCtrl;
	private GLFRecorderProcess recorderProcess;
	
	private JProgressBar bufferStatus;
	private JButton start, startBuffer, stop;
	private JLabel status, file;

	public GLFCtrlPanel(GLFRecorderCtrl recorderCtrl, boolean useTimer) {
		this.recorderCtrl = recorderCtrl;
		this.recorderProcess = recorderCtrl.getRecorderProcess();
		mainPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints(null, 0, 0);
		c.ipadx = 0;
//		mainPanel.setBorder(new TitledBorder(recorderCtrl.getUnitName()));
		bufferStatus = new PamProgressBar(0, 100);
		status = new PamLabel("Status");
		file = new PamLabel("File");
		start = new SmallButton("Start");
		startBuffer = new SmallButton("Buffered");
		stop = new SmallButton("Stop");
		
		c.gridwidth = 3;
		mainPanel.add(new PamLabel("Buffer "), c);
		c.gridy++;
		mainPanel.add(bufferStatus, c);
		c.gridy++;
//		c.gridwidth = 1;
//		mainPanel.add(new PamLabel("Status: ", JLabel.RIGHT), c);
//		c.gridx++;
//		c.gridwidth = 3;
//		mainPanel.add(status, c);
//		c.gridy++;
//		c.gridwidth = 1;
		c.gridx = 0;
//		mainPanel.add(new PamLabel("File: ", JLabel.RIGHT), c);
//		c.gridx++;
		c.gridwidth = 3;
		mainPanel.add(file, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(start, c);
		c.gridx++;
		mainPanel.add(startBuffer, c);
		c.gridx++;
		mainPanel.add(stop, c);
		c.gridx++;
		
		start.setToolTipText("Start recording");
		startBuffer.setToolTipText("Start recording, grabbing buffer contents at start");
		stop.setToolTipText("Stop recording");
		
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startRecording();
			}
		});
		startBuffer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBufferedRecording();
			}
		});
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopRecording();
			}
		});
		
		if (useTimer && recorderCtrl.isViewer() == false) {
			Timer t = new Timer(1000, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					timerAction();
				}
			});
			t.start();
		}
	}
	
	private class SmallButton extends PamButton {

		public SmallButton(String text) {
			super(text);
			Insets insets = this.getInsets();
			insets.left = insets.top*2;
			insets.right = insets.top*2;
			this.setBorder(new EmptyBorder(insets));
		}
		
	}

	protected void timerAction() {
		updateState();
		enableControls();		
	}

	protected void stopRecording() {
		recorderProcess.stopRecording();
	}

	protected void startBufferedRecording() {
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		long start = PamCalendar.getTimeInMillis() - params.bufferSeconds * 1000;
		recorderProcess.startRecording(start);
	}

	protected void startRecording() {
		recorderProcess.startRecording();
	}
	
	public void updateState() {
		BufferState bufferState = recorderProcess.getBufferState();
		boolean recState = recorderProcess.getRecordState();
		bufferStatus.setValue((int) bufferState.getBufferPercent());
		bufferStatus.setString(String.format("%3.1fs", bufferState.getBufferedSeconds()));
		
//		status.setText(recState ? "Recording" : "Idle");
		
		
		File currFile = recorderProcess.getCurrentFile();
		if (currFile == null) {
			file.setText("Idle");
		}
		else {
			file.setText("File: " + currFile.getName());
		}
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	@Override
	public void notify(int state) {
		updateState();
		enableControls();
	}

	private void enableControls() {
		boolean recState = recorderProcess.getRecordState();
		start.setEnabled(recState == false);
		startBuffer.setEnabled(recState == false);
		stop.setEnabled(recState == true);
		
	}

}
