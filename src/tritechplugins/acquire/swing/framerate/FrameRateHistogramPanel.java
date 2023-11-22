package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

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
import tritechplugins.acquire.TritechAcquisition;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class FrameRateHistogramPanel implements UserDisplayComponent, ConfigurationObserver {
	
	private FrameRateDisplayProvider frameRateDisplayProvider;
	
	private TritechAcquisition tritechAcquisition;

	private JPanel mainPanel;

//	private PamPanel plotsPanel;
	HistogramDisplay histogramDisplay;

	private String panelName;
	
	private PamHistogram rateHistogram;
	
	private FrameRateGraph rateGraph;
	
	private long lastFrameTime;
	
	private long lastScaleTime;
	
	private FrameRateDataBlock frameRateDataBlock;
	
	private int plotLifeTime = 60;
	
	private JSplitPane splitPane;

	public FrameRateHistogramPanel(FrameRateDisplayProvider frameRateDisplayProvider,
			UserDisplayControl userDisplayControl, String uniqueDisplayName, TritechAcquisition tritechAcquisition) {
		super();
		this.frameRateDisplayProvider = frameRateDisplayProvider;
		this.tritechAcquisition = tritechAcquisition;
		this.panelName = uniqueDisplayName;

		frameRateDataBlock = new FrameRateDataBlock(tritechAcquisition.getTritechDaqProcess());
		frameRateDataBlock.setNaturalLifetime(plotLifeTime);
		
		rateHistogram = new PamHistogram(0, 12, 100);
		
		mainPanel = new PamPanel(new BorderLayout());
		
		
		histogramDisplay = new HistogramDisplay();
		histogramDisplay.addHistogram(rateHistogram);
		histogramDisplay.setXLabel("Frame rate (fps)");
		
		rateGraph = new FrameRateGraph(this, tritechAcquisition);
		
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(histogramDisplay.getGraphicComponent());
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
	
	private class ImageObserver implements PamObserver {

		@Override
		public long getRequiredDataHistory(PamObservable observable, Object arg) {
			return 0;
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			newImageFrame(pamDataUnit);
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		}

		@Override
		public void removeObservable(PamObservable observable) {
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
		}

		@Override
		public void noteNewSettings() {
		}

		@Override
		public String getObserverName() {
			return panelName;
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
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
		
		if (pamDataUnit.getTimeMilliseconds()-lastScaleTime > 1000) {
			rateHistogram.scaleData(.99);
			lastScaleTime = pamDataUnit.getTimeMilliseconds();
		}
		double dt = (pamDataUnit.getTimeMilliseconds()-lastFrameTime)/1000.;
//		if (dt < rateHistogram.getMaxVal()) {
		if (dt >0)
			rateHistogram.addData(1./dt);
		
//		}
		lastFrameTime = pamDataUnit.getTimeMilliseconds();
		histogramDisplay.repaint();
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
		rateHistogram.clear();
	}
	
	

}
