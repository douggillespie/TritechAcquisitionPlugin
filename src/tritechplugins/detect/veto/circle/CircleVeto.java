package tritechplugins.detect.veto.circle;

import java.awt.Window;

import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;
import tritechplugins.detect.veto.VetoSettingsDialog;

public class CircleVeto extends SpatialVeto {

	private CircleVetoParams circleParams;
	
	public CircleVeto(SpatialVetoProvider vetoProvider) {
		super(vetoProvider);
		circleParams = new CircleVetoParams(vetoProvider);
	}

	@Override
	public boolean configureVeto(Window owner) {
		CircleVetoDialogPanel dialogPanel = new CircleVetoDialogPanel(this);
		boolean ans = VetoSettingsDialog.showDialog(owner, this, dialogPanel);
		return ans;
	}

	@Override
	public boolean isInVeto(double x, double y) {
		double r2 = Math.pow(x-circleParams.centreX, 2) + Math.pow(y-circleParams.centreY, 2);
		double r = Math.sqrt(r2);
		boolean in = r <= circleParams.radius;
//		if (in == false) {
//			System.out.printf("Region r = %3.2f, inside  %s, veto inside is %s\n", r, 
//					in ? "True" : "False", circleParams.vetoInside ? "True" : "False");
//		}
		return in == circleParams.vetoInside;
	}

	@Override
	public CircleVetoParams getParams() {
		return circleParams;
	}

	@Override
	public void setParams(SpatialVetoParams params) {
		if (params instanceof CircleVetoParams) {
			circleParams = (CircleVetoParams) params;
		}
	}

	@Override
	public String getDescription() {
		return String.format("Circle (%s) veto, centre %3.1f,%3.1fm radius %3.1fm",
				circleParams.vetoInside ? "inside" : "outside",
				circleParams.centreX, circleParams.centreY, circleParams.radius);
	}

}
