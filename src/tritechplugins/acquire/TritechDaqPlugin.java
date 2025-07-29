package tritechplugins.acquire;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import tritechplugins.detect.threshold.ThresholdDetector;

public class TritechDaqPlugin implements PamPluginInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return TritechAcquisition.unitType;
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
		return "1.43";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.02.15";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.14";
	}

	@Override
	public String getAboutText() {
		return "Acquisition and display of Gemini sonar data";
	}

	@Override
	public String getClassName() {
		return TritechAcquisition.class.getName();
	}

	@Override
	public String getDescription() {
		return TritechAcquisition.unitType;
	}

	@Override
	public String getMenuGroup() {
		return "Tritech";
	}

	@Override
	public String getToolTip() {
		return "Acquisition, offline processing, and display of Gemini sonar data";
	}

	@Override
	public PamDependency getDependency() {
		return null;
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 1;
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
		return ALLMODES;
	}

}
