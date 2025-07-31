package tritechplugins.record.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.panel.PamPanel;
import tritechplugins.record.GLFRecorderCtrl;

public class GLFRecorderSidePanel implements PamSidePanel {

	private GLFRecorderCtrl glfRecorderCtrl;
	
	private JPanel mainPanel;
	
	private GLFCtrlPanel ctrlPanel;

	public GLFRecorderSidePanel(GLFRecorderCtrl glfRecorderCtrl) {
		super();
		this.glfRecorderCtrl = glfRecorderCtrl;
		mainPanel = new PamPanel(new BorderLayout());
		rename(glfRecorderCtrl.getUnitName());
		ctrlPanel = new GLFCtrlPanel(glfRecorderCtrl, true);
		mainPanel.add(BorderLayout.CENTER, ctrlPanel.getMainPanel());
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}
	
	@Override
	public void rename(String newName) {
		mainPanel.setBorder(new TitledBorder(newName));
	}

}
