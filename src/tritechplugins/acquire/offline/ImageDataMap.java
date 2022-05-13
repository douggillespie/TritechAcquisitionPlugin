package tritechplugins.acquire.offline;

import PamController.OfflineDataStore;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;

public class ImageDataMap extends OfflineDataMap<ImageMapPoint> {

	public ImageDataMap(TritechAcquisition tritechAcquisition, ImageDataBlock imageDataBlock) {
		super(tritechAcquisition, imageDataBlock);
	}

}
