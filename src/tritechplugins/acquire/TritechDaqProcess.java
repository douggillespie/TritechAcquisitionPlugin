package tritechplugins.acquire;

import PamguardMVC.PamProcess;

public class TritechDaqProcess extends PamProcess {
	
	private ImageDataBlock imageDataBlock;

	public ImageDataBlock getImageDataBlock() {
		return imageDataBlock;
	}

	public TritechDaqProcess(TritechAcquisition tritechAcquisition) {
		super(tritechAcquisition, null);
		imageDataBlock = new ImageDataBlock(this);
		addOutputDataBlock(imageDataBlock);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

}
