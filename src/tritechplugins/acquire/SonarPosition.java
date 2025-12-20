package tritechplugins.acquire;

import java.io.Serializable;

/**
 * Position data for a single sonar. 
 * @author dg50
 *
 */
public class SonarPosition implements Cloneable, Serializable{

	private static final long serialVersionUID = 1L;

	private double x,y,height;

	private double head, pitch, roll;
	
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


}
