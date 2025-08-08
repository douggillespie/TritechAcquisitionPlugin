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

//	private BoxLayout histoLayout;

	private GridLayout gridLayout;

	public FrameRateDisplayPanel(FrameRateDisplayProvider frameRateDisplayProvider,
			UserDisplayControl userDisplayControl, String uniqueDisplayName, TritechAcquisition tritechAcquisition) {
		super();
		this.frameRateDisplayProvider = frameRateDisplayProvider;
		this.tritechAcquisition = tritechAcquisition;
		this.panelName = uniqueDisplayName;

		frameRateDataBlock = new FrameRateDataBlock(tritechAcquisition.getTritechDaqProcess());
		frameRateDataBlock.setNaturalLifetime(plotLifeTime+2);
		
//		rateHistogram = new PamHistogram(0, 12, 100);
		
		mainPanel = new PamPanel(new BorderLayout());
		
		
//		histogramDisplay = new HistogramDisplay();
//		histogramDisplay.setSelectedStats(0);
//		histogramDisplay.setXLabel("Frame rate (fps)");
//		histogramDisplay.addHistogram(singleHistogram);
//		
		histogramHolder = new PamPanel();
//		histogramHolder.setLayout(histoLayout = new BoxLayout(histogramHolder, BoxLayout.X_AXIS));
		histogramHolder.setLayout(gridLayout = new GridLayout(1, 1));
//		GridLayout gl = new GridLayout(2, 1);
//		gl.g
		
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
	}
	
	FrameRateHistogram findSonarHistogram(int sonarId) {
		for (FrameRateHistogram rh : frameRateHistrograms) {
			if (rh.getSonarId() == sonarId) {
				return rh;
			}
		}
		FrameRateHistogram rateHisto = new FrameRateHistogram(this, tritechAcquisition, sonarId);
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
		// TODO Auto-generated method stub
		
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
//		histogramDisplay.removeAllHistograms(null);
//		rateHistograms.clear();
	}
	
	

}
