package tritechplugins.detect.veto.xy;

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
import tritechplugins.detect.veto.rthi.RThiVeto;
import tritechplugins.detect.veto.rthi.RThiVetoParams;
import tritechplugins.display.swing.SonarXYProjector;

public class XYOverlayDraw extends PanelOverlayDraw {

	public XYOverlayDraw(PamSymbol defaultSymbol) {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		SpatialVetoDataUnit spatialUnit = (SpatialVetoDataUnit) pamDataUnit;
		SpatialVeto spatialVeto = spatialUnit.getSpatialVeto();
		if (spatialVeto instanceof XYVeto == false) {
			return null;
		}
		XYVeto xyVeto = (XYVeto) spatialVeto;
		XYVetoParams params = xyVeto.getParams();
		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
		double tlAng = -Math.atan2(params.xMin, params.yMax);
		double tlR = Math.sqrt(params.xMin*params.xMin + params.yMax*params.yMax);
		double brAng = -Math.atan2(params.xMax, params.yMin);
		double brR = Math.sqrt(params.xMax*params.xMax + params.yMin*params.yMin);
		
		Coordinate3d topLeft = sonarProjector.getCoord3d(tlR, tlAng, false);
		Coordinate3d botRight = sonarProjector.getCoord3d(brR, brAng, false);
		int np = 4;
		int[] x = {(int) topLeft.x, (int) topLeft.x, (int) botRight.x, (int) botRight.x};
		int[] y = {(int) topLeft.y, (int) botRight.y, (int) botRight.y, (int) topLeft.y};
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
		
		return polygon.getBounds();
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
