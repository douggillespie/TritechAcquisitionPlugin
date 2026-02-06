package tritechplugins.acquire;

import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.SonarImageRecordI;

/**
 * This can hold either a image record OR a status record. Never both. 
 * Users will allways have to check which it is before using. 
 * Have added the status quite late on so that it can go to GLF files. 
 * @author dg50
 *
 */
@SuppressWarnings("rawtypes")
public class ImageDataUnit extends PamDataUnit {

	private SonarImageRecordI geminiImage;
	
	private SonarStatusData sonarStatusData;

	public ImageDataUnit(long timeMilliseconds, int channelMap, SonarImageRecordI geminiImage) {
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
	 * Get the device id from either the image or status data. 
	 * @return device id or -1 if invalid data. 
	 */
	public int getDeviceId() {
		if (geminiImage != null) {
			return geminiImage.getDeviceId();
		}
		if (sonarStatusData != null) {
			return sonarStatusData.getDeviceId();
		}
		return -1;
	}
	/**
	 * @return the geminiImage
	 */
	public SonarImageRecordI getGeminiImage() {
		return geminiImage;
	}
	
	/**
	 * Set the gemini image record. 
	 * @param imageRecord
	 */
	public void setGeminiImage(SonarImageRecordI imageRecord) {
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
