package tritechplugins.detect;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import tritechgemini.detect.DetectedRegion;

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
		double maxAng = region.getMaxBearing();
		double minAng = region.getMinBearing();
		double minR = region.getMinRange();
		double maxR = region.getMaxRange();
		double[] x = new double[4];
		double[] y = new double[4];
		x[0] =  minR*Math.sin(minAng);
		y[0] =  minR*Math.cos(minAng);
		x[1] =  maxR*Math.sin(minAng);
		y[1] =  maxR*Math.cos(minAng);
		x[2] =  maxR*Math.sin(maxAng);
		y[2] =  maxR*Math.cos(maxAng);
		x[3] =  minR*Math.sin(maxAng);
		y[3] =  minR*Math.cos(maxAng);
		int[] xp = new int[4];
		int[] yp = new int[4];
		for (int i = 0; i < 4; i++) {
			Coordinate3d pos = generalProjector.getCoord3d(x[i], y[i], 0);
			xp[i] = (int) Math.round(pos.x);
			yp[i] = (int) Math.round(pos.y);
		}
		
		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		if (symbol != null && symbol.isFill()) {
			g.setColor(symbol.getFillColor());
			g.fillPolygon(xp, yp, 4);
		}
		if (symbol != null) {
			g.setColor(symbol.getLineColor());
		}
		g.drawPolygon(xp, yp, 4);
		
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		try {
			return parameterTypes[0] == ParameterType.X && parameterTypes[1] == ParameterType.Y;
		}
		catch (Exception e) {
			return false;
		}
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
