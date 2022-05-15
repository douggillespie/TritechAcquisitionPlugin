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

	public FanDataImage(FanImageData fanData, ColourArray colours, boolean transparent, int gain) {
		this.fanData = fanData;
		this.colours = colours;
		this.transparent = transparent;
		this.gain = Math.max(1, gain);
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

		Color[] colourValues = colours.getColours();
		for (int ix = 0; ix < nX; ix++) {
			for (int iy = 0; iy < nY; iy++) {
				short val = data[ix][iy];
				if (val < 0) {
					raster.setPixel(ix, iy, transparent);
				}
				else {
					val = (short) Math.min(255, val*gain);
					val &= 0xFF;
					Color col = colourValues[val];
					coloured[0] = col.getRed();
					coloured[1] = col.getGreen();
					coloured[2] = col.getBlue();//sqrt255(val);
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

