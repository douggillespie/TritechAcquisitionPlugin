package tritechplugins.display.swing;

import java.awt.Point;
import java.awt.Rectangle;

import tritechplugins.display.swing.layouts.ImageAspect;

/**
 * Info for a sonar layout. Includes a rectangle for the image
 * and a point which will be the top left corner of a block of text. 
 * @author dg50
 *
 */
public class LayoutInfo {

	/**
	 * Rectangle - position of a transparent JPanel that will hold the image.
	 */
	private Rectangle imageRectangle;
	
	private int sonarId;
	
	/**
	 * Position of sonar vertex, relative to imageRectangle
	 * defaults to (imageRectange.width/2, 0)
	 */
	private Point vertex;
	
	/**
	 * Clockwise rotation of image about vertex, i.e. if 
	 * you put the vertex at (imageRectangle.width/2, imageRectangle.height) you're going to need
	 * a rotation of 180 to be able to see it - or you could have a vertex of
	 * (0, imageRectangle.height/2) and 90 rotation, etc. 
	 */
	private double rotationDegrees;
	
	/**
	 * Position of image data text. 
	 */
	private Point textPoint;

	private ImageAspect imageAspect;

	public LayoutInfo(int sonarId, ImageAspect imageAspect, Rectangle imageRectangle, Point textPoint) {
		super();
		this.sonarId = sonarId;
		this.imageAspect = imageAspect;
		this.imageRectangle = imageRectangle;
		this.textPoint = textPoint;
	}

	public LayoutInfo(int sonarId, ImageAspect imageAspect, Rectangle imageRectangle) {
		super();
		this.sonarId = sonarId;
		this.imageAspect = imageAspect;
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

	/**
	 * @return the rotationDegrees
	 */
	public double getRotationDegrees() {
		return rotationDegrees;
	}

	/**
	 * @param rotationDegrees the rotationDegrees to set
	 */
	public void setRotationDegrees(double rotationDegrees) {
		this.rotationDegrees = rotationDegrees;
	}

	/**
	 * @param imageRectangle the imageRectangle to set
	 */
	public void setImageRectangle(Rectangle imageRectangle) {
		this.imageRectangle = imageRectangle;
	}

	/**
	 * @return the vertex
	 */
	public Point getVertex() {
		if (vertex == null && imageRectangle != null) {
			vertex = new Point(imageRectangle.width/2, 0);
		}
		return vertex;
	}

	/**
	 * @param vertex the vertex to set
	 */
	public void setVertex(Point vertex) {
		this.vertex = vertex;
	}

	public int getSonarId() {
		return sonarId;
	}

	/**
	 * @param sonarId the sonarId to set
	 */
	public void setSonarId(int sonarId) {
		this.sonarId = sonarId;
	}

	/**
	 * @return the imageAspect
	 */
	public ImageAspect getImageAspect() {
		return imageAspect;
	}

}
