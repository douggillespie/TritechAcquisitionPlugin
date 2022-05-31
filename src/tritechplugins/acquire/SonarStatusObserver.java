package tritechplugins.acquire;

import geminisdk.OutputFileInfo;
import geminisdk.structures.LoggerPlaybackUpdate;

public interface SonarStatusObserver {

	public void updateStatus(SonarStatusData sonarStatusData);
	
	public void errorMessage(String errorMessage);
	
	public void updateFrameRate(int frameRate);
	
	public void updateOutputFileInfo(OutputFileInfo outputFileInfo);
	
	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate);

	public void updateFileIndex(int fileIndex);
	
}
