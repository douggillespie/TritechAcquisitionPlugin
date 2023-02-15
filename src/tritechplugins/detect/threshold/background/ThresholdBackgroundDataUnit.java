package tritechplugins.detect.threshold.background;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.background.BackgroundDataUnit;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecord;
import tritechplugins.acquire.ImageDataUnit;

public class ThresholdBackgroundDataUnit extends BackgroundDataUnit {


	private GLFImageRecord geminiImageRecord;

	public ThresholdBackgroundDataUnit(long timeMilliseconds, int channelBitmap, double durationMillis, 
			GLFImageRecord geminiImageRecord) {
		super(timeMilliseconds, channelBitmap, durationMillis);
		this.geminiImageRecord = geminiImageRecord;
	}

	@Override
	public double getCountSPL() {
		
		return 0;
	}

	/**
	 * @return the backgroundImage
	 */
	public GLFImageRecord getBackgroundImage() {
		return geminiImageRecord;
	}

}
