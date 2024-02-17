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
		double centR = Math.sqrt(params.centreX*params.centreX + params.centreY*params.centreY);
		double centA = -Math.atan2(params.centreX, params.centreY);
		double edgeR = Math.sqrt(Math.pow(params.centreX+params.radius,2) + 
				Math.pow(params.centreY,2));
		double edgeA = -Math.atan2(params.centreX+params.radius, params.centreY);
		Coordinate3d centre = sonarProjector.getCoord3d(centR, centA, false);
		Coordinate3d edge = sonarProjector.getCoord3d(edgeR, edgeA, false);
		if (edge == null || centre == null) {
			return null;
		}
		double r = Math.sqrt(Math.pow(edge.x-centre.x,2) + Math.pow(edge.y-centre.y, 2));
		g.drawArc((int) (centre.x-r), (int) (centre.y-r), (int) (2*r), (int) (2*r), 0, 359);
		
		generalProjector.addHoverData(centre, pamDataUnit);
	
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
