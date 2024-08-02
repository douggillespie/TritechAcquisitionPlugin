package tritechplugins.detect.threshold;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.TritechAcquisition;

public class TritechDetectPlugin implements PamPluginInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return ThresholdDetector.unitType;
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
		return "Douglas Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "support@pamguard.org";
	}

	@Override
	public String getVersion() {
		return "2.2";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.02.12";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.12";
	}

	@Override
	public String getAboutText() {
		return "Detection in Gemini sonar data using a simple threshold detector and tracker";
	}

	@Override
	public String getClassName() {
		return ThresholdDetector.class.getName();
	}

	@Override
	public String getDescription() {
		return ThresholdDetector.unitType;
	}

	@Override
	public String getMenuGroup() {
		return "Tritech";
	}

	@Override
	public String getToolTip() {
		return "Automatic detection of object tracks in Gemini sonar data";
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(ImageDataUnit.class, TritechAcquisition.class.getName());
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int allowedModes() {
		return ALLMODES;
	}

}
