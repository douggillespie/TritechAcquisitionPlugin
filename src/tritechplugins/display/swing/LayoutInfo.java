package tritechplugins.display.swing;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Info for a sonar layout. Includes a rectangle for the image
 * and a point which will be the top left corner of a block of text. 
 * @author dg50
 *
 */
public class LayoutInfo {

	private Rectangle imageRectangle;
	
	private Point textPoint;

	public LayoutInfo(Rectangle imageRectangle, Point textPoint) {
		super();
		this.imageRectangle = imageRectangle;
		this.textPoint = textPoint;
	}

	public LayoutInfo(Rectangle imageRectangle) {
		super();
		this.imageRectangle = imageRectangle;
		textPoint = new Point(imageRectangle.x, imageRectangle.y);
	}

	/**
	 * @return the imageRectangle
	 */
	public Rectangle getImageRectangle() {
		return imageRectangle;
	}

	/**
	 * @return the textPoint
	 */
	public Point getTextPoint() {
		return textPoint;
	}

}
