package tritechplugins.detect.veto;

public class RThiVetoParams extends SpatialVetoParams {

	private static final long serialVersionUID = 1L;
	
	protected double rangeMin, rangeMax, angleMin, angleMax;
	
	public RThiVetoParams(SpatialVetoProvider provider) {
		super(provider);
	}

}
