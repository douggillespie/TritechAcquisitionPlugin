package tritechplugins.display.swing.overlays;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptionsPanel;
import PamView.symbol.SwingSymbolOptionsPanel;

public class SonarSymbolPanel extends StandardSymbolOptionsPanel {

	private SonarSymbolChooser sonarSymbolChooser;

	private JPanel mainPanel;
	
	private JRadioButton[] symbolTypes;

	public SonarSymbolPanel(StandardSymbolManager standardSymbolManager, SonarSymbolChooser sonarSymbolChooser) {
		super(standardSymbolManager, sonarSymbolChooser);
		this.sonarSymbolChooser = sonarSymbolChooser;
		
		int[] types = SonarSymbolOptions.getSymbolTypes();
		symbolTypes = new JRadioButton[types.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < types.length; i++) {
			symbolTypes[i] = new JRadioButton("Draw " + SonarSymbolOptions.getSymbolTypeString(types[i]));
			bg.add(symbolTypes[i]);
		}
		
		JPanel symbolPanel = new JPanel(new GridBagLayout());
		symbolPanel.setBorder(new TitledBorder("Symbol type"));
		GridBagConstraints c = new PamGridBagContraints();
		for (int i = 0; i < symbolTypes.length; i++) {
			symbolPanel.add(symbolTypes[i], c);
			c.gridy++;
		}
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, super.getDialogComponent());
		mainPanel.add(BorderLayout.SOUTH, symbolPanel);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		if (sonarSymbolChooser == null) {
			return;
		}
		SonarSymbolOptions params = sonarSymbolChooser.getSymbolOptions();
		int[] types = SonarSymbolOptions.getSymbolTypes();
		for (int i = 0; i < types.length; i++) {
			symbolTypes[i].setSelected(types[i] == params.symbolType);
		}
		super.setParams();
	}

	@Override
	public boolean getParams() {
		
		boolean ok = super.getParams();
		
		SonarSymbolOptions params = sonarSymbolChooser.getSymbolOptions();
		int[] types = SonarSymbolOptions.getSymbolTypes();
		for (int i = 0; i < types.length; i++) {
			if (symbolTypes[i].isSelected()) {
				params.symbolType = types[i];
				return ok;
			}
		}
		return false;
	}

}
