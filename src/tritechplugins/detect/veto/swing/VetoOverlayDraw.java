package tritechplugins.detect.veto.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoDataUnit;
import tritechplugins.display.swing.SonarXYProjector;
import tritechplugins.display.swing.SonarZoomTransform;

public class VetoOverlayDraw extends PanelOverlayDraw {

	public static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 10, 16, false, Color.RED, Color.red);
	
	public VetoOverlayDraw() {
		super(defaultSymbol);
	}
	

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (pamDataUnit instanceof SpatialVetoDataUnit == false) {
			return null;
		}
		SpatialVetoDataUnit spatialUnit = (SpatialVetoDataUnit) pamDataUnit;
		SpatialVeto spatialVeto = spatialUnit.getSpatialVeto();
		
		PanelOverlayDraw vetoDraw = spatialVeto.getVetoProvider().getVetoOverlayDraw();
		if (vetoDraw == null) {
			return null;
		}
		
		if (shouldPlot(spatialVeto, generalProjector) == false) {
			return null;
		}
		
		// set a clip region to be only within the sonar image. 
//		Graphics2D g2d = (Graphics2D) g;
//		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
//		SonarZoomTransform zoomTransform = sonarProjector.getSonarZoomTransform();
//		double r = zoomTransform.getMaximumRange();
//		Coordinate3d centre = sonarProjector.getCoord3d(0, 0, false);
//		Coordinate3d topleft = sonarProjector.getCoord3d(-r, r, false);
////		double screenR = Math.abs(top.y-centre.y);
//		int x = (int) (topleft.x);
//		int y = (int) topleft.y;
//		int w = (int) (2*(centre.x-topleft.x));
//		int h = (int) (1*centre.y-topleft.y);
		
//		int n = 5;
//		int xx[] = {x+w/2, x, x, x+w, x+w};
//		int yy[] = {y+h, y+h/2, y, y, y+h/2}; 
//		
		
//		Shape clip = new Arc2D.Double(x,y,w,h, 30, 120, 0);
//		Shape clip = new Polygon(xx, yy, n);
//		g2d.setColor(Color.RED);
//		g2d.draw(clip);
//		g2d.setClip(clip); // this line is bad. Very bad. it stops anything every drawng outside that clip
//		g2d.fill(clip);

		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		
		if (symbol != null) {
			vetoDraw.setDefaultSymbol(symbol);
			g.setColor(symbol.getLineColor());
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(symbol.getLineThickness()));
		}
		Rectangle rect = vetoDraw.drawDataUnit(g, pamDataUnit, generalProjector);
		return rect;
	}

	/**
	 * Check the veto applies to this sonar
	 * @param spatialVeto
	 * @param generalProjector
	 * @return
	 */
	private boolean shouldPlot(SpatialVeto spatialVeto, GeneralProjector generalProjector) {
		int sonar = spatialVeto.getParams().getSonarId();
		if (sonar == 0) {
			return true;
		}
		if (generalProjector instanceof SonarXYProjector == false) {
			return true;
		}
		SonarXYProjector sonarProjector = (SonarXYProjector) generalProjector;
		return (sonarProjector.getSonarID() == sonar);
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		try {
			if (parameterTypes.length < 2) {
				return false;
			}
			if (parameterTypes[0] == ParameterType.RANGE && parameterTypes[1] == ParameterType.BEARING) {
				return true;
			}
//			if (parameterTypes.length >= 2) {
//				return parameterTypes[0] == ParameterType.X && parameterTypes[1] == ParameterType.Y;
//			}
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
