package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;

import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import pamMaths.HistogramDisplay;
import pamMaths.PamHistogram;
import tritechplugins.acquire.TritechAcquisition;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class FrameRateHistogramPanel implements UserDisplayComponent {
	
	private FrameRateDisplayProvider frameRateDisplayProvider;
	
	private TritechAcquisition tritechAcquisition;

	private JPanel mainPanel;

//	private PamPanel plotsPanel;
	HistogramDisplay histogramDisplay;

	private String panelName;
	
	private PamHistogram rateHistogram;
	
	private long lastFrameTime;
	
	private long lastScaleTime;

	public FrameRateHistogramPanel(FrameRateDisplayProvider frameRateDisplayProvider,
			UserDisplayControl userDisplayControl, String uniqueDisplayName, TritechAcquisition tritechAcquisition) {
		super();
		this.frameRateDisplayProvider = frameRateDisplayProvider;
		this.tritechAcquisition = tritechAcquisition;
		this.panelName = uniqueDisplayName;
		
		rateHistogram = new PamHistogram(0, 5., 100);
		
		mainPanel = new PamPanel(new BorderLayout());
		histogramDisplay = new HistogramDisplay();
		histogramDisplay.addHistogram(rateHistogram);
		histogramDisplay.setXLabel("Frame interval (s)");
		
		mainPanel.add(histogramDisplay.getGraphicComponent());
		
		tritechAcquisition.getImageDataBlock().addObserver(new ImageObserver());
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
		if (pamDataUnit.getTimeMilliseconds()-lastScaleTime > 1000) {
			rateHistogram.scaleData(.99);
			lastScaleTime = pamDataUnit.getTimeMilliseconds();
		}
		double dt = (pamDataUnit.getTimeMilliseconds()-lastFrameTime)/1000.;
		if (dt < rateHistogram.getMaxVal()) {
			rateHistogram.addData(dt);
		}
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
	
	

}
