package tritechplugins.detect.veto;

public class RThiVetoProvider extends SpatialVetoProvider {

	public RThiVetoProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Range/Angle";
	}

	@Override
	SpatialVeto createVeto() {
		return new RThiVeto(this);
	}

}
