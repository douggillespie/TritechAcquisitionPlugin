package tritechplugins.detect.veto.rthi;

import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class RThiVetoParams extends SpatialVetoParams {

	private static final long serialVersionUID = 1L;
	
	protected double rangeMin, rangeMax, angleMin, angleMax;
	
	public RThiVetoParams(SpatialVetoProvider provider) {
		super(provider);
	}

}
