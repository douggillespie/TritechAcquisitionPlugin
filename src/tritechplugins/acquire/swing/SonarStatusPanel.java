package tritechplugins.acquire.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import geminisdk.structures.GemStatusPacket;
import tritechgemini.imagedata.GLFStatusData;
import tritechplugins.acquire.SonarStatusData;
import tritechplugins.acquire.TritechAcquisition;

/*
 * Status panel for a single sonar. 
 */
public class SonarStatusPanel {

	private TritechAcquisition tritechAcquisition;
	private int sonarId;
	private JPanel mainPanel;
	private InfoStrip ipStrip, subnetStrip, macStrip;
	private InfoStrip rxPackets;
	private InfoStrip pcipStrip;
	private InfoStrip firmware;
	private InfoStrip lastUpdate;
	

	public SonarStatusPanel(TritechAcquisition tritechAcquisition, int sonarId) {
		this.tritechAcquisition = tritechAcquisition;
		this.sonarId = sonarId;
		mainPanel = new PamPanel(new GridBagLayout());

//		mainPanel.setOpaque(false);
//		mainPanel.seto
		
		mainPanel.setBorder(new TitledBorder("Sonar " + sonarId));
		GridBagConstraints c = new PamGridBagContraints();
		lastUpdate = addInfoStrip("Last update", c);
		ipStrip = addInfoStrip("ip addr", c);
		pcipStrip = addInfoStrip("PC addr", c);
		subnetStrip = addInfoStrip("subnet", c);
		macStrip = addInfoStrip("MAC addr", c);
		rxPackets = addInfoStrip("RX Packets", c);
		firmware = addInfoStrip("Firmware", c);
		
//		Timer t = new Timer(5000, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				lastUpdate.setText(PamCalendar.formatDBDateTime(System.currentTimeMillis()));
//				ipStrip.setIpAddr(192);
//				
//			}
//		});
//		t.start();
	}

	public Component getComponent() {
		return mainPanel;
	}

	public void updateStatus(SonarStatusData sonarStatusData) {
		lastUpdate.setText(PamCalendar.formatDBDateTime(System.currentTimeMillis()));
		rxPackets.setText(String.format("%d", sonarStatusData.getTotalImages()));
		GLFStatusData statusPacket = sonarStatusData.getStatusPacket();
		ipStrip.setIpAddr(statusPacket.m_sonarAltIp);
		subnetStrip.setIpAddr(statusPacket.m_subNetMask);
		pcipStrip.setIpAddr(statusPacket.m_surfaceIp);
		String mac = String.format("%s:%s:%s", formatMacBit(statusPacket.m_macAddress1), formatMacBit(statusPacket.m_macAddress3), formatMacBit(statusPacket.m_macAddress3));
		macStrip.setText(mac);
		firmware.setText(String.format("0x%X", statusPacket.m_bfVersion));
	}
	
	private Object formatMacBit(short macBit) {
		int bit = Short.toUnsignedInt(macBit);
		return String.format("%02X:%02X", bit&0xFF, (bit>>8)&0xFF);
	}

	private InfoStrip addInfoStrip(String name, GridBagConstraints c) {
		InfoStrip infoStrip = new InfoStrip(name);
		c.gridx = 0;
		mainPanel.add(infoStrip.getNameLabel(), c);
		c.gridx++;
		mainPanel.add(infoStrip.getValueLabel(), c);
		c.gridy++;
		return infoStrip;
	}

}
