package tritechplugins.display.swing.layouts;

import java.awt.Point;

/**
 * information about scaling of an image and what it's aspect is when rotated. 
 * for an unrotated image, ySize is 1 and xSize is cos(maxAngle)*2
 * @author dg50
 *
 */
public class ImageAspect {
	
	private double rotDegrees;
	
	private double xMin, xMax;
	private double yMin, yMax;
	

	
	public ImageAspect(double rotDegrees, double xMin, double xMax, double yMin, double yMax) {
		super();
		this.rotDegrees = rotDegrees;
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}

	/**
	 * Ratio of x dimension to y dimension.
	 * @return
	 */
	public double getAspectRatio() {
		if (getySize() == 0) {
			return 1;
		}
		else {
			return getxSize()/getySize();
		}
	}
	
	public Point getCentre() {
		Point p = new Point((int) -(xMin/(xMax-xMin)), (int) (yMax+yMin)/2);
		return p;
	}

	/**
	 * @return the xMin
	 */
	public double getxMin() {
		return xMin;
	}

	/**
	 * @return the xMax
	 */
	public double getxMax() {
		return xMax;
	}

	/**
	 * @return the yMin
	 */
	public double getyMin() {
		return yMin;
	}

	/**
	 * @return the yMax
	 */
	public double getyMax() {
		return yMax;
	}

	/**
	 * @return the rotDegrees
	 */
	public double getRotDegrees() {
		return rotDegrees;
	}

	/**
	 * @return the xSize
	 */
	public double getxSize() {
		return xMax-xMin;
	}

	/**
	 * @return the ySize
	 */
	public double getySize() {
		return yMax-yMin;
	}
}
