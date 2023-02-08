package tritechplugins.detect.veto.swing;

import java.awt.Rectangle;

import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoProvider;
import tritechplugins.display.swing.SonarXYProjector;

/*
 * Drawing class for specific veto. Subset of functionality of PanelOverlayDraw
 * since most funcitonality is already handled in VeoOverlayDraw
 */
public abstract class VetoXYDraw {

	private SpatialVetoProvider spatialVetoProvider;

	public VetoXYDraw(SpatialVetoProvider spatialVetoProvider) {
		this.spatialVetoProvider = spatialVetoProvider;
	}

	public abstract Rectangle drawVeto(SpatialVeto spatialVeto, SonarXYProjector xyProjector);

	/**
	 * @return the spatialVetoProvider
	 */
	public SpatialVetoProvider getSpatialVetoProvider() {
		return spatialVetoProvider;
	}
	
}
