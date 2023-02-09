package tritechplugins.detect.veto.rthi;

import java.awt.Window;

import PamUtils.LatLong;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.VetoSettingsDialog;

public class RThiVeto extends SpatialVeto {
	
	private RThiVetoParams rThiParams;

	public RThiVeto(RThiVetoProvider rThiProvider) {
		super(rThiProvider);
		rThiParams = new RThiVetoParams(rThiProvider);
	}

	@Override
	public boolean isInVeto(double x, double y) {
		double r = Math.sqrt(x*x+y+y);
		if (r < rThiParams.rangeMin || r > rThiParams.rangeMax) {
			return false;
		}
		double ang = Math.atan2(x,y);
		if (ang < rThiParams.angleMin || ang > rThiParams.angleMax) {
			return false;
		}
		return true;
	}

	@Override
	public boolean configureVeto(Window owner) {
		RThiVetoDialogPanel dialogPanel = new RThiVetoDialogPanel(this);
//		boolean ans =  GenericSwingDialog.showDialog(owner, getName(), dialogPanel);
		boolean ans = VetoSettingsDialog.showDialog(owner, this, dialogPanel);
		return ans;
	}

	@Override
	public RThiVetoParams getParams() {
		return rThiParams;
	}

	@Override
	public void setParams(SpatialVetoParams params) {
		if (params instanceof RThiVetoParams) {
			rThiParams = (RThiVetoParams) params;
		}
	}

	@Override
	public String getDescription() {
		return String.format("r/thi veto range %3.1f to %3.1fm, angles %3.1f%s to %3.1f%s",
				rThiParams.rangeMin, rThiParams.rangeMax, Math.toDegrees(rThiParams.angleMin), LatLong.deg,
				Math.toDegrees(rThiParams.angleMax), LatLong.deg);
	}

}
