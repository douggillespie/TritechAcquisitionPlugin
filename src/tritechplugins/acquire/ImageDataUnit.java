package tritechplugins.acquire;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class ImageDataUnit extends PamDataUnit {

	public ImageDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		// TODO Auto-generated constructor stub
	}

	public ImageDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		// TODO Auto-generated constructor stub
	}

	public ImageDataUnit(DataUnitBaseData basicData) {
		super(basicData);
		// TODO Auto-generated constructor stub
	}


}
