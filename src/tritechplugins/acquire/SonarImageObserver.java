package tritechplugins.acquire;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;

/**
 * Observer for sonar image data, that works out if it's an image or if
 * it's status data. Use whereever possible in place of the standard observers
 * (probably not possible within PAMProcesses). 
 * @author dg50
 *
 */
public abstract class SonarImageObserver extends PamObserverAdapter {


	public SonarImageObserver() {
	}

	@Override
	public final void addData(PamObservable observable, PamDataUnit pamDataUnit) {
		ImageDataUnit imageDataUnit = (ImageDataUnit) pamDataUnit;
		if (imageDataUnit.getGeminiImage() != null) {
			addImageData(observable, imageDataUnit);
		}
		if (imageDataUnit.getSonarStatusData() != null) {
			addStatusData(observable, imageDataUnit);
		}
	}
	
	/**
	 * A new image data has been added to the datablock. 
	 * @param observable
	 * @param imageDataUnit
	 */
	public abstract void addImageData(PamObservable observable, ImageDataUnit imageDataUnit);

	/**
	 * A new status data has been added to the datablock. 
	 * @param observable
	 * @param imageDataUnit
	 */
	public abstract void addStatusData(PamObservable observable, ImageDataUnit imageDataUnit);

	@Override
	public final void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		ImageDataUnit imageDataUnit = (ImageDataUnit) pamDataUnit;
		if (imageDataUnit.getGeminiImage() != null) {
			updateImageData(observable, imageDataUnit);
		}
		if (imageDataUnit.getSonarStatusData() != null) {
			updateStatusData(observable, imageDataUnit);
		}
	}

	/**
	 * Image data has been updated
	 * @param observable
	 * @param imageDataUnit
	 */
	private void updateImageData(PamObservable observable, ImageDataUnit imageDataUnit) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Status data has been updated
	 * @param observable
	 * @param imageDataUnit
	 */
	private void updateStatusData(PamObservable observable, ImageDataUnit imageDataUnit) {
		// TODO Auto-generated method stub
		
	}

}
