package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechDaqParams;

public class SonarPositionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int sonarId;
	
	private JTextField x, y, head;
	private JTextField name;
	private JCheckBox flipLR;
	private SonarRemoveObserver removeObserver;

	public SonarPositionPanel(int sonarId, SonarRemoveObserver removeObserver) {
		this.sonarId = sonarId;
		this.removeObserver = removeObserver;
		this.setBorder(new TitledBorder("Sonar " + sonarId));
		setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		name = new JTextField(15);
		x = new JTextField(4);
		y = new JTextField(4);
		head = new JTextField(4);
		name.setToolTipText("Sonar name, e.g. 'Top', 'Far Bank' (display use only)");
		x.setToolTipText("x coordinate, relative to geo-reference position");
		y.setToolTipText("y coordinate, relative to geo-reference position");
		head.setToolTipText("Clockwise rotation relative to North");

		add(new JLabel("Name ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 6;
		add(name, c);
		
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy++;
		add(new JLabel("Relative (x,y) position ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		add(x, c);
		c.gridx++;
		add(y, c);
		c.gridx++;
		add(new JLabel( " m"), c);
		
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 1;
		add(new JLabel("Flip sonar", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 4;
		add(flipLR = new JCheckBox(" (upside down)"), c);
		
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy++;
		add(new JLabel("Rotation ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = 1;
		add(head, c);
		c.gridx++;
		c.gridwidth = 2;
		add(new JLabel( " degrees"), c);
		
		if (removeObserver != null) {
			JButton remBut = new JButton("Remove");
			c.gridy++;
			c.gridwidth = 2;
			add(remBut, c);
			remBut.setToolTipText("Remove sonar from list");
			remBut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeObserver.removeSonar(sonarId);
				}
			});
		}
		
		
	}

	public void setParams(TritechDaqParams daqParams) {
		SonarPosition sonarPos = daqParams.getSonarPosition(sonarId);
		name.setText(sonarPos.getSonarName());
		x.setText(String.format("%3.2f", sonarPos.getX()));
		y.setText(String.format("%3.2f", sonarPos.getY()));
		head.setText(String.format("%3.1f", sonarPos.getHead()));
		flipLR.setSelected(sonarPos.isFlipLR());
	}
	
	public boolean getParams(TritechDaqParams daqParams) {
		SonarPosition sonarPos = daqParams.getSonarPosition(sonarId);
		sonarPos.setSonarName(name.getText());
		sonarPos.setFlipLR(flipLR.isSelected());
		try {
			sonarPos.setX(Double.valueOf(x.getText()));
			sonarPos.setY(Double.valueOf(y.getText()));
			sonarPos.setHead(Double.valueOf(head.getText()));
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Invalid parameter", "Invalid parameter for sonar " + sonarId);
		}
		return true;
	}

	/**
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}

}
