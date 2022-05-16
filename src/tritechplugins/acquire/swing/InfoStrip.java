package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.dialog.PamLabel;

class InfoStrip {
	
	private String name;
	
	PamLabel value;

	public InfoStrip(String name) {
		super();
		this.name = name;
		value = new PamLabel();
	}
	
	public void setText(String text) {
		value.setText(text);
	}
	
	public void setIpAddr(int ipAddr) {
		int[] bytes = new int[4];
		long asLong = Integer.toUnsignedLong(ipAddr);
		for (int i = 0; i < 4; i++) {
			bytes[i] = (int) (asLong & 0xFF);
			asLong >>= 8;
		}
		setText(String.format("%03d.%03d.%03d.%03d", bytes[3], bytes[2], bytes[1], bytes[0]));
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Create and add an info strip using gridbaglayout. 
	 * @param name name to display 
	 * @param component parent component (using Gridbaglayout)
	 * @param c current constraing. 
	 * @return InfoStrip object. 
	 */
	public static InfoStrip addInfoStrip(String name, JComponent component, GridBagConstraints c) {
		InfoStrip iStrip = new InfoStrip(name);
		c.gridx = 0;
		component.add(new PamLabel(name + ": ", JLabel.RIGHT), c);
		c.gridx++;
		component.add(iStrip.value, c);
		c.gridy++;
		return iStrip;
	}
	
}