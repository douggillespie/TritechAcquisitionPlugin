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
import geminisdk.OutputFileInfo;
import geminisdk.Svs5Exception;
import geminisdk.structures.LoggerPlaybackUpdate;
import tritechplugins.acquire.swing.DaqDialog;

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
		
	
	private ArrayList<SonarStatusObserver> statusObservers = new ArrayList();
	
	/**
	 * This is a variety of ways of getting data in - from real time using svs5 via JNA
	 * to my own pure Java file reader. 
	 */
	private TritechDaqSystem tritechDaqSystem;
	
	/**
	 * @return the tritechDaqSystem
	 */
	public TritechDaqSystem getTritechDaqSystem() {
		return tritechDaqSystem;
	}

	public ImageDataBlock getImageDataBlock() {
		return imageDataBlock;
	}

	public TritechDaqProcess(TritechAcquisition tritechAcquisition) {
		super(tritechAcquisition, null);
		this.tritechAcquisition = tritechAcquisition;
		imageDataBlock = new ImageDataBlock(this);
		addOutputDataBlock(imageDataBlock);
		
		sortDaqSystem();
	}
	
	/**
	 * Call to sort out what type of daq system we actually want to use 
	 */
	private boolean sortDaqSystem() {
		isAcquire = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		if (isAcquire == false) {
			return false;
		}
		Class want = neededDaqClass();
		if (tritechDaqSystem != null && tritechDaqSystem.getClass() != want) {
			tritechDaqSystem.unprepareProcess();
			tritechDaqSystem.uninitialise();
			tritechDaqSystem = null;
		}
		if (tritechDaqSystem == null) {
			if (want == TritechJNADaq.class) {
				tritechDaqSystem = new TritechJNADaq(tritechAcquisition, this);
			}
			if (want == TritechJNAPlayback.class) {
				tritechDaqSystem = new TritechJNAPlayback(tritechAcquisition, this);
			}
			if (want == JavaFileAcquisition.class) {
				tritechDaqSystem = new JavaFileAcquisition(tritechAcquisition, this);
			}
		}
		return tritechDaqSystem != null;
	}
	
	/**
	 * get the class of the daq system we want to use. 
	 * @return
	 */
	private Class neededDaqClass() {
		isAcquire = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		if (params.getRunMode() == TritechDaqParams.RUN_ACQUIRE) {
			return TritechJNADaq.class;
		}
		else {
			return JavaFileAcquisition.class;
//			return TritechJNAPlayback.class;
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
	public void prepareProcess() {
		super.prepareProcess();
		sortDaqSystem();
		if (tritechDaqSystem != null) {
			tritechDaqSystem.prepareProcess();
		}
	}

	@Override
	public void pamStart() {
		if (tritechDaqSystem != null) {
			tritechDaqSystem.start();
		}
	}

	@Override
	public void pamStop() {
		if (tritechDaqSystem != null) {
			tritechDaqSystem.stop();
		}
	}

	/**
	 * Called when PAMGuard is really closing ...
	 */
	public void pamClose() {
		if (tritechDaqSystem != null) {
			tritechDaqSystem.uninitialise();
		}
		
	}

//	@Override
//	public int getNumSonars() {
//		if (tritechDaqSystem != null) {
//		return tritechDaqSystem.getNumSonars();
//		}
//		else {
//			return 0;
//		}
//	}
//
//	@Override
//	public int[] getSonarIDs() {
//		if (tritechDaqSystem != null) {
//			return tritechDaqSystem.getSonarIDs();
//		}
//		else {
//			return null;
//		}
//	}

	public JMenuItem createDaqMenu(Frame parentFrame) {
		JMenu menu = new JMenu(tritechAcquisition.getUnitName());
		JMenuItem menuItem;
		menuItem = new JMenuItem("Settings ...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
//		tritechDaqSystem.
		menuItem = new JMenuItem("Reboot");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tritechDaqSystem.rebootSonars();
			}
		});
		
		return menu;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		TritechDaqParams newParams = DaqDialog.showDialog(parentFrame, tritechAcquisition.getDaqParams());
		if (newParams != null) {
			tritechAcquisition.setDaqParams(newParams);
			prepareProcess();
			tritechAcquisition.configurationChanged();
		}
	}


//	public void setRange(int range) {
//		if (jnaDaq == null) {
//			return;
//		}
//		try {
//			jnaDaq.setRange(range, 0);
//		} catch (Svs5Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void setGain(int gain) {
//		if (jnaDaq == null) {
//			return;
//		}
//		try {
//			jnaDaq.setGain(gain, 0);
//		} catch (Svs5Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * New status data from a sonar. These come thick and fast whether acquiring or not
	 * @param sonarStatusData
	 */
	public void updateStatusData(SonarStatusData sonarStatusData) {
		// run some checks on the status to see if it looks OK. 
		
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateStatus(sonarStatusData);
		}
	}
	
	public void updateFileName(OutputFileInfo outputFileInfo) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateOutputFileInfo(outputFileInfo);
		}		
	}

	public void updateFrameRate(int frameRate, double trueFPS) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFrameRate(frameRate, trueFPS);
		}
	}
	
	public void updateQueueSize(int svs5QueueSize) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateQueueSize(svs5QueueSize);
		}
		
	}

	public void updateLoggerPlayback(LoggerPlaybackUpdate loggerPlaybackUpdate) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateLoggerPlayback(loggerPlaybackUpdate);
		}
	}

	public void updateFileIndex(int fileIndex) {
		for (SonarStatusObserver obs : statusObservers) {
			obs.updateFileIndex(fileIndex);
		}
	}
	
//	public String getLibVersion() {
//		if (jnaDaq == null) {
//			return "Offline";
//		}
//		return jnaDaq.getLibVersion();
//	}
	
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
	
	/**
	 * Remove a status observer. 
	 * @param statusObserver
	 */
	public void removeStatusObserver(SonarStatusObserver statusObserver) {
		statusObservers.remove(statusObserver);
	}

	/**
	 * @return the tritechAcquisition
	 */
	public TritechAcquisition getTritechAcquisition() {
		return tritechAcquisition;
	}

//	/**
//	 * Set the playback speed. Called from the task bar when running offline. 
//	 * @param speed 0 = fast as, otherwise a double
//	 */
//	public void setPlaybackSpeed(double speed) {
//		jnaDaq.setPlaybackSpeed(speed);
//	}


}
