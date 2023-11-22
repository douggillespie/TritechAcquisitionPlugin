package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import Layout.PamFramePlots;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import tritechplugins.acquire.TritechAcquisition;

public class FrameRateGraph {

	private FrameRateHistogramPanel histPanel;

	private TritechAcquisition tritechAcquisition;
	
	private FrameRateDataBlock frameRateDataBlock;
	
	private GraphAxisPanel graphAxis;
	
	private GraphPlotPanel graphPlot;
	
	private PamAxis xAxis, yAxis;

	public FrameRateGraph(FrameRateHistogramPanel histPanel, TritechAcquisition tritechAcquisition) {
		super();
		this.histPanel = histPanel;
		this.tritechAcquisition = tritechAcquisition;
		
		frameRateDataBlock = histPanel.getFrameRateDataBlock();
		
		graphPlot = new GraphPlotPanel();
		graphAxis = new GraphAxisPanel();
		xAxis = new PamAxis(0, 1, 0, 1, 0, 1, PamAxis.BELOW_RIGHT, "Time (s)", PamAxis.LABEL_NEAR_CENTRE, "%d");
		yAxis = new PamAxis(0, 1, 0, 1, 0, 2., PamAxis.ABOVE_LEFT, "Frame interval (s)", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(PamBorder.createInnerBorder());
		innerPanel.add(BorderLayout.CENTER, graphPlot);
		graphAxis.setPlotPanel(graphPlot);
		graphAxis.setInnerPanel(innerPanel);
		graphAxis.setAutoInsets(true);
		graphAxis.setPlotPanel(graphPlot);
		graphAxis.setSouthAxis(xAxis);
		graphAxis.setWestAxis(yAxis);
	
	}
	
	public Component getComponent() {
		return graphAxis;
	}
	
	public void update() {
		int lifeTime = frameRateDataBlock.getNaturalLifetime();
		xAxis.setRange(-lifeTime, 0);

		
		graphAxis.repaint(200);
		graphPlot.repaint(200);
	}
	
	private class GraphAxisPanel extends PamAxisPanel {
		
	}
	
	private class GraphPlotPanel extends PamPanel {

		public GraphPlotPanel() {
			super(PamColor.PlOTWINDOW);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			ArrayList<FrameRateDataUnit> dataCopy = frameRateDataBlock.getDataCopy();
			if (dataCopy.size() == 0) {
				return;
			}
			long lastT = dataCopy.get(dataCopy.size()-1).getTimeMilliseconds();
			long prevT = dataCopy.get(0).getTimeMilliseconds();
			int prevX=-1, prevY=-1;
			g.setColor(PamColors.getInstance().getWhaleColor(0));
			for (int i = 1; i < dataCopy.size(); i++) {
				long t = dataCopy.get(i).getTimeMilliseconds();
				int x = (int) xAxis.getPosition((t-lastT)/1000.);
				double dt = (t-prevT)/1000.;
				int y = (int) yAxis.getPosition(dt);
				if (prevX >= 0) {
					g.drawLine(prevX, prevY, x, y);
				}
				prevX = x;
				prevY = y;
				prevT = t;
			}
		}
		
	}
}
