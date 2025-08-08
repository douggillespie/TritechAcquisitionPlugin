package tritechplugins.acquire;

import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.GeminiImageRecordI;

/**
 * This can hold either a image record OR a status record. Never both. 
 * Users will allways have to check which it is before using. 
 * Have added the status quite late on so that it can go to GLF files. 
 * @author dg50
 *
 */
@SuppressWarnings("rawtypes")
public class ImageDataUnit extends PamDataUnit {

	private GeminiImageRecordI geminiImage;
	
	private SonarStatusData sonarStatusData;

	public ImageDataUnit(long timeMilliseconds, int channelMap, GeminiImageRecordI geminiImage) {
		super(timeMilliseconds);
		setChannelBitmap(channelMap);
		this.geminiImage = geminiImage;
	}

	public ImageDataUnit(long timeMilliseconds, int channelMap, SonarStatusData sonarStatusData) {
		super(timeMilliseconds);
		setChannelBitmap(channelMap);
		this.setSonarStatusData(sonarStatusData);
	}

	/**
	 * @return the geminiImage
	 */
	public GeminiImageRecordI getGeminiImage() {
		return geminiImage;
	}
	
	/**
	 * Set the gemini image record. 
	 * @param imageRecord
	 */
	public void setGeminiImage(GeminiImageRecordI imageRecord) {
		this.geminiImage = imageRecord;
	}

	/**
	 * @return the sonarStatusData
	 */
	public SonarStatusData getSonarStatusData() {
		return sonarStatusData;
	}

	/**
	 * @param sonarStatusData the sonarStatusData to set
	 */
	public void setSonarStatusData(SonarStatusData sonarStatusData) {
		this.sonarStatusData = sonarStatusData;
	}

}
