package tritechplugins.detect.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import PamView.panel.PamPanel;
import tritechplugins.detect.ThresholdDetector;
import tritechplugins.detect.ThresholdObserver;
import userDisplay.UserDisplayComponent;

public class DetectorHistogramPanel implements UserDisplayComponent, ThresholdObserver {

	private ThresholdDetector thresholdDetector;
	private String panelName;
	private HashMap<Integer, ThresholdHistrogramPlot> histogramPlots;
	private ThresholdHistrogramPlot thresholdHistrogramPlot;
	
	private JPanel mainPanel;
	private PamPanel plotsPanel;
	private boolean firstPlot;

	public DetectorHistogramPanel(ThresholdDetector thresholdDetector, String panelName) {
		this.thresholdDetector = thresholdDetector;
		this.panelName = panelName;
		histogramPlots = new HashMap<>();
		
		mainPanel = new PamPanel(new BorderLayout());
		plotsPanel = new PamPanel();
		plotsPanel.setLayout(new BoxLayout(plotsPanel, BoxLayout.Y_AXIS));
		mainPanel.add(plotsPanel, BorderLayout.CENTER);
		findHistogramPlot(0);
		firstPlot = true;
		thresholdDetector.addThresholdObserver(this);
	}
	
	private ThresholdHistrogramPlot findHistogramPlot(int sonarId) {
		ThresholdHistrogramPlot histoPlot = histogramPlots.get(sonarId);
		if (firstPlot) {
			ThresholdHistrogramPlot dummyPlot = histogramPlots.get(0);
			if (dummyPlot != null) {
				plotsPanel.remove(dummyPlot.getComponent());
			}
		}
		if (histoPlot == null) {
			histoPlot = new ThresholdHistrogramPlot();
			histogramPlots.put(sonarId, histoPlot);
			plotsPanel.add(histoPlot.getComponent());
			plotsPanel.invalidate();
			plotsPanel.doLayout();
			mainPanel.repaint();
		}
		return histoPlot;
	}

	@Override
	public Component getComponent() {
		return mainPanel;
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
		return panelName;
	}

	@Override
	public void newRawData(int sonarId, byte[] data) {
		ThresholdHistrogramPlot histoPlot = findHistogramPlot(sonarId);
		String dataName = String.format("Sonar %d raw", sonarId);
		histoPlot.setData(dataName, data);
//		histoPlot.
	}

	@Override
	public void newTreatedData(int sonarId, byte[] data) {
		ThresholdHistrogramPlot histoPlot = findHistogramPlot(sonarId);
		String dataName = String.format("Sonar %d treated", sonarId);
		histoPlot.setData(dataName, data);
		
	}

}
