package tritechplugins.acquire;

import geminisdk.OutputFileInfo;

public interface SonarStatusObserver {

	public void updateStatus(SonarStatusData sonarStatusData);
	
	public void errorMessage(String errorMessage);
	
	public void updateFrameRate(int frameRate);
	
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo);
	
}
