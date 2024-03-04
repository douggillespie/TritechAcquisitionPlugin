package tritechplugins.detect.veto.polygon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;

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
import tritechplugins.display.swing.SonarRThiProjector;
import tritechplugins.display.swing.SonarXYProjector;

public class PolygonOverlayDraw extends PanelOverlayDraw {

	public PolygonOverlayDraw(PamSymbol defaultSymbol) {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		SpatialVetoDataUnit spatialUnit = (SpatialVetoDataUnit) pamDataUnit;
		SpatialVeto spatialVeto = spatialUnit.getSpatialVeto();
		if (spatialVeto instanceof PolygonVeto == false) {
			return null;
		}
		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
		PolygonVeto polygonVeto = (PolygonVeto) spatialVeto;
		PolygonParams params = polygonVeto.getParams();
		int n = params.getNumPoints();
		if (n == 0) {
			return null;
		}
		// sonar projector has been converted to r/phi. 
		int[] xx = new int[n];
		int[] yy = new int[n];
		double[] x = params.getX();
		double[] y = params.getY();
		for (int i = 0; i < n; i++) {
			double p1 = x[i];
			double p2 = y[i];
			if (generalProjector instanceof SonarRThiProjector) {
				p1 = Math.sqrt(x[i]*x[i] + y[i]*y[i]);
				p2 = -Math.atan2(x[i], y[i]);
			}
			Coordinate3d coord = sonarProjector.getCoord3d(p1, p2, false);
			xx[i] = (int) coord.x;
			yy[i] = (int) coord.y;
		}
		Rectangle clipRect = g.getClipBounds();
		g.drawPolygon(xx, yy, n);
		
		
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
