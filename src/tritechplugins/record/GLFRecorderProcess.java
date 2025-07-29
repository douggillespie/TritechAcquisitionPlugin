package tritechplugins.record;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import tritechplugins.acquire.ImageDataBlock;

public class GLFRecorderProcess extends PamProcess {

	private GLFRecorderCtrl recorderCtrl;
	
	private ImageDataBlock dataBuffer;

	private boolean preparedOK;

	public GLFRecorderProcess(GLFRecorderCtrl glfRecorderCtrl) {
		super(glfRecorderCtrl, null);
		this.recorderCtrl = glfRecorderCtrl;
		dataBuffer = new ImageDataBlock(null);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepareProcess() {
		preparedOK = true;
		super.prepareProcess();
		GLFRecorderParams params = recorderCtrl.getRecorderParams();
		PamDataBlock sourceImages = PamController.getInstance().getDataBlockByLongName(params.imageDataSource);
		setParentDataBlock(sourceImages);
		if (sourceImages == null) {
			preparedOK = false;
		}
		dataBuffer.setNaturalLifetime(params.bufferSeconds);
		preparedOK &= recorderCtrl.checkOutputFolder(true);
	}

	@Override
	public boolean prepareProcessOK() {
		prepareProcess();
		return preparedOK;
	}


}
