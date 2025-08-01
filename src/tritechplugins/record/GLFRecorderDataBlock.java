package tritechplugins.record;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class GLFRecorderDataBlock extends PamDataBlock<GLFRecorderDataUnit> {

	public GLFRecorderDataBlock(GLFRecorderCtrl recorderCtrl, GLFRecorderProcess parentProcess) {
		super(GLFRecorderDataUnit.class, recorderCtrl.getUnitName(), parentProcess, 0);
	}

}
