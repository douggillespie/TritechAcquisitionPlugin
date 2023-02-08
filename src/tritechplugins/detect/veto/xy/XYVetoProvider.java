package tritechplugins.detect.veto.xy;

import PamView.PanelOverlayDraw;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class XYVetoProvider implements SpatialVetoProvider {

	private XYOverlayDraw xyOverlayDraw;
	
	public XYVetoProvider() {
		xyOverlayDraw = new XYOverlayDraw(null);
	}

	@Override
	public String getName() {
		return "X,Y Box Veto";
	}

	@Override
	public SpatialVeto createVeto() {
		return new XYVeto(this);
	}

	@Override
	public PanelOverlayDraw getVetoOverlayDraw() {
		return xyOverlayDraw;
	}

}
