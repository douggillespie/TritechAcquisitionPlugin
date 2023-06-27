package tritechplugins.acquire.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamGridBagContraints;

public class TimeZonePanel {

	private JPanel mainPanel;
	
	private JComboBox<String> offlineTimeZone;
	
	private JButton defaultTimeZone;
	
	private String bigHint = "<html>"
			+ "Gemini data are collected in local time according to the PC data were collected on.<p>"
			+ "PAMGuard needs to convert the recorded times to UTC, which is standard across PAMGuard.<p>"
			+ "To do this, it needs to know the time zone that was used when data were collected.<p>"
			+ "Usually, this will be the Default. However, if you're exchanging data between people in<p>"
			+ "different countries, you will need to manually select the time zone used when data were collected.";
	
	public TimeZonePanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setToolTipText(bigHint);
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Time Zone for Gemini data files ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(defaultTimeZone = new JButton("Use Default"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		mainPanel.add(offlineTimeZone = new JComboBox<>(), c);
		fillTimeZones();
		offlineTimeZone.setToolTipText("<html>Time zone for conversion of Tritech file times to UTC. <p>"
				+ "This should be set to the time zone of the computer that collected the data.");
		defaultTimeZone.setToolTipText("Use PC default");
		defaultTimeZone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDefaultTimeZone();
			}
		});
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	private void fillTimeZones() {
		String[] zones = TimeZone.getAvailableIDs();	
		String tzStr;
		for (int i = 0; i < zones.length; i++) {
			TimeZone tz = TimeZone.getTimeZone(zones[i]);
//			offlineTimeZone.addItem(zones[i]);
//			offlineTimeZone.addItem(tz.getDisplayName(true, 0));
			tz = TimeZone.getTimeZone(zones[i]);
			if (tz.getRawOffset() < 0) {
				tzStr = String.format("UTC%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
			}
			else {
				tzStr = String.format("UTC+%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
			}
			offlineTimeZone.addItem(tzStr);
		}
	}
	
	public void setDefaultTimeZone() {
		TimeZone dtz = TimeZone.getDefault();
		if (dtz == null) {
			return;
		}
		setTimeZone(dtz.getID());
	}

	public void setTimeZone(String id) {
		String[] zones = TimeZone.getAvailableIDs();
		for (int i = 0; i < zones.length; i++) {
			if (zones[i].equals(id)) {
				offlineTimeZone.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public String getTimeZoneId() {
		String[] zones = TimeZone.getAvailableIDs();
		return zones[offlineTimeZone.getSelectedIndex()];
	}

}
