package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.dialog.PamLabel;

class InfoStrip {
	
	private PamLabel nameLabel;
	
	private PamLabel valueLabel;

	public InfoStrip(String name, String toolTip) {
		super();
		this.nameLabel = new PamLabel(formatName(name), PamLabel.RIGHT);
		valueLabel = new PamLabel();
		if (toolTip != null) {
			nameLabel.setToolTipText(toolTip);
			valueLabel.setToolTipText(toolTip);
		}
	}
	
	public String formatName(String bareName) {
		return bareName + ": ";
	}
	
	public void setText(String text) {
		valueLabel.setText(text);
	}
	
	public void setIpAddr(int ipAddr) {
		int[] bytes = new int[4];
		long asLong = Integer.toUnsignedLong(ipAddr);
		for (int i = 0; i < 4; i++) {
			bytes[i] = (int) (asLong & 0xFF);
			asLong >>= 8;
		}
		setText(String.format("%03d.%03d.%03d.%03d", bytes[0], bytes[1], bytes[2], bytes[3]));
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return nameLabel.getText();
	}

	/**
	 * Create and add an info strip using gridbaglayout. 
	 * @param name name to display 
	 * @param component parent component (using Gridbaglayout)
	 * @param c current constraing. 
	 * @return InfoStrip object. 
	 */
	public static InfoStrip addInfoStrip(String name, JComponent component, GridBagConstraints c) {
		InfoStrip iStrip = new InfoStrip(name, null);
		c.gridx = 0;
		component.add(iStrip.nameLabel = new PamLabel(name + ": ", JLabel.RIGHT), c);
		c.gridx++;
		component.add(iStrip.valueLabel, c);
		c.gridy++;
		return iStrip;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		nameLabel.setText(formatName(name));
	}

	/**
	 * @return the nameLabel
	 */
	public PamLabel getNameLabel() {
		return nameLabel;
	}

	/**
	 * @return the valueLabel
	 */
	public PamLabel getValueLabel() {
		return valueLabel;
	}
	
}