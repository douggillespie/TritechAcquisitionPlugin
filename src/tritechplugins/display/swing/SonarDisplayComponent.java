package tritechplugins.display.swing;

import java.awt.Component;

import PamController.SettingsNameProvider;
import tritechplugins.acquire.TritechAcquisition;
import userDisplay.UserDisplayComponent;

public class SonarDisplayComponent implements UserDisplayComponent, SettingsNameProvider {

	private TritechAcquisition tritechAcquisition;
	
	private SonarsOuterPanel sonarsOuterPanel;

	private String uniqueName;
	
	public SonarDisplayComponent(TritechAcquisition tritechAcquisition, String uniqueDisplayName) {
		this.tritechAcquisition = tritechAcquisition;
		this.uniqueName = uniqueDisplayName;
		sonarsOuterPanel = new SonarsOuterPanel(tritechAcquisition, this);
	}

	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return sonarsOuterPanel.getOuterPanel();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return uniqueName;
	}

	@Override
	public String getUnitName() {
		return getUniqueName();
	}

}
