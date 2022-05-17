package tritechplugins.acquire;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamController;
import PamguardMVC.PamProcess;
import geminisdk.Svs5Exception;

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
	
	private ArrayList<SonarStatusObserver> statusObservers = new ArrayList();

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
	
	/**
	 * Get the range of gain values. This is a percentage between 1 and 100.  <br>
	 *  see Sv5JavaInterface.h 
	 * @return range of gain values.
	 */
	public int[] getGainRange() {
		int[] range = {1, 100};
		return range;
	}
	/**
	 *  Range of range values. note that these are actually sent to
	 *  the sonar as double precision values.<br>
	 *  see Sv5JavaInterface.h 
	 * @return range of gain values. 
	 */
	public int[] getRangeRange() {
		int[] range = {1, 120};
		return range;
	}

	
	
	@Override
	public void pamStart() {
		jnaDaq.start();
	}

	@Override
	public void pamStop() {
		if (jnaDaq != null) {
			jnaDaq.stop();
		}
	}

	@Override
	public int getNumSonars() {
		return jnaDaq.getNumSonars();
	}

	@Override
	public int[] getSonarIDs() {
		return jnaDaq.getSonarIDs();
	}

	public JMenuItem createDaqMenu(Frame parentFrame) {
		JMenu menu = new JMenu();
		int[] sonarIDs = jnaDaq.getSonarIDs();
		JMenuItem menuItem = new JMenuItem("Reboot sonar(s)");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rebootSonars();
			}
		});
		menu.add(menuItem);
		return menuItem;
	}

	protected void rebootSonars() {
		try {
			jnaDaq.rebootSonars();
		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setRange(int range) {
		if (jnaDaq == null) {
			return;
		}
		try {
			jnaDaq.setRange(range, 0);
		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setGain(int gain) {
		if (jnaDaq == null) {
			return;
		}
		try {
			jnaDaq.setGain(gain, 0);
		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * New status data from a sonar. These come thick and fast whether acquiring or not
	 * @param sonarStatusData
	 */
	public void updateStatusData(SonarStatusData sonarStatusData) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateStatus(sonarStatusData);
		}
	}
	
	public void updateFileName(String fileName) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFileName(fileName);
		}		
	}

	public void updateFrameRate(int frameRate) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFrameRate(frameRate);
		}
	}
	
	public String getLibVersion() {
		if (jnaDaq == null) {
			return "Offline";
		}
		return jnaDaq.getLibVersion();
	}
	
//	public int getCurrentFrameRate() {
//		if (jnaDaq == null) {
//			return 0;
//		}
//		return jnaDaq.getCurrentFrameRate();
//	}
	
	/**
	 * Add an observer which will receive updates every time new status data arrive. 
	 * @param statusObserver
	 */
	public void addStatusObserver(SonarStatusObserver statusObserver) {
		statusObservers.add(statusObserver);
	}

}
