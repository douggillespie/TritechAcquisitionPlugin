package tritechplugins.detect.veto;

public class XYVetoProvider extends SpatialVetoProvider {

	public XYVetoProvider() {
	}

	@Override
	public String getName() {
		return "X,Y Box Veto";
	}

	@Override
	SpatialVeto createVeto() {
		return new XYVeto(this);
	}

}
