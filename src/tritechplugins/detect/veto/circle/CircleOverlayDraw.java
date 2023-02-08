package tritechplugins.detect.veto.circle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import tritechplugins.display.swing.SonarXYProjector;

public class CircleOverlayDraw extends PanelOverlayDraw {

	public CircleOverlayDraw() {
		super(null);
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		SpatialVetoDataUnit spatialUnit = (SpatialVetoDataUnit) pamDataUnit;
		SpatialVeto spatialVeto = spatialUnit.getSpatialVeto();
		if (spatialVeto instanceof CircleVeto == false) {
			return null;
		}
		CircleVeto circleVeto = (CircleVeto) spatialVeto;
		CircleVetoParams params = circleVeto.getParams();
		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
		Coordinate3d centre = sonarProjector.getCoord3d(params.centreX, params.centreY, false);
		Coordinate3d edge = sonarProjector.getCoord3d(params.centreX+params.radius, params.centreY, false);
		if (edge == null || centre == null) {
			return null;
		}
		double r = Math.sqrt(Math.pow(edge.x-centre.x,2) + Math.pow(edge.y-centre.y, 2));
		g.drawArc((int) (centre.x-r), (int) (centre.y-r), (int) (2*r), (int) (2*r), 0, 359);
		
		generalProjector.addHoverData(centre, pamDataUnit);

//		Graphics2D g2d = (Graphics2D) g;
//		PamSymbol symbol = getDefaultSymbol();
//		if (symbol.isFill()) {
//			Color fillCol = symbol.getFillColor();
//			fillCol = new Color(fillCol.getRed(), fillCol.getGreen(), fillCol.getBlue(), 128);
//			g2d.setColor(fillCol);
//			g2d.fill(polygon);
//		}
		
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return null;
	}

}
