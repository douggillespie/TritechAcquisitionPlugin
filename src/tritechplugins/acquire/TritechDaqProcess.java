package tritechplugins.acquire;

import java.util.HashMap;

import PamController.PamController;
import PamguardMVC.PamProcess;

/**
 * Tritech DAQ will acquire from and control the Gemini's. Because we're still not sure if
 * it will work best with JNA or JNI all calls to the sonars will be behind an interface so 
 * its easy to switch between them, though at time of writing, there is only a JNA.  
 * @author dg50
 *
 */
public class TritechDaqProcess extends PamProcess implements TritechRunMode {
	
	private ImageDataBlock imageDataBlock;
	private TritechAcquisition tritechAcquisition;
	private boolean isAcquire;
	private TritechJNADaq jnaDaq;

	public ImageDataBlock getImageDataBlock() {
		return imageDataBlock;
	}

	public TritechDaqProcess(TritechAcquisition tritechAcquisition) {
		super(tritechAcquisition, null);
		this.tritechAcquisition = tritechAcquisition;
		imageDataBlock = new ImageDataBlock(this);
		addOutputDataBlock(imageDataBlock);
		
		isAcquire = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		
		if (isAcquire) {
			jnaDaq = new TritechJNADaq(tritechAcquisition, this);
			boolean isInit = jnaDaq.initialise();
			String version = jnaDaq.getLibVersion();
			System.out.printf("JNA Daq initialised %s version %s\n", new Boolean(isInit).toString(), version);
		}
	}

	
	
	@Override
	public void pamStart() {
		jnaDaq.start();
	}

	@Override
	public void pamStop() {
		jnaDaq.stop();
	}

	@Override
	public int getNumSonars() {
		return jnaDaq.getNumSonars();
	}

	@Override
	public int[] getSonarIDs() {
		return jnaDaq.getSonarIDs();
	}

}
