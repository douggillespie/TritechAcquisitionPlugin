package tritechplugins.detect.threshold;

public interface ThresholdObserver {

	public void newRawData(int sonarId, byte[] data);
	
	public void newTreatedData(int sonarId, byte[] data);

	public void newBackgroundData(int sonarId, byte[] data);
	
}
