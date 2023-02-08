package tritechplugins.detect.veto.rthi;

import PamView.PanelOverlayDraw;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class RThiVetoProvider implements SpatialVetoProvider {

	private RThiOverlayDraw rThiOverlayDraw;
	
	public RThiVetoProvider() {
		rThiOverlayDraw = new RThiOverlayDraw(null);
	}

	@Override
	public String getName() {
		return "Range/Angle Veto";
	}

	@Override
	public SpatialVeto createVeto() {
		return new RThiVeto(this);
	}

	@Override
	public PanelOverlayDraw getVetoOverlayDraw() {
		return rThiOverlayDraw;
	}

}
