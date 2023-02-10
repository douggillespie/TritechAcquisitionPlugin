package tritechplugins.detect.veto.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.RemoveButton;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.WestAlignedPanel;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoManager;
import tritechplugins.detect.veto.SpatialVetoProvider;
import warnings.PamWarning;

public class VetoDialogPanel implements PamDialogPanel {

	private SpatialVetoManager vetoManager;
	
	private JPanel mainPanel;
	
	private JButton addButton;
	
	private JPanel currentPanel;

	private Window owner;

	public VetoDialogPanel(Window owner, SpatialVetoManager vetoManager) {
		this.owner = owner;
		this.vetoManager = vetoManager;
		mainPanel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new WestAlignedPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		topPanel.add(addButton = new JButton("Add Veto"), c);
		topPanel.setBorder(new TitledBorder("Spatial Vetos"));
		
		
		currentPanel = new JPanel(new GridBagLayout());
		currentPanel.setBorder(new TitledBorder("Current Vetos"));
		
		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, currentPanel);
		
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addVetoButton(e);
			}
		});
	}

	protected void addVetoButton(ActionEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		ArrayList<SpatialVetoProvider> providers = vetoManager.getVetoProviders();
		for (SpatialVetoProvider provider : providers) {
			JMenuItem menuItem = new JMenuItem(provider.getName());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addVeto(provider);
				}
			});
			popMenu.add(menuItem);
		}
		popMenu.show(addButton, addButton.getWidth()/2, addButton.getHeight()/2);
	}

	private void updateCurrentPanel() {
		currentPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		ArrayList<SpatialVeto> currentVetos = vetoManager.getCurrentVetos();
		for (SpatialVeto veto : currentVetos) {
			c.gridx = 0;
			JLabel label;
			currentPanel.add(label = new JLabel(veto.getName() + " "), c);
			label.setToolTipText(veto.toString());
			c.gridx++;
			JButton settingsButton = new PamSettingsIconButton();
			settingsButton.setToolTipText("Configure veto");
			currentPanel.add(settingsButton, c);
			settingsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editVeto(veto, label);
				}
			});
			c.gridx++;
			JButton removeButton = new RemoveButton();
			removeButton.setToolTipText("Remove veto");
			currentPanel.add(removeButton, c);
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeVeto(veto);
				}
			});
			c.gridy++;
		}
		currentPanel.invalidate();
		currentPanel.repaint();
		owner.pack();
	}

	protected void addVeto(SpatialVetoProvider provider) {
		vetoManager.addVeto(provider);
		updateCurrentPanel();
	}

	protected void removeVeto(SpatialVeto veto) {
		String str = String.format("Are you sure you want to remove the %s veto ?", veto.getName());
		int ans = WarnOnce.showWarning("Veto Removal", str, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			vetoManager.removeVeto(veto);
			updateCurrentPanel();
		}
	}

	protected void editVeto(SpatialVeto veto, JLabel label) {
		veto.configureVeto(owner);
		label.setToolTipText(veto.toString());
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		updateCurrentPanel();
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

}
