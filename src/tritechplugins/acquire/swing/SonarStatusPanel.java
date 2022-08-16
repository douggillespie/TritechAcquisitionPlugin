package tritechplugins.acquire.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
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
import tritechplugins.display.swing.SonarDisplayDecoration;

/*
 * Status panel for a single sonar. 
 */
public class SonarStatusPanel {

	private TritechAcquisition tritechAcquisition;
	private int sonarId;
	private JPanel mainPanel;
	private InfoStrip ipStrip;
	private InfoStrip linkquality;
	private InfoStrip rxPackets;
//	private InfoStrip pcipStrip, subnetStrip, macStrip;;
	private InfoStrip firmware;
	private InfoStrip lastUpdate;
	
	private long lastUpdateTime;
	private int totalFrameCount;
	

	public SonarStatusPanel(TritechAcquisition tritechAcquisition, int sonarId) {
		this.tritechAcquisition = tritechAcquisition;
		this.sonarId = sonarId;
		mainPanel = new PamPanel(new GridBagLayout());

//		mainPanel.setOpaque(false);
//		mainPanel.seto
		
		mainPanel.setBorder(new TitledBorder("Sonar " + sonarId));
		GridBagConstraints c = new PamGridBagContraints();
		lastUpdate = addInfoStrip("Last update", c, "Last received data time");
		ipStrip = addInfoStrip("ip addr", c, "Sonar IP address");
		linkquality = addInfoStrip("Link quality", c, "Link Quality");
//		pcipStrip = addInfoStrip("PC addr", c, null);
//		subnetStrip = addInfoStrip("subnet", c, null);
//		macStrip = addInfoStrip("MAC addr", c, null);
		rxPackets = addInfoStrip("Packets", c, "Received/Dropped/Resent/Lost");
		firmware = addInfoStrip("Firmware", c, "firmware version");
		
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

	public void updateStatus(SonarStatusData sonarStatusData) {
		lastUpdate.setText(PamCalendar.formatDBDateTime(System.currentTimeMillis()));
		GLFStatusData statusPacket = sonarStatusData.getStatusPacket();
		/*
		 * 
	public int m_packetCount;
	public int m_recvErrorCount;
	public int m_resentPacketCount;
	public int m_droppedPacketCount;
	public int m_unknownPacketCount;
		 */
		double badRate = 0;
		long now = System.currentTimeMillis();
		if (lastUpdateTime == 0) {
			lastUpdateTime = System.currentTimeMillis();
		}
		else {// if (now - lastUpdateTime > 100) {
			int newFrames = sonarStatusData.getTotalImages() - totalFrameCount;
//			if (newFrames < 2) {
				badRate = newFrames * 1000. / (double) (now-lastUpdateTime);
//			}
			totalFrameCount = sonarStatusData.getTotalImages();
			lastUpdateTime = now;
		}
		
		
		String pxtText = String.format("%d/%d/%d/%d", statusPacket.m_packetCount, statusPacket.m_droppedPacketCount,
				statusPacket.m_resentPacketCount, statusPacket.m_lostLineCount);
		rxPackets.setText(pxtText);
		ipStrip.setIpAddr(statusPacket.m_sonarAltIp);
		String lq;
		if (badRate < 2) {
			lq = String.format("%d%% ERROR: %3.0ffps", statusPacket.m_linkQuality, badRate);
		}
		else {
			lq = String.format("%d%%", statusPacket.m_linkQuality);
		}
		linkquality.setText(lq);
//		subnetStrip.setIpAddr(statusPacket.m_subNetMask);
//		pcipStrip.setIpAddr(statusPacket.m_surfaceIp);
		String mac = String.format("%s:%s:%s", formatMacBit(statusPacket.m_macAddress1), formatMacBit(statusPacket.m_macAddress3), formatMacBit(statusPacket.m_macAddress3));
//		macStrip.setText(mac);
		firmware.setText(String.format("0x%X", statusPacket.m_bfVersion));
	}
	
	private Object formatMacBit(short macBit) {
		int bit = Short.toUnsignedInt(macBit);
		return String.format("%02X:%02X", bit&0xFF, (bit>>8)&0xFF);
	}

	private InfoStrip addInfoStrip(String name, GridBagConstraints c, String toolTip) {
		InfoStrip infoStrip = new InfoStrip(name, toolTip);
		c.gridx = 0;
		mainPanel.add(infoStrip.getNameLabel(), c);
		c.gridx++;
		mainPanel.add(infoStrip.getValueLabel(), c);
		c.gridy++;
		return infoStrip;
	}

	public JComponent getComponent() {
		return mainPanel;
	}


}
