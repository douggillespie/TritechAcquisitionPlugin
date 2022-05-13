package tritechplugins.acquire;

import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.GeminiImageRecordI;

public class ImageDataUnit extends PamDataUnit {

	private GeminiImageRecordI geminiImage;

	public ImageDataUnit(long timeMilliseconds, int channelMap, GeminiImageRecordI geminiImage) {
		super(timeMilliseconds);
		setChannelBitmap(channelMap);
		this.geminiImage = geminiImage;
	}

	/**
	 * @return the geminiImage
	 */
	public GeminiImageRecordI getGeminiImage() {
		return geminiImage;
	}

}
