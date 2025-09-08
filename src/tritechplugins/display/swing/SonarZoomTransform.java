package tritechplugins.display.swing;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import PamUtils.Coordinate3d;
import tritechgemini.imagedata.GeminiImageRecordI;

/**
 * Zoom transforms which allow moving between three coordinate systems. <br>
 * 1. Screen coordinates <br>
 * 2. sonar coordinates in metres <br>
 * 3. Sonar coordinates in pixels. <br>
 * Screen coordinates are from the top. Sonar and fan image y coordinates from the bottom. 
 * @author dg50
 *
 */
public class SonarZoomTransform {

	/**
	 * Gemini image. 
	 */

	//	private GeminiImageRecordI imageRecord;
	private double maximumRange;

	/**
	 * Rectangle projected into on screen. 
	 */
	private Rectangle screenRectangle;

	//	/**
	//	 * Fan image, can be varying sizes depending on selected display resolution
	//	 */
	//	private BufferedImage fanImage;
	private Rectangle fanImageRectangle;

	/**
	 * Zoom factor, >= 1
	 */
	private double zoomFactor;

	/**
	 * The rectangle of the imageClip which will get displayed in the screenRectangle.
	 */
	private Rectangle imageClipRect;

	/**
	 * Restrict dimension to be within the screen rectangle. 
	 */
	private boolean restrict = true;

	private boolean isFlip = false;


	/*
	 * Zoom centre in metres from sonar centre. 
	 */
	private Coordinate3d zoomCentre;

	private AffineTransform rTransform;

	private AffineTransform invTransform;

	/**
	 * @param imageRecord the imageRecord to set
	 */
	public void setImageRecord(GeminiImageRecordI imageRecord) {
		//		this.imageRecord = imageRecord;
		this.maximumRange = imageRecord.getMaxRange();
		calculateTransforms();
	}

	/**
	 * @param screenRectangle the screenRectangle to set
	 */
	public void setScreenRectangle(Rectangle screenRectangle) {
		this.screenRectangle = screenRectangle;
		calculateTransforms();
	}

	/**
	 * @param fanImage the fanImage to set
	 */
	public void setFanImage(BufferedImage fanImage) {
		//		this.fanImage = fanImage;
		this.fanImageRectangle = new Rectangle(0,0,fanImage.getWidth(),fanImage.getHeight());
		calculateTransforms();
	}

