package tritechplugins.detect.veto;

public class XYVetoParams extends SpatialVetoParams {

	private static final long serialVersionUID = 1L;
	
	public double xMin, xMax, yMin, yMax;
	
	public XYVetoParams(SpatialVetoProvider provider) {
		super(provider);
	}

}
