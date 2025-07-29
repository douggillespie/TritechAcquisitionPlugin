package tritechplugins.record;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.TritechAcquisition;

public class GLFRecorderPlugin implements PamPluginInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return GLFRecorderCtrl.unitName;
	}

	@Override
	public String getHelpSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Doug Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "support@pamguard.org";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.2.17";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.2.17";
	}

	@Override
	public String getAboutText() {
		return "Record Tritech GLF Files";
	}

	@Override
	public String getClassName() {
		return GLFRecorderCtrl.class.getName();
	}

	@Override
	public String getDescription() {
		return "Record Tritech GLF Files";
	}

	@Override
	public String getMenuGroup() {
		return "Tritech";
	}

	@Override
	public String getToolTip() {
		return getDescription();
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(ImageDataUnit.class, TritechAcquisition.unitType);
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 0;
	}

	@Override
	public int getNInstances() {
		return 0;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public int allowedModes() {
		return 0;
	}

}
