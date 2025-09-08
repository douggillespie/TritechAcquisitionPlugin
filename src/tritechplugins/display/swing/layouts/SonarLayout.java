package tritechplugins.display.swing.layouts;

import java.awt.Rectangle;
import java.util.HashMap;

import tritechplugins.display.swing.LayoutInfo;

public abstract class SonarLayout {


	/**
	 * Link a sonar id to an image index.  
	 */
	protected HashMap<Integer, Integer> imageIndexes = new HashMap<>();
	
	public SonarLayout() {
	}
	
	/**
	 * Get the sonar panel index for a sonar, or -1 if this sonar does not have an display panel. 
	 * @param sonarId
	 * @return
	 */
	public int getImageIndex(int sonarId) {
		Integer ind = imageIndexes.get(sonarId);
		if (ind == null) {
			return -1;
		}
		return ind;
	}
	
	/**
	 * Work out rectangles for each sonar, which may overlap if being clever. All rectangles should have
	 * been passed through checkAspect to make sure they are the right dimension. 
	 * @param bounds outer bounding rectangle. 
	 * @param nSonar number of sonars. 
	 * @param maxAngle Maximum angle in radians. 
	 * @return Rectangles for layout. 
	 */
	public abstract LayoutInfo[] getRectangles(Rectangle bounds, int nSonar, double maxAngle);
	
	/**
	 * Get the name of the layout
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Get the ratio of width to height. 
	 * @param maxAngle max angle in radians (generally can be first in angle list, if neg, will take abs)
	 * @return width/height ratio
	 */
	public double getImageAspect(double maxAngle) {
		double aspect = 2*Math.sin(Math.abs(maxAngle)); 
		return aspect;
	}
	
	/**
	 * Get the aspect ratio of the surrouding window, which is the rotation of 
	 * the imageAspect. 
	 * @param maxSonarAngle (radians)
	 * @param rotationDeg (radians clockwise)
	 * @return
	 */
	public double getWindowAspect(double maxSonarAngle, double rotation) {
		double imageAspect = getImageAspect(maxSonarAngle); // width/height = x if y is 1. 
		double c = Math.cos(rotation);
		double s = Math.sin(rotation);
		double x = c*imageAspect + s;
		double y = -s*imageAspect + c;
		return Math.abs(x/y);
	}
	
	public Rectangle checkImageAspect(Rectangle rect, double maxAng) {
		double aspect = getImageAspect(maxAng);
		Rectangle newRect = new Rectangle(rect);
		if (rect.height*aspect < rect.width) {
			// height is smallest, so reduce width. 
			int newWidth = (int) (rect.height*aspect);
			int xGap = rect.width-newWidth;
			newRect.x += xGap/2;
			newRect.width = newWidth;
		}
		else if (rect.height*aspect > rect.width) {
			// height is largest, so reduce height. 
			int newHeight = (int) (rect.width/aspect);
			int yGap = rect.height-newHeight;
			newRect.y += yGap/2;
			newRect.height = newHeight;
		}
		return newRect;
	}

}
