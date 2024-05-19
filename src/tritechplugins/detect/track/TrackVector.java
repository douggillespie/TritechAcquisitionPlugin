package tritechplugins.detect.track;

import PamUtils.PamUtils;
import pamMaths.PamVector;

/**
 * Sonar track vectors (calculated in TrackDirection class)
 * <p>Data are stored as a vector in Cartesian coordinates
 * @author dg50
 *
 */
public class TrackVector extends PamVector {

	public static final long serialVersionUID = 1L;
	private long startMillis;
	private long endMillis;
	private double startX;
	private double startY;

	public TrackVector(double x0, double y0, double x1, double y1, long start, long end) {
		super(x1-x0, y1-y0, 0);
		this.startX = x0;
		this.startY = y0;
		this.startMillis = start;
		this.endMillis = end;
	}
	
	/**
	 * Get the velocity as a vector. 
	 * @return
	 */
	public PamVector getVelocity() {
		PamVector v = new PamVector(this);
		double tS = getSeconds();
		double[] vec = v.getVector();
		vec[0]/=tS;
		vec[1]/=tS;
		return v;
	}
	
	/**
	 * Get the speed as a scalar. 
	 * @return
	 */
	public double getSpeed() {
		double len = this.norm();
		return len / getSeconds();
	}
	
	/**
	 * Get the heading direction relative to the sonar zero 
	 * angle, which is straight up (pseudo north). 
	 * @return relative heading (-180 < heading <= 180)
	 */
	public double getHeading() {
		double angle = Math.toDegrees(Math.atan2(this.getElement(0), this.getElement(1)));
		angle = PamUtils.constrainedAngle(angle, 180);
		return angle;
	}
	
	/**
	 * Get the track heading relative to a specified flow direction
	 * @param relativeTo flow direction
	 * @return relative heading (-180 < heading <= 180)
	 */
	public double getRelativeHeading(double relativeTo) {
		double head = getHeading()-relativeTo;
		return PamUtils.constrainedAngle(head, 180);
	}

	/**
	 * Get duration of vector in seconds. 
	 * @return
	 */
	public double getSeconds() {
		return (endMillis-startMillis)/1000.;
	}
	
	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @return the start X coordinate
	 */
	public double getStartX() {
		return startX;
	}

	/**
	 * @return the start Y coordinate
	 */
	public double getStartY() {
		return startY;
	}
	/**
	 * @return the end x coordinate. 
	 */
	public double getEndX() {
		return startX + getElement(0);
	}

	/**
	 * @return the end y coordinate
	 */
	public double getEndY() {
		return startY + getElement(1);
	}


}
