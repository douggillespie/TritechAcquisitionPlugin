package tritechplugins.acquire.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import geminisdk.structures.GemStatusPacket;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.TritechAcquisition;

/*
 * Status panel for a single sonar. 
 */
public class SonarStatusPanel {

	private TritechAcquisition tritechAcquisition;
	private int sonarId;
	private JPanel mainPanel;
	private InfoStrip ipStrip;
	private InfoStrip rxPackets;
	

	public SonarStatusPanel(TritechAcquisition tritechAcquisition, int sonarId) {
		this.tritechAcquisition = tritechAcquisition;
		this.sonarId = sonarId;
		mainPanel = new PamPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Sonar " + sonarId));
		GridBagConstraints c = new PamGridBagContraints();
		ipStrip = addInfoStrip("ip addr", c);
		rxPackets = addInfoStrip("RX Packets", c);
		ipStrip.setIpAddr(45);
	}

	public Component getComponent() {
		return mainPanel;
	}

	public void updateStatus(SonarStatusData sonarStatusData) {
		rxPackets.setText(String.format("%d", sonarStatusData.getTotalImages()));
		GemStatusPacket statusPacket = sonarStatusData.getStatusPacket();
		ipStrip.setIpAddr(statusPacket.m_sonarFixIp);
	}
	
	private InfoStrip addInfoStrip(String name, GridBagConstraints c) {
		InfoStrip infoStrip = new InfoStrip(name);
		c.gridx = 0;
		mainPanel.add(new PamLabel(name + ": ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(infoStrip.value);
		c.gridy++;
		return infoStrip;
	}
	
	private class InfoStrip {
		
		private String name;
		
		private PamLabel value;

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
		
	}

}
