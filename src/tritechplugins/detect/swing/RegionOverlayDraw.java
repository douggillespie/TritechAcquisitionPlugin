package tritechplugins.detect.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
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
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.swing.SonarOverlayDraw;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.track.TrackLinkDataUnit;
import tritechplugins.detect.track.TrackLinkProcess;
import tritechplugins.display.swing.SonarRThiProjector;
import tritechplugins.display.swing.overlays.SonarSymbolChooser;
import tritechplugins.display.swing.overlays.SonarSymbolOptions;

/**
 * Uses simple x,y projector to draw outlines of regions for given frame. 
 * @author dg50
 *
 */
public class RegionOverlayDraw extends SonarOverlayDraw {

	public static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS, 2, 2, false, Color.BLUE, Color.BLUE);
	
	private Color highlightCol = Color.YELLOW;

	private ThresholdDetector thresholdDetector;
	
	public RegionOverlayDraw(ThresholdDetector thresholdDetector) {
		super(defaultSymbol);
		this.thresholdDetector = thresholdDetector;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		RegionDataUnit regionDataUnit = (RegionDataUnit) pamDataUnit;
		ParameterType[] parameterTypes = generalProjector.getParameterTypes();
		if ((parameterTypes[0] == ParameterType.RANGE && parameterTypes[1] == ParameterType.BEARING)) {
			return drawOnSonarDisplay(g, regionDataUnit, generalProjector);
		}
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return drawOnMap(g, regionDataUnit, generalProjector);
		}
		
		return null;
	}
	private Rectangle drawOnMap(Graphics g, RegionDataUnit regionDataUnit, GeneralProjector generalProjector) {

		if (generalProjector instanceof MapRectProjector == false) {
			return null;
		}
		DetectedRegion region = regionDataUnit.getRegion();
		MapRectProjector mapProj = (MapRectProjector) generalProjector;
		/*
		 *  doesn't work because we dont' have access to the sonar Record, so can just do this manually 
		 *  by working out the lat long of the point using standard transforms. 
		 */
//		regionDataUnit.get
//		MapRectProjector mapProj = (MapRectProjector) generalProjector;
//		Graphics2D g2d = (Graphics2D) g.create();
//		Rectangle r = setupDrawRectangle(g2d, mapProj, sonarRecord);
		LatLong origin = getStreamerOrigin(0, regionDataUnit.getTimeMilliseconds());
		SonarPosition sonarPosition = getSonarPosition(regionDataUnit.getSonarId());
		origin = origin.addDistanceMeters(sonarPosition.getX(), sonarPosition.getY());

		PamSymbol symbol = getPamSymbol(regionDataUnit, generalProjector);
		
		PamSymbolChooser symbolChooser = generalProjector.getPamSymbolChooser();
		SonarSymbolOptions symbolOptions;
		if (symbolChooser instanceof SonarSymbolChooser) {
			SonarSymbolChooser sonarSymbolChooser = (SonarSymbolChooser) symbolChooser;
			symbolOptions = sonarSymbolChooser.getSymbolOptions();
		}
		else {
			symbolOptions = new SonarSymbolOptions();
		}
		if (symbolOptions.symbolType == SonarSymbolOptions.DRAW_BOX) {
			return drawMapBox(g, regionDataUnit, sonarPosition, origin, mapProj, symbol);
		}
		else {
			return drawMapSymbol(g, regionDataUnit, sonarPosition, origin, mapProj, symbol);
		}
		

	}

	private Rectangle drawMapBox(Graphics g, RegionDataUnit regionDataUnit, SonarPosition sonarPosition, LatLong origin,
			MapRectProjector mapProj, PamSymbol symbol) {
		int[] x = new int[4];
		int[] y = new int[4];
		double a, r;
		DetectedRegion region = regionDataUnit.getRegion();
		// work through the four corners. 
		a = -Math.toDegrees(region.getMinBearing()) + sonarPosition.getHead();
		r = region.getMinRange();
		LatLong coord = origin.travelDistanceMeters(a, r);
		Coordinate3d pos = mapProj.getCoord3d(coord);
		x[0] = (int) pos.x;
		y[0] = (int) pos.y;

		a = -Math.toDegrees(region.getMinBearing()) + sonarPosition.getHead();
		r = region.getMaxRange();
		 coord = origin.travelDistanceMeters(a, r);
		 pos = mapProj.getCoord3d(coord);
		x[1] = (int) pos.x;
		y[1] = (int) pos.y;

		a = -Math.toDegrees(region.getMaxBearing()) + sonarPosition.getHead();
		r = region.getMaxRange();
		 coord = origin.travelDistanceMeters(a, r);
		 pos = mapProj.getCoord3d(coord);
		x[2] = (int) pos.x;
		y[2] = (int) pos.y;

		a = -Math.toDegrees(region.getMaxBearing()) + sonarPosition.getHead();
		r = region.getMinRange();
		 coord = origin.travelDistanceMeters(a, r);
		 pos = mapProj.getCoord3d(coord);
		x[3] = (int) pos.x;
		y[3] = (int) pos.y;
		
		if (symbol.isFill()) {
			g.setColor(symbol.getFillColor());
			g.fillPolygon(x, y, 4);
		}
		g.setColor(symbol.getLineColor());
		g.drawPolygon(x, y, 4);

		Shape shape = new Polygon(x, y, 4);
		mapProj.addHoverData(shape, regionDataUnit);
		
		return null;
	}

	private Rectangle drawMapSymbol(Graphics g, RegionDataUnit regionDataUnit, 
			SonarPosition sonarPosition, LatLong origin, MapRectProjector mapProj,
			PamSymbol symbol) {		
		DetectedRegion region = regionDataUnit.getRegion();
		double a = -Math.toDegrees(region.getPeakBearing()) + sonarPosition.getHead();
		double r = region.getPeakRange();
		LatLong coord = origin.travelDistanceMeters(a, r);
		Coordinate3d pos = mapProj.getCoord3d(coord);

		Rectangle dr = symbol.draw(g, pos.getXYPoint());
		mapProj.addHoverData(pos, regionDataUnit);
		return dr;
	}

	private TrackLinkProcess getTrackLinkProcess() {
		return thresholdDetector.getTrackLinkProcess();
	}

	public Rectangle drawOnSonarDisplay(Graphics g, RegionDataUnit regionDataUnit, GeneralProjector generalProjector) {	
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
						
		Coordinate3d pos = generalProjector.getCoord3d(range, ang,  0);
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
		g2d.setStroke(new BasicStroke(symbol.getLineThickness()));
		
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
				if ((parameterTypes[0] == ParameterType.RANGE && parameterTypes[1] == ParameterType.BEARING)
				|| (parameterTypes[0] == ParameterType.X && parameterTypes[1] == ParameterType.Y)) {
					return true;
				}
			}
		}
		catch (Exception e) {
			return false;
		}
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true; // can draw on map
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

	@Override
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector projector) {
		PamSymbol symbol = super.getPamSymbol(pamDataUnit, projector);
		boolean highlight = isHighlighted(pamDataUnit, projector);
		if (highlight) {
			symbol = symbol.clone();
			symbol.setFillColor(highlightCol);
			symbol.setLineColor(highlightCol);
//			g2d.setStroke(new BasicStroke(symbol.getLineThickness()+1));
		}
		return symbol;
	}
	
	private boolean isHighlighted(PamDataUnit pamDataUnit, GeneralProjector projector) {
		RegionDataUnit regionDataUnit = (RegionDataUnit) pamDataUnit;
		DetectedRegion region = regionDataUnit.getRegion();
		// try to work out if it's a clicked on track, in which case we'll colour it differently. 
		if (projector instanceof SonarRThiProjector) {
			SonarRThiProjector rtProj = (SonarRThiProjector) projector;
			TrackLinkDataUnit clickedTrack = rtProj.getSonarImagePanel().getClickedOnTrack();
			if (clickedTrack != null && regionDataUnit.getSuperDetection(TrackLinkDataUnit.class)== clickedTrack) {
				return true;
			}
		}
		return false;
		
	}

	@Override
	public SonarPosition getSonarPosition(int sonarId) {
		TritechAcquisition daq = thresholdDetector.getTrackLinkProcess().getTritechAcquisition();
		if (daq == null) {
			return new SonarPosition();
		}
		return daq.getDaqParams().getSonarPosition(sonarId);
	}

}
