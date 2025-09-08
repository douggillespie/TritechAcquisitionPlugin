package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import pamMaths.HistogramDisplay;
import pamMaths.PamHistogram;
import tritechplugins.acquire.TritechAcquisition;

public class FrameRateHistogram {

	private FrameRateDisplayPanel histPanel;
	private TritechAcquisition tritechAcquisition;
	private int sonarId;

	private HistogramDisplay histogramDisplay;
	private PamHistogram singleHistogram = new PamHistogram(0, 12, 100);
	
	private JPanel mainPanel;
	private long lastScaleTime;
	private long lastFrameTime;

	public FrameRateHistogram(FrameRateDisplayPanel histPanel, TritechAcquisition tritechAcquisition, int sonarId) {
		super();
		this.histPanel = histPanel;
		this.tritechAcquisition = tritechAcquisition;
		this.sonarId = sonarId;
		
		histogramDisplay = new HistogramDisplay(singleHistogram);
		histogramDisplay.setSelectedStats(0);
		histogramDisplay.setShowStats(false);
		histogramDisplay.setXLabel("Frame rate (fps)");
		
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, histogramDisplay.getGraphicComponent());
		mainPanel.setBorder(new TitledBorder("Sonar " + sonarId));
	}

	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}
	
	public void setMaxFPS(double maxFPS) {
		if (maxFPS != singleHistogram.getMaxVal()) {
			singleHistogram.setRange(0, maxFPS, 100);
		}
	}
	public void clear() {
		singleHistogram.clear();
	}

	public void newData(long timeMilliseconds) {
		if (timeMilliseconds-lastScaleTime > 1000) {
			singleHistogram.scaleData(.95);
			lastScaleTime = timeMilliseconds;
		}
		double dt = (timeMilliseconds-lastFrameTime)/1000.;
		if (dt >0  && dt<singleHistogram.getMaxVal()*2)
			singleHistogram.addData(1./dt);
		
//		}
		lastFrameTime = timeMilliseconds;
		histogramDisplay.repaint();
	}
}
