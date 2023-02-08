package tritechplugins.detect.veto.circle;

import PamView.PanelOverlayDraw;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class CircleVetoProvider implements SpatialVetoProvider {

	private CircleOverlayDraw circleOverlayDraw;
	
	public CircleVetoProvider() {
		circleOverlayDraw = new CircleOverlayDraw();
	}

	@Override
	public String getName() {
		return "Circle / round veto";
	}

	@Override
	public SpatialVeto createVeto() {
		return new CircleVeto(this);
	}

	@Override
	public PanelOverlayDraw getVetoOverlayDraw() {
		return circleOverlayDraw;
	}

}
