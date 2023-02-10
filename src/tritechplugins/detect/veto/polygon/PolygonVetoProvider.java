package tritechplugins.detect.veto.polygon;

import PamView.PanelOverlayDraw;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class PolygonVetoProvider implements SpatialVetoProvider {

	private PolygonOverlayDraw polygonOverlayDraw;
	
	public PolygonVetoProvider() {
		this.polygonOverlayDraw = new PolygonOverlayDraw(null);
	}

	@Override
	public String getName() {
		return "Polygon Veto";
	}

	@Override
	public SpatialVeto createVeto() {
		return new PolygonVeto(this);
	}

	@Override
	public PanelOverlayDraw getVetoOverlayDraw() {
		return polygonOverlayDraw;
	}

}
