package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import PamView.ColourArray;
import tritechgemini.imagedata.FanImageData;

/**
 * Make a buffered image from imagefandata. 
 * @author dg50
 *
 */
public class FanDataImage {

	private BufferedImage bufferedImage;

	private FanImageData fanData;

	private ColourArray colours;

	private boolean transparent;
	
	private int gain;

	// pixel ranges in the image to be used in the output image. 
	private int xPix0;

	private int xPix1;

	private int yPix0;

	private int yPix1;

	/**
	 * Create a buffered image equal in size to the fanData
	 * @param fanData
	 * @param colours
	 * @param transparent
	 * @param gain
	 */
	public FanDataImage(FanImageData fanData, ColourArray colours, boolean transparent, int gain) {
		this.fanData = fanData;
		this.colours = colours;
		this.transparent = transparent;
		this.gain = Math.max(1, gain);
		short[][] data = fanData.getImageValues();
		if (data != null) {
			int nX = data.length;
			int nY = data[0].length;
			xPix0 = 0;
			xPix1 = nX;
			yPix0 = 0;
			yPix1 = nY;
		}
	}

	/**
	 * Create a buffered image from just part of the fan image. 
	 * @param fanData
	 * @param colours
	 * @param transparent
	 * @param gain
	 * @param xMin minimum x in metres
	 * @param xMax maximum x in metres
	 * @param yMin minimum y in metres
	 * @param yMax maximum y in metres
	 */
	public FanDataImage(FanImageData fanData, ColourArray colours, boolean transparent, int gain, double xMin, double xMax, double yMin, double yMax) {
		this.fanData = fanData;
		this.colours = colours;
		this.transparent = transparent;
		this.gain = Math.max(1, gain);
		short[][] data = fanData.getImageValues();
		if (data != null) {
			int nX = data.length;
			int nY = data[0].length;
			xPix0 = 0;
			xPix1 = nX;
			yPix0 = 0;
			yPix1 = nY;
			/*
			 *  now modify those with the x and y limits passed in. Note that the passed
			 *  value for x is in metres relative to the centre of the image, y is from the
			 *  bottom. 
			 */
			double x1 = Math.min(xMin, xMax);
			double x2 = Math.max(xMin, xMax);
			xPix0 = (int) (nX/2+x1/fanData.getMetresPerPixX());
			xPix1 = (int) (nX/2+x2/fanData.getMetresPerPixX());
			double y1 = Math.min(yMin, yMax);
			double y2 = Math.max(yMin, yMax);
			yPix0 = (int) (y1/fanData.getMetresPerPixY());
			yPix1 = (int) (y2/fanData.getMetresPerPixY());
			xPix0 = Math.max(xPix0, 0);
			xPix1 = Math.min(xPix1,  nX);
			yPix0 = Math.max(yPix0, 0);
			yPix1 = Math.min(yPix1,  nY);
		}
	}
	
	public BufferedImage getBufferedImage() {
		if (bufferedImage == null) {
			bufferedImage = makeImage();
		}
		return bufferedImage;
	}

	public FanImageData getFanData() {
		return fanData;
	}

	private BufferedImage makeImage() {
		if (fanData == null) {
			return null;
		}
		short[][] data = fanData.getImageValues();
		if (data == null) {
			return null;
		}
		int nX = data.length;
		int nY = data[0].length;
		if (xPix1 <= xPix0 || yPix1 <= yPix0) {
			return null;
		}
		BufferedImage image = new BufferedImage(xPix1-xPix0, yPix1-yPix0, BufferedImage.TYPE_4BYTE_ABGR);
		WritableRaster raster = image.getRaster();
		int[] transparent = {0,0,0,0};
		int[] coloured = {0,0,0,255};

		Color[] colourValues = colours.getColours();
		for (int ix = xPix0, ixout = 0; ix < xPix1; ix++, ixout++) {
			for (int iy = yPix0, iyout = 0; iy < yPix1; iy++, iyout++) {
				short val = data[ix][iy];
				if (val < 0) {
					raster.setPixel(ixout, iyout, transparent);
				}
				else {
					val = (short) Math.min(255, val*gain);
					val &= 0xFF;
					Color col = colourValues[val];
					coloured[0] = col.getRed();
					coloured[1] = col.getGreen();
					coloured[2] = col.getBlue();//sqrt255(val);
					//					coloured[3] = val;
					raster.setPixel(ixout, iyout, coloured);
				}
			}
		}
		return image;
	}
	/**
	 * Get the sqrt of 255 on a scale of 1:255. 
	 * @param val
	 * @return
	 */
	private int sqrt255(int val) {
		return (int) Math.sqrt(val*255);
	}
}

