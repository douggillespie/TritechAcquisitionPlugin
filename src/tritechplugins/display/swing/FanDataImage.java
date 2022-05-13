package tritechplugins.display.swing;

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

	public FanDataImage(FanImageData fanData, ColourArray colours, boolean transparent) {
		this.fanData = fanData;
		this.colours = colours;
		this.transparent = transparent;
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
		BufferedImage image = new BufferedImage(nX, nY, BufferedImage.TYPE_4BYTE_ABGR);
		WritableRaster raster = image.getRaster();
		//		Color transParent = new Color(0,0,0,0);
		//		Color pixCol = new Color(0,0,0,0);
		int[] transparent = {0,0,0,0};
		int[] coloured = {0,0,0,255};
		for (int ix = 0; ix < nX; ix++) {
			for (int iy = 0; iy < nY; iy++) {
				short val = data[ix][iy];
				if (val < 0) {
					raster.setPixel(ix, iy, transparent);
				}
				else {
					val*=10;
					val &= 0xFF;
					coloured[0] = val;
					coloured[1] = val*0;
					coloured[2] = val;//sqrt255(val);
					//					coloured[3] = val;
					raster.setPixel(ix, iy, coloured);
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

