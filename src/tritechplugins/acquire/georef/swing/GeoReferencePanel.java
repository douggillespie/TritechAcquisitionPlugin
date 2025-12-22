package tritechplugins.acquire.georef.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamUtils.LatLongDialogStrip;
import PamView.dialog.PamGridBagContraints;
import tritechplugins.acquire.georef.GeoRefParams;
import tritechplugins.acquire.georef.GeoRefType;
import tritechplugins.acquire.georef.GeoReference;

public class GeoReferencePanel extends JPanel {

	public static final long serialVersionUID = 1L;

	private JComboBox<GeoRefType> referenceType;
	
	private LatLongDialogStrip latitude, longitude;
	
	public GeoReferencePanel() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		referenceType = new JComboBox<>();
		GeoRefType[] types = GeoRefType.values();
		for (int i = 0; i < types.length; i++) {
			referenceType.addItem(types[i]);
		}
		latitude = new LatLongDialogStrip(true);
		longitude = new LatLongDialogStrip(false);
		
		add(new JLabel("Reference type: ", JLabel.RIGHT), c);
		c.gridx++;
		add(referenceType);
		c.gridx = 0;
		c.gridy++;
		add(new JLabel("Latitude: ", JLabel.RIGHT), c);
		c.gridx++;
		add(latitude, c);

		c.gridx = 0;
		c.gridy++;
		add(new JLabel("Longitude: ", JLabel.RIGHT), c);
		c.gridx++;
		add(longitude, c);
		
		
	}

	public void setParams(GeoRefParams geoRefParams) {
		
	}
	
	public boolean getParams(GeoRefParams geoRefParams) {
		
		return true;
	}

}
