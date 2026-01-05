package tritechplugins.acquire.swing;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.SwingSymbolOptionsPanel;
import tritechplugins.acquire.ImageDataBlock;

public class SonarImageSymbolManager extends PamSymbolManager<SonarImageSymbolChooser> {



	private ImageDataBlock imageDataBlock;

	public SonarImageSymbolManager(ImageDataBlock imageDataBlock) {
		super(imageDataBlock);
		this.imageDataBlock = imageDataBlock;
	}

	@Override
	protected SonarImageSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new SonarImageSymbolChooser(this, imageDataBlock, displayName, projector);
	}


}
