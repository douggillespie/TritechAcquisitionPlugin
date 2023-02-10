package tritechplugins.detect.veto.polygon;

import tritechplugins.detect.veto.SpatialVetoParams;
import tritechplugins.detect.veto.SpatialVetoProvider;

public class PolygonParams extends SpatialVetoParams {

	private double[] x;
	private double[] y;
	
	public PolygonParams(SpatialVetoProvider provider) {
		super(provider);
	}

	/**
	 * @return the x
	 */
	public double[] getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double[] x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double[] getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double[] y) {
		this.y = y;
	}

	/**
	 * Get the number of points in the polygon
	 * @return
	 */
	public int getNumPoints() {
		if (x == null) {
			return 0;
		}
		else {
			return x.length;
		}
	}
}
