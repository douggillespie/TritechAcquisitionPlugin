package tritechplugins.display.swing;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import tritechplugins.acquire.TritechAcquisition;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class SonarPanelProvider implements UserDisplayProvider {

	private TritechAcquisition tritechAcquisition;

	public SonarPanelProvider(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
	}

	@Override
	public String getName() {
		return "Sonar image display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new SonarDisplayComponent(tritechAcquisition, uniqueDisplayName);
	}

	@Override
	public Class getComponentClass() {
		return SonarDisplayComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}


}
