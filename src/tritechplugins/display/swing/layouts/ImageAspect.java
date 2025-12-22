package tritechplugins.display.swing.layouts;

/**
 * information about scaling of an image and what it's aspect is when rotated. 
 * for an unrotated image, ySize is 1 and xSize is cos(maxAngle)*2
 * @author dg50
 *
 */
public class ImageAspect {
	
	private double rotDegrees;
	
	private double xSize;
	private double ySize;
	
	public ImageAspect(double rotDegrees, double xSize, double ySize) {
		this.rotDegrees = rotDegrees;
		this.xSize = xSize;
		this.ySize = ySize;
	}
	
	/**
	 * Ratio of x dimension to y dimension.
	 * @return
	 */
	public double getAspectRatio() {
		if (ySize == 0) {
			return 1;
		}
		else {
			return xSize/ySize;
		}
	}

}
