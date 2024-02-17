package tritechplugins.detect.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataUnit;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.display.swing.SonarRThiProjector;
import tritechplugins.display.swing.overlays.SonarSymbolChooser;
import tritechplugins.display.swing.overlays.SonarSymbolOptions;

/**
 * Uses simple x,y projector to draw outlines of regions for given frame. 
 * @author dg50
 *
 */
public class RegionOverlayDraw extends PanelOverlayDraw {

	public static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS, 2, 2, false, Color.BLUE, Color.BLUE);
	
	public RegionOverlayDraw() {
		super(defaultSymbol);
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		RegionDataUnit regionDataUnit = (RegionDataUnit) pamDataUnit;
		DetectedRegion region = regionDataUnit.getRegion();
		if (region == null) {
			return null;
		}
		PamSymbolChooser symbolChooser = generalProjector.getPamSymbolChooser();
		SonarSymbolOptions symbolOptions;
		if (symbolChooser instanceof SonarSymbolChooser) {
			SonarSymbolChooser sonarSymbolChooser = (SonarSymbolChooser) symbolChooser;
			symbolOptions = sonarSymbolChooser.getSymbolOptions();
		}
		else {
			symbolOptions = new SonarSymbolOptions();
		}
		boolean useRThi = false;
		if (generalProjector.getClass() == SonarRThiProjector.class) {
			useRThi = true;
		}
		if (symbolOptions.symbolType == SonarSymbolOptions.DRAW_BOX) {
			if (useRThi) {
				return drawRThiBox(g, regionDataUnit, generalProjector);
			}
			else {
				return drawBox(g, regionDataUnit, generalProjector);
			}
		}
		else {
			return drawSymbol(g, regionDataUnit, generalProjector);
		}
	}
	public Rectangle drawSymbol(Graphics g, RegionDataUnit regionDataUnit, GeneralProjector generalProjector) {
		DetectedRegion region = regionDataUnit.getRegion();
//		double ang = region.getPeakBearing();
//		double range = region.getPeakRange();
		// above not yest stored in database 
		double ang = (region.getMaxBearing()+region.getMinBearing())/2.;
		double range = (region.getMinRange()+region.getMaxRange())/2.;
				
		double x = -range*Math.sin(ang);
		double y = range*Math.cos(ang);
		
		Coordinate3d pos = generalProjector.getCoord3d(x,  y,  0);
		if (pos == null) {
			return null;
		}

		PamSymbol symbol = getPamSymbol(regionDataUnit, generalProjector);
		
		generalProjector.addHoverData(pos, regionDataUnit);
		
		return symbol.draw(g, pos.getXYPoint());
	}

	public Rectangle drawRThiBox(Graphics g, RegionDataUnit regionDataUnit, GeneralProjector generalProjector) {
		DetectedRegion region = regionDataUnit.getRegion();
		
		double maxAng = region.getMaxBearing();
		double minAng = region.getMinBearing();
		double minR = region.getMinRange();
		double maxR = region.getMaxRange();
		double[] x = new double[4];
		double[] y = new double[4];
		x[0] = minR;// -minR*Math.sin(minAng);
		y[0] = minAng;// minR*Math.cos(minAng);
		x[1] = maxR;// -maxR*Math.sin(minAng);
		y[1] = minAng;// maxR*Math.cos(minAng);
		x[2] = maxR;// -maxR*Math.sin(maxAng);
		y[2] = maxAng;// maxR*Math.cos(maxAng);
		x[3] = minR;// -minR*Math.sin(maxAng);
		y[3] = maxAng;// minR*Math.cos(maxAng);
		int[] xp = new int[4];
		int[] yp = new int[4];
		int minx = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (int i = 0; i < 4; i++) {
			Coordinate3d pos = generalProjector.getCoord3d(x[i], y[i], 0);
			if (pos == null) {
				// at least one corner is outside the plot rectangle, so don't draw. 
				return null; 
			}
			xp[i] = (int) Math.round(pos.x);
			yp[i] = (int) Math.round(pos.y);
			minx = Math.min(minx, xp[i]);
			maxx = Math.max(maxx, xp[i]);
			miny = Math.min(miny, yp[i]);
			maxy = Math.max(maxy, yp[i]);
		}
		
		PamSymbol symbol = getPamSymbol(regionDataUnit, generalProjector);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		
//		if it's tiny,  plot the symbol
		if (maxx-minx <= 2 || maxy-miny <=2) {
			symbol.draw(g, new Point((minx+maxx)/2, (miny+maxy)/2));
		}

		Rectangle clipRect = g.getClipBounds();
		
		if (symbol != null && symbol.isFill()) {
			g.setColor(symbol.getFillColor());
			g.fillPolygon(xp, yp, 4);
		}
		if (symbol != null) {
			g.setColor(symbol.getLineColor());
		}
		g.drawPolygon(xp, yp, 4);
//		for (int i = 0; i < xp.length; i++) {
//			g.drawLine(0, 0, xp[i], yp[i]);
//		}
		
		Shape shape = new Polygon(xp, yp, 4);
		
		generalProjector.addHoverData(shape, regionDataUnit);
		
		return null;
	}
	public Rectangle drawBox(Graphics g, RegionDataUnit regionDataUnit, GeneralProjector generalProjector) {

		DetectedRegion region = regionDataUnit.getRegion();
		
		double maxAng = region.getMaxBearing();
		double minAng = region.getMinBearing();
		double minR = region.getMinRange();
		double maxR = region.getMaxRange();
		double[] x = new double[4];
		double[] y = new double[4];
		x[0] =  -minR*Math.sin(minAng);
		y[0] =  minR*Math.cos(minAng);
		x[1] =  -maxR*Math.sin(minAng);
		y[1] =  maxR*Math.cos(minAng);
		x[2] =  -maxR*Math.sin(maxAng);
		y[2] =  maxR*Math.cos(maxAng);
		x[3] =  -minR*Math.sin(maxAng);
		y[3] =  minR*Math.cos(maxAng);
		int[] xp = new int[4];
		int[] yp = new int[4];
		int minx = Integer.MAX_VALUE;
		int maxx = Integer.MIN_VALUE;
		int miny = Integer.MAX_VALUE;
		int maxy = Integer.MIN_VALUE;
		for (int i = 0; i < 4; i++) {
			Coordinate3d pos = generalProjector.getCoord3d(x[i], y[i], 0);
			if (pos == null) {
				// at least one corner is outside the plot rectangle, so don't draw. 
				return null; 
			}
			xp[i] = (int) Math.round(pos.x);
			yp[i] = (int) Math.round(pos.y);
			minx = Math.min(minx, xp[i]);
			maxx = Math.max(maxx, xp[i]);
			miny = Math.min(miny, yp[i]);
			maxy = Math.max(maxy, yp[i]);
		}
		
		PamSymbol symbol = getPamSymbol(regionDataUnit, generalProjector);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		
//		if it's tiny,  plot the symbol
		if (maxx-minx <= 2 || maxy-miny <=2) {
			symbol.draw(g, new Point((minx+maxx)/2, (miny+maxy)/2));
		}
		
		if (symbol != null && symbol.isFill()) {
			g.setColor(symbol.getFillColor());
			g.fillPolygon(xp, yp, 4);
		}
		if (symbol != null) {
			g.setColor(symbol.getLineColor());
		}
		g.drawPolygon(xp, yp, 4);
		
		Shape shape = new Polygon(xp, yp, 4);
		
		generalProjector.addHoverData(shape, regionDataUnit);
		
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		try {
			if (parameterTypes[0] == ParameterType.BEARING) {
				return true;
			}
			if (parameterTypes.length >= 2) {
				return (parameterTypes[0] == ParameterType.RANGE && parameterTypes[1] == ParameterType.BEARING)
				|| (parameterTypes[0] == ParameterType.X && parameterTypes[1] == ParameterType.Y);
			}
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return null;
	}

}