	/**
	 * @param zoomFactor the zoomFactor to set
	 */
	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
		calculateTransforms();
	}

	/**
	 * @param zoomCentre the zoomCentre to set
	 */
	public void setZoomCentre(Coordinate3d zoomCentre) {
		this.zoomCentre = zoomCentre;
		calculateTransforms();
	}

	/**
	 * The rectangle within the fanimage in pixels which corresponds to the current zoom. 
	 * With no zoom this should just be the dimension of the fan image.  
	 * @return the imageClipRect
	 */
	public Rectangle getImageClipRect() {
		return imageClipRect;
	}

	/**
	 * 
	 * @param imageRecord
	 * @param screenRectangle
	 * @param fanImage
	 * @param zoomFactor
	 * @param zoomCentre
	 */
	public SonarZoomTransform(GeminiImageRecordI imageRecord, Rectangle screenRectangle, BufferedImage fanImage, 
			double zoomFactor, Coordinate3d zoomCentre, boolean isFlip) {
		this(imageRecord.getMaxRange(), screenRectangle, new Rectangle(0,0,fanImage.getWidth(), fanImage.getHeight()), 
				zoomFactor, zoomCentre, isFlip);
	}
	/**
	 * 
	 * @param imageRecord
	 * @param screenRectangle
	 * @param fanImage
	 * @param zoomFactor
	 * @param zoomCentre
	 */
	public SonarZoomTransform(double maximumRange, Rectangle screenRectangle, Rectangle fanImageRectangle, 
			double zoomFactor, Coordinate3d zoomCentre, boolean isFlip) {
		this.maximumRange = maximumRange;
		this.screenRectangle = screenRectangle;
		this.fanImageRectangle = fanImageRectangle;
		this.zoomFactor = zoomFactor;
		this.zoomCentre = zoomCentre;
		this.isFlip = isFlip;
		calculateTransforms();
	}



	//	/**
	//	 * @return the imageRecord
	//	 */
	//	public GeminiImageRecordI getImageRecord() {
	//		return imageRecord;
	//	}

	/**
	 * @return the screenRectangle
	 */
	public Rectangle getScreenRectangle() {
		return screenRectangle;
	}

	//	/**
	//	 * @return the fanImage
	//	 */
	//	public BufferedImage getFanImage() {
	//		return fanImage;
	//	}

	/**
	 * @return the zoomFactor
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}

	/**
	 * @return the zoomCentre
	 */
	public Coordinate3d getZoomCentre() {
		return zoomCentre;
	}


	/**
	 * Get the maximum range used in the calcs in metres
	 * @return
	 */
	public double getMaximumRange() {
		return maximumRange;
	}

	/**
	 * Calculate everything we're likely to want to know. 
	 */
	private void calculateTransforms() {
		/*
		 * scale of original fan image in pixels per metre.
		 */
		double imageScale = fanImageRectangle.getHeight()/maximumRange;
		int ws = fanImageRectangle.width;
		int wd = screenRectangle.width;
		int hs = fanImageRectangle.height;
		int hd = screenRectangle.height;


		/**
		 * initial zooming only uses hs and ws, the sonar image sizes. 
		 * this can then be projected onto the larger image. 
		 */
		double x1 = ws/2*(1.-1./zoomFactor) + zoomCentre.x*imageScale;
		double x2 = ws/zoomFactor+x1;
		double y1 = zoomCentre.y*imageScale;
		double y2 = y1+hs/zoomFactor;


		//		System.out.printf("Z %3.1f, ws %d, hs %d, x1 %3.1f, x2 %3.1f, y1 %3.1f, y2 %3.1f\n", 
		//				zoomFactor, ws, hs, x1,x2,y1,y2);
		if (x1 < 0) {
			x2 -= x1;
			x1 = 0;
		}
		if (x2 > ws) {
			double wid = x2-x1;
			x2 = ws-1;
			x1 = x2-wid;		
		}
		if (y1 < 0) {
			y2 -= y1;
			y1 = 0;
		}
		if (y2 > hs) {
			double h = y2-y1;
			y2 = hs-1;
			y1 = y2-h;
		}

		/**
		 * This is now the bit of the fan image that get's projected into the screen rectangle. 
		 */
		imageClipRect = new Rectangle((int) x1, (int) y1, (int) (x2-x1), (int) (y2-y1));
	}

	/**
	 * Convert a screen coordinate to a position on the image. 
	 * @param screenX x coordinate on screen
	 * @param screenY y coordinate on screen
	 * @return coordinate in image
	 */
	public Coordinate3d screenToImageMetres(double screenX, double screenY) {
		double[] xy = invTranslate(screenX, screenY);
		screenX = xy[0];
		screenY = xy[1];
		if (isFlip) {
			screenX = screenRectangle.getX()*2+screenRectangle.getWidth() - screenX;
		}
		Coordinate3d imagePixs = screenToImagePixels(screenX, screenY);
		if (imagePixs == null) {
			return null;
		}
		return imagePixToImageMetres(imagePixs.x, imagePixs.y);
	}

	/**
	 * convert a sonar coordinate value in metres to a screen coordinate
	 * @param imageMetresX
	 * @param imageMetresY
	 * @return
	 */
	public Coordinate3d imageMetresToScreen(double imageMetresX, double imageMetresY) {
		Coordinate3d imagePix = metresToImagePix(imageMetresX, imageMetresY);
		Coordinate3d screen = imagePixelsToScreen(imagePix.x, imagePix.y);
		if (isFlip) {
			screen.x = screenRectangle.getX()*2+screenRectangle.getWidth() - screen.x;
		}
		double[] tXY = translate(screen.x, screen.y);
		screen.x = tXY[0];
		screen.y = tXY[1];
		return screen;
	}

	/**
	 * Transform pixels within the fan image to x,y in metres
	 * @param imagePixX
	 * @param imagePixY
	 * @return position in metres.
	 */
	public Coordinate3d imagePixToImageMetres(double imagePixX, double imagePixY) {
		double metresPerPix = maximumRange / fanImageRectangle.getHeight();
		double x = (imagePixX - fanImageRectangle.getWidth()/2.)*metresPerPix;
		double y = (imagePixY) * metresPerPix;
		//		System.out.printf("Image metres x %3.1f, y %3.1f\n", x, y);
		return new Coordinate3d(x, y);
	}

	/**
	 * Transform metres to a pixel value in the full image.
	 * @param imageMetresX metres in fan image
	 * @param imageMetresY metres in fan image 
	 * @return position in metres.
	 */
	public Coordinate3d metresToImagePix(double imageMetresX, double imageMetresY) {
		double metresPerPix = maximumRange / fanImageRectangle.getHeight();
		double x = fanImageRectangle.getWidth()/2. + imageMetresX / metresPerPix;
		double y = imageMetresY / metresPerPix;
		return new Coordinate3d(x, y);
	}



	/**
	 * Convert from a screen coordinate to a coordinate within the fan image 
	 * in pixels. This transform just needs the two rectangles. 
	 * @param screenx
	 * @param screenY
	 * @return
	 */
	public Coordinate3d screenToImagePixels(double screenX, double screenY) {
		// should just be a linear transformaton for both x and y. 
		// move the screen pixels relative to the screen rectangle
		screenY = screenRectangle.y + screenRectangle.getHeight() - screenY;
		screenX -= screenRectangle.x;
		if (restrict) {
			if (screenY < 0 || screenY >= screenRectangle.height) {
				return null;
			}
			if (screenX < 0 || screenX >= screenRectangle.width) {
				return null;
			}
		}
		double scale = imageClipRect.getWidth() / screenRectangle.getWidth();
		double offsX = imageClipRect.x;// - scale*screenRectangle.x;
		double offsY = imageClipRect.y;// - scale*screenRectangle.y;
		double x = offsX + scale * screenX;
		double y = offsY + scale * screenY;
		//		System.out.printf("Image pix x %3.1f, y %3.1f\n", x, y);
		return new Coordinate3d(x, y);		
	}

	/**
	 * Convert from a screen coordinate to a coordinate within the fan image 
	 * in pixels. This transform just needs the two rectangles. 
	 * @param imageX
	 * @param imageY
	 * @return position on screen
	 */
	public Coordinate3d imagePixelsToScreen(double imageX, double imageY) {
		// opposite of  screenToImagePixels
		double scale = imageClipRect.getWidth() / screenRectangle.getWidth();
		double offsX = imageClipRect.x;// - scale*screenRectangle.x;
		double offsY = imageClipRect.y;// - scale*screenRectangle.y;
		double x = (imageX-offsX)/scale;
		double y = (imageY-offsY)/scale;

		y = screenRectangle.y + screenRectangle.getHeight() - y;
		x += screenRectangle.x;
		return new Coordinate3d(x, y);		
	}

	/**
	 * @return the isFlip
	 */
	public boolean isFlip() {
		return isFlip;
	}

	/**
	 * @param isFlip the isFlip to set
	 */
	public void setFlip(boolean isFlip) {
		this.isFlip = isFlip;
	}

	private double[] translate(double x, double y) {
		double[] dst = {x,y}; 
		if (rTransform != null) {
			double[] src = {x,y};
			rTransform.transform(src, 0, dst, 0, 1);
		}
		return dst;
	}

	private double[] invTranslate(double x, double y) {
		double[] dst = {x,y}; 
		if (invTransform != null) {
			double[] src = {x,y};
			invTransform.transform(src, 0, dst, 0, 1);
		}
		return dst;
	}

	/**
	 * @return the rTransform
	 */
	public AffineTransform getrTransform() {
		return rTransform;
	}

	/**
	 * @param rTransform the rTransform to set
	 */
	public void setrTransform(AffineTransform rTransform) {
		this.rTransform = rTransform;
		if (rTransform != null) {
			try {
				invTransform = rTransform.createInverse();
			} catch (NoninvertibleTransformException e) {
				invTransform = null;
			}
		}
		else {
			invTransform = null;
		}
	}
}
