package tritechplugins.record.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import tritechplugins.record.GLFRecorderCtrl;
import tritechplugins.record.GLFTriggerData;

/**
 * Pane for displaying trigger information for a specified trigger. 
 * @author dg50
 *
 */
public class GLFTriggerPane extends JPanel {
		
	private JTextField preSeconds, postSeconds;
	
	private JCheckBox active;
	
	private PamSettingsIconButton dataSelect;

	private PamDataBlock dataBlock;

	private Window parentWin;

	public GLFTriggerPane(Window parentWin, PamDataBlock dataBlock) {
		super(new BorderLayout());
		this.parentWin = parentWin;
		this.dataBlock = dataBlock;
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel = new PamAlignmentPanel(BorderLayout.WEST);
		mainPanel.setLayout(new GridBagLayout());;//
		this.setBorder(new TitledBorder(dataBlock.getLongDataName()));
		GridBagConstraints c = new PamGridBagContraints();
		active = new JCheckBox("Activate");
		preSeconds = new JTextField(2);
		postSeconds = new JTextField(2);
		DataSelector ds = dataBlock.getDataSelector(GLFRecorderCtrl.DATASELECTNAME, false);
		
		mainPanel.add(active, c);
//		c.gridx = 3;
		
//		c.gridy++;
		c.gridx++;
		mainPanel.add(new JLabel("Pre seconds: "), c);
		c.gridx++;
		mainPanel.add(preSeconds, c);
		c.gridx++;
		mainPanel.add(new JLabel(",    post seconds: "), c);
		c.gridx++;
		mainPanel.add(postSeconds, c);
		if (ds != null) {
			c.gridx++;
			mainPanel.add(new JLabel("    "), c);
			c.gridx++;
			mainPanel.add(ds.getDialogButton(parentWin), c);
		}
		
		active.setToolTipText("Trigger active");
		preSeconds.setToolTipText("Seconds of buffered data to record before the trigger");
		postSeconds.setToolTipText("Seconds of buffered data to record after the trigger");
		
		this.add(BorderLayout.CENTER, mainPanel);
	}

	public void setParams(GLFTriggerData trigData) {
		active.setSelected(trigData.enabled);
		preSeconds.setText(String.format("%d", trigData.preSeconds));
		postSeconds.setText(String.format("%d", trigData.postSeconds));
		
	}
	
	public boolean getParams(GLFTriggerData trigData) {
		trigData.enabled = active.isSelected();
		try {
			trigData.preSeconds = Integer.valueOf(preSeconds.getText());
			trigData.postSeconds = Integer.valueOf(postSeconds.getText());
		}
		catch (NumberFormatException e) {
			if (trigData.enabled) {
				return PamDialog.showWarning(null, "Invalid parameter", "pre and post sampling times must be integer values");
			}
		}
		return true;
	}
	
	public String getLongName() {
		return dataBlock.getLongDataName();
	}

	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

}
