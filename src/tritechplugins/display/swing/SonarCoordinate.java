package tritechplugins.display.swing;

/**
 * Little class for passing around sonar coordinates. 
 * @author dg50
 *
 */
public class SonarCoordinate {

	private int sonarId;
	private double x, y;
	private int sonarIndex;


	/**
	 * 
	 * @param sonarIndex sonar index (generally set to Id)
	 * @param sonarId sonar Id
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param yr 
	 */
	public SonarCoordinate(int sonarIndex, int sonarId, double x, double y) {
		this.sonarIndex = sonarIndex;
		this.sonarId = sonarId;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the sonarIndex
	 */
	public int getSonarIndex() {
		return sonarIndex;
	}
	
	/**
	 * 
	 * @return the sonar Id
	 */
	public int getSonarId() {
		return sonarId;
	}

	/**
	 * 
	 * @return the x coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * 
	 * @return the y coordinate
	 */
	public double getY() {
		return y;
	}

	/**
	 * 
	 * @return the angle in radians measured clockwise from top. 
	 */
	public double getAngleRadians() {
		return Math.atan2(x, y);	
	}

	/**
	 * 
	 * @return the angle in degrees measured clockwise from top. 
	 */
	public double getAngleDegrees() {
		return Math.toDegrees(getAngleRadians());
	}

	/**
	 * 
	 * @return the radial distance from the centre. 
	 */
	public double getRange() {
		return Math.sqrt(x*x+y*y);
	}

}
