package tritechplugins.detect.veto.polygon;

import java.awt.Window;
import java.awt.geom.Path2D;

import java.awt.geom.Point2D;
import tritechplugins.detect.veto.SpatialVeto;
import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;
import tritechplugins.detect.veto.VetoSettingsDialog;

public class PolygonVeto extends SpatialVeto {

	private PolygonParams polygonParams;
	
	private Path2D polygon;
	
	public PolygonVeto(SpatialVetoProvider vetoProvider) {
		super(vetoProvider);
		this.polygonParams = new PolygonParams(vetoProvider);
	}

	@Override
	public boolean configureVeto(Window owner) {
		PolygonVetoDialogPanel polygonVetoDialogPanel = new PolygonVetoDialogPanel(this);
		boolean ans = VetoSettingsDialog.showDialog(owner, this, polygonVetoDialogPanel);
		
		makePolygon();
		
		return ans;
	}

	@Override
	public boolean isInVeto(double x, double y) {
		if (polygon == null) {
			return false;
		}
		boolean isIn = polygon.contains(new Point2D.Double(x, y));
//		System.out.printf("%3.1f,%3.1f is %s\n", x,y,isIn ? "In" : "Out");
		return isIn;
	}

	@Override
	public PolygonParams getParams() {
		return polygonParams;
	}

	@Override
	public void setParams(SpatialVetoParams params) {
		if (params instanceof PolygonParams) {
			this.polygonParams = (PolygonParams) params;
		}
		makePolygon();
	}

	/*
	 * Make a awt Path2D which can be used to see if 
	 * points are in it or not. 
	 */
	private void makePolygon() {
		if (polygonParams.getNumPoints() == 0) {
			polygon = null;
			return;
		}
		polygon = new Path2D.Double(Path2D.WIND_NON_ZERO, polygonParams.getNumPoints());
		double xx[] = polygonParams.getX();
		double yy[] = polygonParams.getY();
		polygon.moveTo(xx[0], yy[0]);
		for (int i = 1; i < xx.length; i++) {
			polygon.lineTo(xx[i], yy[i]);
		}
		polygon.closePath();
	}

	@Override
	public String getDescription() {
		if (polygonParams.getNumPoints() == 0) {
			return "Polygon Veto: Currently not set";
		}
		return String.format("Polygon Veto with %d vertices", polygonParams.getNumPoints());
	}

	/**
	 * @return the polygon
	 */
	public Path2D getPolygon() {
		return polygon;
	}

}
