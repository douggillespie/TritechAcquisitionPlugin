package tritechplugins.display.swing;

import java.awt.Rectangle;

public abstract class SonarLayout {

	public SonarLayout() {
	}
	
	/**
	 * Work out rectangles for each sonar, which may overlap if being clever. All rectangles should have
	 * been passed through checkAspect to make sure they are the right dimension. 
	 * @param bounds outer bounding rectangle. 
	 * @param nSonar number of sonars. 
	 * @param maxAngle MAximum angle in radians. 
	 * @return Rectangles for layout. 
	 */
	public abstract LayoutInfo[] getRectangles(Rectangle bounds, int nSonar, double maxAngle);
	
	/**
	 * Get the ratio of width to height. 
	 * @param maxAngle max angle in radians (generally can be first in angle list, if neg, will take abs)
	 * @return width/height ratio
	 */
	public double getAspect(double maxAngle) {
		double aspect = 2*Math.sin(Math.abs(maxAngle)); 
		return aspect;
	}
	
	public Rectangle checkAspect(Rectangle rect, double maxAng) {
		double aspect = getAspect(maxAng);
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
