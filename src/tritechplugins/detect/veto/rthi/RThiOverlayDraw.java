package tritechplugins.detect.veto.rthi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoDataUnit;
import tritechplugins.detect.veto.circle.CircleVeto;
import tritechplugins.detect.veto.circle.CircleVetoParams;
import tritechplugins.display.swing.SonarXYProjector;

public class RThiOverlayDraw extends PanelOverlayDraw {

	public RThiOverlayDraw(PamSymbol defaultSymbol) {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		SpatialVetoDataUnit spatialUnit = (SpatialVetoDataUnit) pamDataUnit;
		SpatialVeto spatialVeto = spatialUnit.getSpatialVeto();
		if (spatialVeto instanceof RThiVeto == false) {
			return null;
		}
		RThiVeto rthiVeto = (RThiVeto) spatialVeto;
		RThiVetoParams params = rthiVeto.getParams();
		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
		Coordinate3d centre = sonarProjector.getCoord3d(0, 0, false);
//		Coordinate3d c1 = sonarProjector.getCoord3d(params.rangeMin*Math.sin(params.angleMin), params.rangeMin*Math.cos(params.angleMin), false);
//		Coordinate3d c2 = sonarProjector.getCoord3d(params.rangeMax*Math.sin(params.angleMin), params.rangeMax*Math.cos(params.angleMin), false);
//		Coordinate3d c3 = sonarProjector.getCoord3d(params.rangeMax*Math.sin(params.angleMax), params.rangeMax*Math.cos(params.angleMax), false);
//		Coordinate3d c4 = sonarProjector.getCoord3d(params.rangeMin*Math.sin(params.angleMax), params.rangeMin*Math.cos(params.angleMax), false);
		Coordinate3d c1 = sonarProjector.getCoord3d(params.rangeMin, -params.angleMin, false);
		Coordinate3d c2 = sonarProjector.getCoord3d(params.rangeMax, -params.angleMin, false);
		Coordinate3d c3 = sonarProjector.getCoord3d(params.rangeMax, -params.angleMax, false);
		Coordinate3d c4 = sonarProjector.getCoord3d(params.rangeMin, -params.angleMax, false);
		int np = 4;
		int[] x = {(int) c1.x, (int) c2.x, (int) c3.x, (int) c4.x};
		int[] y = {(int) c1.y, (int) c2.y, (int) c3.y, (int) c4.y};
		Polygon polygon = new Polygon(x, y, np);
		Graphics2D g2d = (Graphics2D) g;
		// draw two lines and two arcs. 
		g2d.drawLine(x[0], y[0], x[1], y[1]);
		g2d.drawLine(x[2], y[2], x[3], y[3]);
		int deg1 = 90-(int) Math.round(Math.toDegrees(params.angleMax));
		int deg2 = (int) Math.round(Math.toDegrees(params.angleMax-params.angleMin));
		double r1 = (int) Math.round(Math.sqrt(Math.pow(c2.x-centre.x, 2) + Math.pow(c2.y-centre.y, 2)));
		g2d.drawArc((int) (centre.x-r1), (int) (centre.y-r1), (int) (2*r1), (int) (2*r1), deg1, deg2);
		r1 = (int) Math.sqrt(Math.pow(c1.x-centre.x, 2) + Math.pow(c1.y-centre.y, 2));
		if (r1 > 0) {
			g2d.drawArc((int) (centre.x-r1), (int) (centre.y-r1), (int) (2*r1), (int) (2*r1), deg1, deg2);
		}
		
		int cx = 0, cy = 0;
		for (int i = 0; i < 4; i++) {
			cx += x[i];
			cy += y[i];
		}
		cx/=4;
		cy/=4;
//		PamSymbol symbol = getDefaultSymbol();
//		if (symbol.isFill()) {
//			Color fillCol = symbol.getFillColor();
//			fillCol = new Color(fillCol.getRed(), fillCol.getGreen(), fillCol.getBlue(), 128);
//			g2d.setColor(fillCol);
//			g2d.fill(polygon);
//		}
		generalProjector.addHoverData(new Coordinate3d(cx, cy, 0), pamDataUnit);
//		if (edge == null || centre == null) {
//			return null;
//		}
		
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		// TODO Auto-generated method stub
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
