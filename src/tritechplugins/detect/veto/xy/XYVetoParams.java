package tritechplugins.detect.veto.xy;

import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class XYVetoParams extends SpatialVetoParams {

	private static final long serialVersionUID = 1L;
	
	public double xMin, xMax, yMin, yMax;
	
	public XYVetoParams(SpatialVetoProvider provider) {
		super(provider);
	}

}
