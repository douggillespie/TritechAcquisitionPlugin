package tritechplugins.detect.veto;

import java.awt.Window;

import PamView.dialog.GenericSwingDialog;

public class RThiVeto extends SpatialVeto {
	
	private RThiVetoParams rThiParams;

	public RThiVeto(RThiVetoProvider rThiProvider) {
		super(rThiProvider);
		rThiParams = new RThiVetoParams(rThiProvider);
	}

	@Override
	public boolean isInVeto(double x, double y) {
		// TODO Auto-generated method stub
		return false;
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

}
