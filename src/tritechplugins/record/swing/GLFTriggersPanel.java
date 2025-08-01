package tritechplugins.record.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamDetection.PamDetection;
import PamView.dialog.PamDialog;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import tritechplugins.record.GLFRecorderCtrl;
import tritechplugins.record.GLFRecorderParams;
import tritechplugins.record.GLFTriggerData;

/**
 * Panel for displaying info on multiple GLF Record triggers. 
 * @author dg50
 *
 */
public class GLFTriggersPanel {

	private JPanel mainPanel;
	private JPanel trigsPanel;
	private GLFRecorderCtrl recorderControl;
	private JButton addButton, removeButton;
	private Window parent;
	private GLFRecorderParams recorderParams;

//	private HashMap<String, GLFTriggerData> currentTriggers;

	public GLFTriggersPanel(GLFRecorderCtrl recorderControl, Window parent) {
		this.recorderControl = recorderControl;
		this.parent = parent;
		mainPanel = new JPanel(new BorderLayout());
//		mainPanel = new PamAlignmentPanel(BorderLayout.NORTH);
//		mainPanl
//		trigsPanel = new PamAlignmentPanel(BorderLayout.NORTH);
		trigsPanel = new JPanel(new BorderLayout());
		trigsPanel.setLayout(new BoxLayout(trigsPanel, BoxLayout.Y_AXIS));
		JPanel trigOuter = new PamAlignmentPanel(trigsPanel, BorderLayout.NORTH);
		
		mainPanel.add(BorderLayout.CENTER, trigOuter);
		
		JPanel ctrlPanel = new JPanel(new FlowLayout());
		addButton = new JButton("Add trigger");
		removeButton = new JButton("Remove trigger");
		ctrlPanel.add(addButton);
		ctrlPanel.add(removeButton);
		mainPanel.add(BorderLayout.SOUTH, ctrlPanel);
		
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addButtonPress(e);
			}
		});
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeButtonPress(e);
			}
		});
		
		addButton.setToolTipText("Add new record trigger");
		removeButton.setToolTipText("Remove a record trigger");

	}

	protected void addButtonPress(ActionEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		ArrayList<PamDataBlock> newBlocks = getNewTriggerBlocks();
		for (PamDataBlock aBlock : newBlocks) {
			JMenuItem menuItem = new JMenuItem(aBlock.getLongDataName());
			menuItem.addActionListener(new AddAction(aBlock));
			popMenu.add(menuItem);
		}
		popMenu.show(addButton, addButton.getWidth()/2, addButton.getHeight()/2);
	}

	protected void removeButtonPress(ActionEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		Set<String> keys = recorderParams.getTriggerHashKeys();
		for (String setName : keys) {
			JMenuItem menuItem = new JMenuItem(setName);
			menuItem.addActionListener(new RemoveAction(setName));
			popMenu.add(menuItem);
		}

		popMenu.show(removeButton, removeButton.getWidth()/2, removeButton.getHeight()/2);
		
	}
	
	private class AddAction implements ActionListener {
		
		PamDataBlock dataBlock;

		public AddAction(PamDataBlock dataBlock) {
			super();
			this.dataBlock = dataBlock;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			addTriggerPane(dataBlock);
		}
	}
	
	private class RemoveAction implements ActionListener {
		
		String setName;

		public RemoveAction(String setName) {
			super();
			this.setName = setName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			removeTriggerPane(setName);
		}
	}
	
	private void addTriggerPane(PamDataBlock dataBlock) {
		GLFTriggerPane trigPane = new GLFTriggerPane(parent, dataBlock);
		trigsPanel.add(trigPane);
		GLFTriggerData trigData = recorderParams.getTriggerData(dataBlock, true);
		trigPane.setParams(trigData);
		parent.pack();
		enableControls();
	}

	public void removeTriggerPane(String setName) {
		GLFTriggerPane trigPane = findTriggerPane(setName);
		if (trigPane != null) {
			trigsPanel.remove(trigPane);
			recorderParams.removeTriggerData(setName);
			parent.pack();
		}
		trigsPanel.invalidate();
		trigsPanel.repaint();
		parent.pack();
		enableControls();
	}

	/**
	 * find a trigger pane by name
	 * @param longName
	 * @return
	 */
	private GLFTriggerPane findTriggerPane(String longName) {
		int n = trigsPanel.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component comp = trigsPanel.getComponent(i);
			if (comp instanceof GLFTriggerPane) {
				GLFTriggerPane trigPane = (GLFTriggerPane) comp;
				if (trigPane.getLongName().equals(longName)) {
					return trigPane;
				}
			}
		}
		return null;
	}
	
	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setParams(GLFRecorderParams recorderParams) {
//		currentTriggers = recorderParams.getTriggerDataHash();
		this.recorderParams = recorderParams;
		rebuild();

		enableControls();
	}

	public boolean getParams(GLFRecorderParams recorderParams) {
		int n = trigsPanel.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component comp = trigsPanel.getComponent(i);
			if (comp instanceof GLFTriggerPane) {
				GLFTriggerPane trigPane = (GLFTriggerPane) comp;
				GLFTriggerData trigParams = recorderParams.getTriggerData(trigPane.getDataBlock(), true);
				boolean ans = trigPane.getParams(trigParams);
				if (ans) {
					recorderParams.setTrigerData(trigPane.getDataBlock(), trigParams);
				}
				else {
					return PamDialog.showWarning(parent, "Invalid trigger params", trigPane.getLongName());
				}
			}
		}
		return true;
	}

	public void rebuild() {
		trigsPanel.removeAll();
		
		Set<String> keys = recorderParams.getTriggerHashKeys();
		for (String aKey : keys) {
			PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(aKey);
			if (dataBlock == null) {
				continue;
			}
			GLFTriggerPane trigPane = new GLFTriggerPane(parent, dataBlock);
			trigPane.setParams(recorderParams.getTriggerData(dataBlock, true));
			trigsPanel.add(trigPane);
		}
		
		
		parent.pack();
	}

	private void enableControls() {
		ArrayList<PamDataBlock> newBlocks = getNewTriggerBlocks();
		addButton.setEnabled(newBlocks.size() > 0);
		int  n = trigsPanel.getComponentCount();
		removeButton.setEnabled(n > 0);
	}

	/**
	 * List of all possible trigger data blocks. 
	 * @return
	 */
	private ArrayList<PamDataBlock> getTriggerBlocks() {
		return PamController.getInstance().getDataBlocks(PamDetection.class, true);
	}

	/**
	 * List of data blocks not already set up as a trigger. 
	 * @return
	 */
	private ArrayList<PamDataBlock> getNewTriggerBlocks() {
		ArrayList<PamDataBlock> blocks = getTriggerBlocks();
		ListIterator<PamDataBlock> it = blocks.listIterator();
		while (it.hasNext()) {
			PamDataBlock aBlock = it.next();
			if (recorderParams.getTriggerData(aBlock, false) != null) {
				it.remove();
			}
		}
		return blocks;
	}

}
