package tritechplugins.detect.veto;

import java.awt.Window;

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

}
