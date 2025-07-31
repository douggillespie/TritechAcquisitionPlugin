package tritechplugins.record.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamController.PamController;
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

	private HashMap<String, GLFTriggerData> currentTriggers;

	public GLFTriggersPanel(GLFRecorderCtrl recorderControl, Window parent) {
		this.recorderControl = recorderControl;
		this.parent = parent;
		mainPanel = new JPanel(new BorderLayout());
		trigsPanel = new JPanel();
		trigsPanel.setLayout(new BoxLayout(trigsPanel, BoxLayout.Y_AXIS));
		mainPanel.add(BorderLayout.CENTER, trigsPanel);
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
		Collection<GLFTriggerData> current = currentTriggers.values();
		for (GLFTriggerData aTrig : current) {
			JMenuItem menuItem = new JMenuItem(aTrig.triggerDataName);
			menuItem.addActionListener(new RemoveAction(aTrig));
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
			// TODO Auto-generated method stub
			
		}
	}
	private class RemoveAction implements ActionListener {
		
		GLFTriggerData triggerData;

		public RemoveAction(GLFTriggerData triggerData) {
			super();
			this.triggerData = triggerData;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setParams(GLFRecorderParams recorderParams) {
		currentTriggers = recorderParams.getTriggerDataHash();
		rebuild();

		enableControls();
	}

	public boolean getParams(GLFRecorderParams recorderParams) {

		return true;
	}

	public void rebuild() {


		parent.pack();
	}

	private void enableControls() {
		ArrayList<PamDataBlock> newBlocks = getNewTriggerBlocks();
		addButton.setEnabled(newBlocks.size() > 0);
		removeButton.setEnabled(currentTriggers.isEmpty() == false);
	}

	/**
	 * List of all possible trigger data blocks. 
	 * @return
	 */
	private ArrayList<PamDataBlock> getTriggerBlocks() {
		return PamController.getInstance().getDataBlocks();
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
			if (currentTriggers.get(aBlock.getLoggingName()) != null) {
				it.remove();
			}
		}
		return blocks;
	}

}
