package tritechplugins.record.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.record.GLFRecorderParams;

public class GLFRecorderDialog extends PamDialog {
	
	private SourcePanel sourcePanel;

	private GLFRecorderParams recorderParams;
	
	private SelectFolder outputFolder;
	
	private JTextField bufferSeconds, maxFileSize;
	
	private JRadioButton initialStart, initialIdle;
	
	private static GLFRecorderDialog singleInstance;

	public GLFRecorderDialog(Window parentFrame) {
		super(parentFrame, "GLF Recorder", true);
		
		JPanel mainPanel = new JPanel();
//		mainPanel.setBorder(new TitledBorder("GLF Recorder"));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// source data
		sourcePanel = new SourcePanel(this, "Sonar Image data source", ImageDataUnit.class, false, true);
		mainPanel.add(sourcePanel.getPanel());
		// output folder
		outputFolder = new SelectFolder("Output folder", 60);
		JPanel fPan = outputFolder.getFolderPanel();
		fPan.setBorder(new TitledBorder("Output Folder"));
		mainPanel.add(fPan);
		// other params
		PamAlignmentPanel pPanel = new PamAlignmentPanel(BorderLayout.WEST);
		pPanel.setLayout( new GridBagLayout());
		pPanel.setBorder(new TitledBorder("Options"));
		GridBagConstraints c = new PamGridBagContraints();
		pPanel.add(new JLabel("Buffer length ", JLabel.RIGHT), c);
		c.gridx++;
		pPanel.add(bufferSeconds = new JTextField(4), c);
		c.gridx++;
		pPanel.add(new JLabel(" seconds"), c);
		c.gridx = 0;
		c.gridy++;
		pPanel.add(new JLabel("Max file size ", JLabel.RIGHT), c);
		c.gridx++;
		pPanel.add(maxFileSize = new JTextField(4), c);
		c.gridx++;
		pPanel.add(new JLabel(" Megabytes"), c);

		c.gridx = 0;
		c.gridy++;
		pPanel.add(new JLabel("Initial State ", JLabel.RIGHT), c);
		c.gridx++;
		pPanel.add(initialStart = new JRadioButton("Start"), c);
		c.gridx++;
		pPanel.add(initialIdle = new JRadioButton("Remain idle"), c);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(initialIdle);
		bg.add(initialStart);
		mainPanel.add(pPanel);
		
		
		setDialogComponent(mainPanel);
	}

	public static GLFRecorderParams showDialog(Window parentFrame, GLFRecorderParams recorderParams) {
//		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new GLFRecorderDialog(parentFrame);
//		}
		singleInstance.setParams(recorderParams);
		singleInstance.setVisible(true);
		return singleInstance.recorderParams;
	}
	
	private void setParams(GLFRecorderParams recorderParams) {
		this.recorderParams = recorderParams;
		sourcePanel.setSource(recorderParams.imageDataSource);
		outputFolder.setFolderName(recorderParams.outputFolder);
		bufferSeconds.setText(String.format("%d", recorderParams.bufferSeconds));
		maxFileSize.setText(String.format("%d", recorderParams.maxSizeMegabytes));
		initialIdle.setSelected(recorderParams.initialState == GLFRecorderParams.START_IDLE);
		initialStart.setSelected(recorderParams.initialState == GLFRecorderParams.START_RECORD);
	}

	@Override
	public boolean getParams() {
		PamDataBlock source = sourcePanel.getSource();
		if (source == null) {
			return showWarning("No image data source selected");
		}
		recorderParams.imageDataSource = source.getLongDataName();
		
		String ofold = outputFolder.getFolderName(true);
		if (ofold == null) {
			return false;
		}
		recorderParams.outputFolder = ofold;
		
		try {
			recorderParams.bufferSeconds = Integer.valueOf(bufferSeconds.getText());
			recorderParams.maxSizeMegabytes = Integer.valueOf(maxFileSize.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter. Buffer length and file size must be integer");
		}
		if (initialStart.isSelected()) {
			recorderParams.initialState = GLFRecorderParams.START_RECORD;
		}
		else {
			recorderParams.initialState = GLFRecorderParams.START_IDLE;
		}
		
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		recorderParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new GLFRecorderParams());
	}

}
