package tritechplugins.acquire.swing.framerate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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

	private FrameRateDisplayPanel histPanel;

	private TritechAcquisition tritechAcquisition;
	
	private FrameRateDataBlock frameRateDataBlock;
	
	private GraphAxisPanel graphAxis;
	
	private GraphPlotPanel graphPlot;
	
	private PamAxis xAxis, yAxis;

	public FrameRateGraph(FrameRateDisplayPanel histPanel, TritechAcquisition tritechAcquisition) {
		super();
		this.histPanel = histPanel;
		this.tritechAcquisition = tritechAcquisition;
		
		frameRateDataBlock = histPanel.getFrameRateDataBlock();
		
		graphPlot = new GraphPlotPanel();
		graphAxis = new GraphAxisPanel();
		xAxis = new PamAxis(0, 1, 0, 1, 0, 1, PamAxis.BELOW_RIGHT, "Time (s)", PamAxis.LABEL_NEAR_CENTRE, "%d");
		yAxis = new PamAxis(0, 1, 0, 1, 0, 1., PamAxis.ABOVE_LEFT, "Frame interval (s)", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(PamBorder.createInnerBorder());
		innerPanel.add(BorderLayout.CENTER, graphPlot);
		graphAxis.setPlotPanel(graphPlot);
		graphAxis.setInnerPanel(innerPanel);
		graphAxis.setAutoInsets(true);
		graphAxis.setPlotPanel(graphPlot);
		graphAxis.setSouthAxis(xAxis);
		graphAxis.setWestAxis(yAxis);
		
		graphPlot.setToolTipText("Inter frame interval");
	
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
		public String getToolTipText(MouseEvent event) {
			double val = yAxis.getDataValue(event.getY());
			return String.format("%5.2fs (%3.1f fps)", val, 1./val);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			ArrayList<FrameRateDataUnit> dataCopy = frameRateDataBlock.getDataCopy();
			if (dataCopy.size() == 0) {
				return;
			}
			HashMap<Integer, Long> sonarIdMap = frameRateDataBlock.getSonarIds();
			Set<Integer> sonarIdVals = sonarIdMap.keySet();
			long lastT = frameRateDataBlock.getLastTimeMilliseconds();
			
			
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
			
			if (sonarIdVals.size() == 1) {
				return;
			}
			// plot the sonars individually.
			
			int iCol = 0;
			for (Integer sId : sonarIdVals) {
				prevX = prevY = -1000;
				prevT = 0;
				g.setColor(PamColors.getInstance().getChannelColor(++iCol));
				for (int i = 0; i < dataCopy.size(); i++) {
					if (dataCopy.get(i).getSonarId() != sId) {
						continue;
					}
					long t = dataCopy.get(i).getTimeMilliseconds();
					int x = (int) xAxis.getPosition((t-lastT)/1000.);
					double dt = (t-prevT)/1000.;
					int y = (int) yAxis.getPosition(dt);
					if (prevX >= -100 & prevY > -getHeight()) {
						g.drawLine(prevX, prevY, x, y);
					}
					prevX = x;
					prevY = y;
					prevT = t;
				}
			}
			
			// and draw a simple key in the top rh corner. 
			FontMetrics fm = g.getFontMetrics();
			int fh = fm.getHeight();
			int lineLen = fm.getMaxAdvance()*4;
			int y = fh;
			g.setColor(PamColors.getInstance().getWhaleColor(0));
			g.drawLine(lineLen/4, y, lineLen, y);
			g.drawString(" All sonars", lineLen, y+fm.getAscent()/2);
			iCol = 0;
			for (Integer sId : sonarIdVals) {
				g.setColor(PamColors.getInstance().getChannelColor(++iCol));
				y += fh*3/2;
				g.drawLine(lineLen/4, y, lineLen, y);
				String txt = String.format(" Sonar %d", sId);
				g.setColor(Color.BLACK);
				g.drawString(txt, lineLen, y+fm.getAscent()/2);
			}
		}
		
	}
}
