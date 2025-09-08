package tritechplugins.acquire.swing.framerate;

import PamController.PamController;
import tritechplugins.acquire.TritechAcquisition;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class FrameRateDisplayProvider implements UserDisplayProvider{

	private TritechAcquisition tritechAcquisition;
	
	
	public FrameRateDisplayProvider(TritechAcquisition tritechAcquisition) {
		super();
		this.tritechAcquisition = tritechAcquisition;
	}

	@Override
	public String getName() {
		return "Frame rate histogram";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new FrameRateDisplayPanel(this,  userDisplayControl, uniqueDisplayName, tritechAcquisition);
	}

	@Override
	public Class getComponentClass() {
		return FrameRateDisplayPanel.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

}
