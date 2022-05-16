package tritechplugins.acquire;

public interface SonarStatusObserver {

	public void updateStatus(SonarStatusData sonarStatusData);
	
	public void errorMessage(String errorMessage);
	
	public void updateFrameRate(int frameRate);
	
}
