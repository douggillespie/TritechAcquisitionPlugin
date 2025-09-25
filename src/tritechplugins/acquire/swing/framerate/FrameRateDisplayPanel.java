package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import PamController.PamController;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import pamMaths.HistogramDisplay;
import pamMaths.PamHistogram;
import tritechplugins.acquire.ConfigurationObserver;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.SonarImageObserver;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class FrameRateDisplayPanel implements UserDisplayComponent, ConfigurationObserver {
	
	private FrameRateDisplayProvider frameRateDisplayProvider;
	
	private TritechAcquisition tritechAcquisition;

	private JPanel mainPanel;

	private String panelName;

	private ArrayList<FrameRateHistogram> frameRateHistrograms = new ArrayList();
	
	private JPanel histogramHolder;
//	private HashMap<Integer, PamHistogram> rateHistograms = new HashMap();
	private FrameRateGraph rateGraph;
	
	private long lastFrameTime;
	
	private long lastScaleTime;
	
	private FrameRateDataBlock frameRateDataBlock;
	
	private int plotLifeTime = 60;
	
	private JSplitPane splitPane;

	private GridLayout gridLayout;
	
	/*
	 * Target frame rate in millis. Set to 5 for free run. 
	 */
	private int currentFrameInterval = 100; 

	public FrameRateDisplayPanel(FrameRateDisplayProvider frameRateDisplayProvider,
			UserDisplayControl userDisplayControl, String uniqueDisplayName, TritechAcquisition tritechAcquisition) {
		super();
		this.frameRateDisplayProvider = frameRateDisplayProvider;
		this.tritechAcquisition = tritechAcquisition;
		this.panelName = uniqueDisplayName;

		frameRateDataBlock = new FrameRateDataBlock(tritechAcquisition.getTritechDaqProcess());
		frameRateDataBlock.setNaturalLifetime(plotLifeTime+2);
				
		mainPanel = new PamPanel(new BorderLayout());
						
		histogramHolder = new PamPanel();

		histogramHolder.setLayout(gridLayout = new GridLayout(1, 1));
		
		rateGraph = new FrameRateGraph(this, tritechAcquisition);
		
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(histogramHolder);
		splitPane.add(rateGraph.getComponent());
		mainPanel.add(splitPane);
		
		tritechAcquisition.getImageDataBlock().addObserver(new ImageObserver());
		
		tritechAcquisition.addConfigurationObserver(this);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				splitPane.setDividerLocation(0.5);
			}
		});
		
		configurationChanged();
	}
	
	synchronized FrameRateHistogram findSonarHistogram(int sonarId) {
		for (FrameRateHistogram rh : frameRateHistrograms) {
			if (rh.getSonarId() == sonarId) {
				return rh;
			}
		}
		FrameRateHistogram rateHisto = new FrameRateHistogram(this, tritechAcquisition, sonarId);
		rateHisto.setMaxFPS(getHistoMaxSeconds());
		frameRateHistrograms.add(rateHisto);
		gridLayout.setColumns(frameRateHistrograms.size());
		histogramHolder.add(rateHisto.getComponent());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
//				histoLayout.layoutContainer(histoLayout.getTarget());
//				histogramHolder.invalidate();
				mainPanel.invalidate();				
			}
		});
		return rateHisto;
		
//		PamHistogram hist = rateHistograms.get(0);
//		if (hist == null) {
//			hist = new PamHistogram(0, 12, 100);
//			rateHistograms.put(sonarId, hist);
//			histogramDisplay.addHistogram(hist);
//		}
//		return hist;
	}
	
	private double getHistoMaxSeconds() {
		int nSonar = Math.max(1,  frameRateHistrograms.size());
		if (currentFrameInterval > 0) {
			return 1000./currentFrameInterval * 3 / nSonar;
		}
		else {
			return 10;
		}
	}
	
	private double getGraphMaxRate() {
		int nSonar = Math.max(1,  frameRateHistrograms.size());
		return (double) currentFrameInterval / 1000. * 3. * nSonar;
	}
	
	private class ImageObserver extends SonarImageObserver {

		@Override
		public String getObserverName() {
			return "Frame rate monitor";
		}

		@Override
		public void addImageData(PamObservable observable, ImageDataUnit imageDataUnit) {
			newImageFrame(imageDataUnit);			
		}

		@Override
		public void addStatusData(PamObservable observable, ImageDataUnit imageDataUnit) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	public void newImageFrame(PamDataUnit pamDataUnit) {
		
		if (pamDataUnit instanceof ImageDataUnit == false) {
			return;
		}
		
		ImageDataUnit idu = (ImageDataUnit) pamDataUnit;
		
		FrameRateDataUnit fdu = new FrameRateDataUnit(pamDataUnit.getTimeMilliseconds(), idu.getGeminiImage().getDeviceId());
		frameRateDataBlock.addPamData(fdu);
		rateGraph.update();
		
		FrameRateHistogram rateHistogram = findSonarHistogram(idu.getGeminiImage().getDeviceId());
		
		rateHistogram.newData(pamDataUnit.getTimeMilliseconds());
		
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			configurationChanged();
		}		
	}

	@Override
	public String getUniqueName() {
		return panelName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.panelName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return "Gemini frame rate";
	}

	public FrameRateDataBlock getFrameRateDataBlock() {
		return frameRateDataBlock;
	}

	@Override
	public void configurationChanged() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		int newInterval = params.getManualPingInterval();
		if (params.isManualPingRate() == false)
		{
			newInterval = 100;
		}
		double maxFPS = 1000./newInterval*3;
//		if (newInterval != currentFrameInterval) {
			currentFrameInterval = newInterval;
			resetHistograms();
//		}
	}

	private synchronized void resetHistograms() {
		for (int i = 0; i < frameRateHistrograms.size(); i++) {
			frameRateHistrograms.get(i).clear();
			frameRateHistrograms.get(i).setMaxFPS(getHistoMaxSeconds());
		}
		rateGraph.setYRange(0, getGraphMaxRate());
	}
	
	

}
