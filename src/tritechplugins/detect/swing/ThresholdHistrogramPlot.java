package tritechplugins.detect.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamView.PamColors;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.PamBorderPanel;

public class ThresholdHistrogramPlot {

	private PamAxisPanel axisPanel;
	private HistoPlotPanel histoPlotPanel;
	
	private HashMap<String, ThresholdHistogram> histogramData = new HashMap<String, ThresholdHistogram>();
	
	private boolean logY = false;
	
	private PamAxis xAxis, yAxis;
	
	public ThresholdHistrogramPlot() {
		axisPanel = new PamAxisPanel();
		histoPlotPanel = new HistoPlotPanel();
		axisPanel.setInnerPanel(histoPlotPanel);
		xAxis = new PamAxis(0, 1, 0, 1, 0, ThresholdHistogram.MAX_VAL, PamAxis.BELOW_RIGHT, null, PamAxis.LABEL_NEAR_MAX, "%d");
		axisPanel.setSouthAxis(xAxis);
		yAxis = new PamAxis(0, 1, 0, 1, 0, 1, PamAxis.ABOVE_LEFT, null, PamAxis.LABEL_NEAR_MAX, "%d");
		axisPanel.setWestAxis(yAxis);
		axisPanel.setMinNorth(10);
		axisPanel.setMinEast(10);
	}
	
	/**
	 * Set loc scale
	 * @param logScale
	 */
	public void setLogXScale(boolean logScale) {
		xAxis.setMinVal(logScale ? 1 : 0);
		xAxis.setLogScale(logScale);
	}
	
	/**
	 * Get main component. 
	 * @return
	 */
	public JComponent getComponent() {
		return axisPanel;
	}

	private class HistoPlotPanel extends JPanelWithPamKey {

		public HistoPlotPanel() {
//			super(PamColor.PlOTWINDOW);
			setBorder(BorderFactory.createBevelBorder(1));
			setBackground(Color.WHITE);
//			setPreferredSize(new Dimension(800, 650));
		}

		@Override
		public void paintComponent(Graphics g) {
			try {
				paintHistograms(g);
			}
			catch (Exception e) {
				
			}
		}
		private void paintHistograms(Graphics g) {
			super.paintComponent(g);
			setLogXScale(false);
			logY = true;
			
			Set<String> keys = null;
			Collection<ThresholdHistogram> histograms = null;
			synchronized (histogramData) {
				keys = histogramData.keySet();
				histograms = histogramData.values();
			}
			
			double maxVal = 0;
			for (ThresholdHistogram ahist : histograms) {
				maxVal = Math.max(maxVal, ahist.getMaxValue());
			}
//			maxVal = roundUp(maxVal); // auto scale to max
			if (maxVal == 0) {
				return;
			}
			double minVal = 0;
			if (logY) {
				minVal = Math.max(-.5, Math.log(maxVal/100000));
				maxVal = Math.log(maxVal);
			}
			int plotHeight = getHeight();
			int lastY;
			double v;
			int iPlot = 0;
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(2));
			FontMetrics fm = g2d.getFontMetrics();
			
			Iterator<String> keyIt = keys.iterator();
			for (ThresholdHistogram aHist : histograms) {
				
				Color col = PamColors.getInstance().getWhaleColor(iPlot+3);
				g.setColor(col);
				iPlot++;
				
				int[] data = aHist.getData();
				int lastX = 0;
				v = data[0];
				if (logY) {
					v = Math.log(v);
				}
				lastY = plotHeight - (int) ((v-minVal)/(maxVal-minVal)*plotHeight);
				for (int i = 1; i < data.length; i++) {
					int x = (int) xAxis.getPosition(i);
					v = data[i];
					if (logY) {
						if (v == 0) {
							v = minVal/10;
						}
						else {
							v = Math.log(v);
						}
					}
					int y = plotHeight - (int) ((v-minVal)/(maxVal-minVal)*plotHeight);
					g.drawLine(lastX, lastY, x, y);
					lastX = x;
					lastY = y;
				}
				if (keyIt.hasNext()) {
					String key = keyIt.next();
					int xt = (getWidth() - fm.stringWidth(key))/2;;
					int yt = iPlot*fm.getHeight();
					g.drawString(key, xt, yt);
				}
//				break;
			}
		}
	}
	
	private double roundUp(double maxVal) {
		if (xAxis.isLogScale()) {
			// go to the next decade.
			
		}
		return 0;
	}
	
	ThresholdHistogram findHistogram(String dataName) {
		ThresholdHistogram hist = histogramData.get(dataName);
		if (hist == null) {
			hist = new ThresholdHistogram();
			histogramData.put(dataName, hist);
		}
		return hist;
	}
	
	public void setData(String dataName, byte[] data) {
		ThresholdHistogram aHist = findHistogram(dataName);
		aHist.scaleData(.99);
		aHist.addData(data);
		repaintAll();
	}

	private void repaintAll() {
		histoPlotPanel.repaint();
	}
}
