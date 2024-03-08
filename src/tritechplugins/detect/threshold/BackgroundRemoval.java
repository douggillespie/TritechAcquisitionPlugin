package tritechplugins.detect.threshold;

import tritechgemini.detect.BackgroundSub;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataUnit;

/**
 * Wrapper around backgroundSub to input and output dataunits. Basically just another function
 * to handle data packed as dataunits. 
 * @author dg50
 *
 */
public class BackgroundRemoval extends BackgroundSub {
	
	public BackgroundRemoval() {
		super();
	}
	
	/**
	 * Remove background from a data unit, cloning the record. 
	 * @param imageDataUnit
	 * @return
	 */
	public ImageDataUnit removeBackground(ImageDataUnit imageDataUnit, boolean updateFirst) {
		GeminiImageRecordI image = imageDataUnit.getGeminiImage();
		GeminiImageRecordI newImage = removeBackground(image, updateFirst);
		ImageDataUnit newDataUnit = new ImageDataUnit(imageDataUnit.getTimeMilliseconds(), imageDataUnit.getChannelBitmap(), newImage);
		return newDataUnit;
	}

	

}
