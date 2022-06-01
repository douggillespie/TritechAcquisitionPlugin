package tritechplugins.acquire.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import tritechplugins.acquire.JavaFileAcquisition;
import tritechplugins.acquire.JavaFileObserver;
import tritechplugins.acquire.JavaFileStatus;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.display.swing.SonarDisplayDecoration;

public class JavaFileStatusBar implements SonarDisplayDecoration, JavaFileObserver {
	
	private JPanel mainPanel, leftPanel;
	private JLabel fileNumber, fileName;
	private JComboBox<String> playSpeed;
	
	private double[] playSpeeds = TritechDaqParams.playSpeeds;
	
	private JavaFileAcquisition javaFileAcquisition;
	private TritechAcquisition tritechAcquisition;

	public JavaFileStatusBar(TritechAcquisition tritechAcquisition, JavaFileAcquisition javaFileAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		this.javaFileAcquisition = javaFileAcquisition;
		mainPanel = new PamPanel(new FlowLayout());
		leftPanel = new PamPanel(new BorderLayout());
		leftPanel.add(mainPanel, BorderLayout.WEST);
		mainPanel.add(fileNumber = new PamLabel(" File playback mode "));
		mainPanel.add(new PamLabel("  "));
		mainPanel.add(fileName = new PamLabel(" "));

		mainPanel.add(new PamLabel(" Play speed "));
		mainPanel.add(playSpeed = new JComboBox<>());
		int selInd = 0;
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		for (int i = 0; i < playSpeeds.length; i++) {
			if (playSpeeds[i] <= 0) {
				playSpeed.addItem("Free run");
			}
			else {
				playSpeed.addItem(String.format("x%3.1f", playSpeeds[i]));
			}
			if (params.getPlaySpeed() == playSpeeds[i]) {
				selInd = i;
			}
		}
		playSpeed.setSelectedIndex(selInd);
		playSpeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newPlaySpeed();
			}
		});
		
		javaFileAcquisition.addObserver(this);
		leftPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}

	protected void newPlaySpeed() {
		double speed = playSpeeds[playSpeed.getSelectedIndex()];
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		params.setPlaySpeed(speed);
	}

	@Override
	public JComponent getComponent() {
		return leftPanel;
	}

	@Override
	public void destroyComponent() {
		javaFileAcquisition.removeObserver(this);
	}

	@Override
	public void update(JavaFileStatus javaFileStatus) {
		if (javaFileStatus == null) {
			return;
		}
		fileNumber.setText(String.format("File %d of %d ", javaFileStatus.getCurrentFile()+1, javaFileStatus.getnFiles()));
		fileName.setText(javaFileStatus.getFileName());
	}

}
