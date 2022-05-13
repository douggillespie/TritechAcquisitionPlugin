package tritechplugins.acquire;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class ImageDataBlock extends PamDataBlock {

	private TritechDaqProcess tritechDaqProcess;
	
	public ImageDataBlock(TritechDaqProcess parentProcess) {
		super(ImageDataUnit.class, "Tritech Image Data", parentProcess, 0);
		this.tritechDaqProcess = parentProcess;
	
	}



}
