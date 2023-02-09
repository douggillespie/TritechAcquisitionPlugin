package tritechplugins.detect.veto.xy;

import java.awt.Window;

import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;
import tritechplugins.detect.veto.VetoSettingsDialog;

public class XYVeto extends SpatialVeto {
	
	private XYVetoParams xyVetoParams;

	public XYVeto(SpatialVetoProvider vetoProvider) {
		super(vetoProvider);
		xyVetoParams = new XYVetoParams(vetoProvider);
	}

	@Override
	public boolean configureVeto(Window owner) {
		XYVetoDialogPanel dialogPanel = new XYVetoDialogPanel(this);
		boolean ans = VetoSettingsDialog.showDialog(owner, this, dialogPanel);
		return ans;
	}

	@Override
	public boolean isInVeto(double x, double y) {
		if (x < xyVetoParams.xMin || x > xyVetoParams.xMax) {
			return false;
		}
		if (y < xyVetoParams.yMin || y > xyVetoParams.yMax) {
			return false;
		}
		return true;
	}

	@Override
	public XYVetoParams getParams() {
		return xyVetoParams;
	}

	@Override
	public void setParams(SpatialVetoParams params) {
		if (params instanceof XYVetoParams) {
			xyVetoParams = (XYVetoParams) params;
		}

	}

	@Override
	public String getDescription() {
		return String.format("xy veto %3.1f<x<%3.1f, %3.1f<y<%3.1f", 
				xyVetoParams.xMin, xyVetoParams.xMax, xyVetoParams.yMin, xyVetoParams.yMax);
	}

}
