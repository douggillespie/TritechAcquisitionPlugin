package tritechplugins.acquire;

import java.io.Serializable;

/**
 * Position data for a single sonar. 
 * @author dg50
 *
 */
public class SonarPosition implements Cloneable, Serializable{

	private static final long serialVersionUID = 1L;
	
	private String sonarName; // friendly name, such as 'upper', 'far bank', etc. 

	private double x,y,height;

	private double head, pitch, roll;
	
	private boolean flipLR;
	
	@Override
	public SonarPosition clone() {
		try {
			return (SonarPosition) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Return true if everything is zero, so no point in doing any transforms. 
	 * @return
	 */
	public boolean isZero() {
		return x == 0 && y == 0 && height == 0 && head == 0 && pitch == 0 && roll == 0; 
	}
	
	/**
	 * Translate x and y;
	 * @param relx
	 * @param rely
	 * @return two element vector of translated xy
	 */
	public double[] translate(double relx, double rely) {
		double[] newXY = {relx, rely};
		if (isZero()) {
			return newXY;
		}
		if (head != 0) {
			double c = Math.cos(Math.toRadians(head));
			double s = Math.sin(Math.toRadians(head));
			newXY[0] = relx*c + rely*s;
			newXY[1] = -relx*s + rely*c;
		}
		newXY[0] += this.x;
		newXY[1] += this.y;
		return newXY;
	}
	
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @return the head
	 */
	public double getHead() {
		return head;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(double head) {
		this.head = head;
	}

	/**
	 * @return the pitch
	 */
	public double getPitch() {
		return pitch;
	}

	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(double pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the roll
	 */
	public double getRoll() {
		return roll;
	}

	/**
	 * @param roll the roll to set
	 */
	public void setRoll(double roll) {
		this.roll = roll;
	}

	/**
	 * @return the sonarName
	 */
	public String getSonarName() {
		return sonarName;
	}

	/**
	 * @param sonarName the sonarName to set
	 */
	public void setSonarName(String sonarName) {
		this.sonarName = sonarName;
	}

	/**
	 * @return the flipLR
	 */
	public boolean isFlipLR() {
		return flipLR;
	}

	/**
	 * @param flipLR the flipLR to set
	 */
	public void setFlipLR(boolean flipLR) {
		this.flipLR = flipLR;
	}


}
