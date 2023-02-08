package tritechplugins.detect.veto.rthi;

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
		Coordinate3d c1 = sonarProjector.getCoord3d(params.rangeMin*Math.sin(params.angleMin), params.rangeMin*Math.cos(params.angleMin), false);
		Coordinate3d c2 = sonarProjector.getCoord3d(params.rangeMax*Math.sin(params.angleMin), params.rangeMax*Math.cos(params.angleMin), false);
		Coordinate3d c3 = sonarProjector.getCoord3d(params.rangeMax*Math.sin(params.angleMax), params.rangeMax*Math.cos(params.angleMax), false);
		Coordinate3d c4 = sonarProjector.getCoord3d(params.rangeMin*Math.sin(params.angleMax), params.rangeMin*Math.cos(params.angleMax), false);
		int np = 4;
		int[] x = {(int) c1.x, (int) c2.x, (int) c3.x, (int) c4.x};
		int[] y = {(int) c1.y, (int) c2.y, (int) c3.y, (int) c4.y};
		Polygon polygon = new Polygon(x, y, np);
		Graphics2D g2d = (Graphics2D) g;
		g2d.draw(polygon);
		PamSymbol symbol = getDefaultSymbol();
		if (symbol.isFill()) {
			Color fillCol = symbol.getFillColor();
			fillCol = new Color(fillCol.getRed(), fillCol.getGreen(), fillCol.getBlue(), 128);
			g2d.setColor(fillCol);
			g2d.fill(polygon);
		}
		generalProjector.addHoverData(polygon, pamDataUnit);
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
