package tritechplugins.detect.veto.circle;

import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class CircleVetoParams extends SpatialVetoParams {

	private static final long serialVersionUID = 1L;
	
	public double centreX, centreY, radius = 10;
	/**
	 * Veto is inside the circle, otherwise keep inside and reject outside. 
	 */
	public boolean vetoInside = true;

	public CircleVetoParams(SpatialVetoProvider provider) {
		super(provider);
	}

}
