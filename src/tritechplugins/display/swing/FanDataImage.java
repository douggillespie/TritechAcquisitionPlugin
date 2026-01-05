package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
	
	static int nFail = 0;

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
	
	/**
	 * pull individual bytes for the colours out into a simple
	 * 2d byte array to more quickly copy over to image raster
	 * @param colourArray
	 * @return raw byte data for colours
	 */
	private byte[][] extractColours(ColourArray colourArray) {
		Color[] cols = colourArray.getColours();
		byte[][] byteData = new byte[cols.length][4];
		// note reverse byte order.
		for (int i = 0; i < cols.length; i++) {
			Color col = cols[i];
			byte[] bRow = byteData[i];
			bRow[0] = (byte) col.getAlpha(); 
			bRow[1] = (byte) col.getBlue(); 
			bRow[2] = (byte) col.getGreen();
			bRow[3] = (byte) col.getRed();
		}
		return byteData;
	}

	private BufferedImage makeImage() {
		/**
		 * Without multithreading, this is currently taking about 
		 * 6 times longer than the conversion from rectangular to 
		 * fan data, which is not good! This has now been reduced to 
		 * about the same time, i.e. abut 50ms for a high res image. 
		 */
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

		int[] transparent = {0,0,0,0};
		Color transparentCol = new Color(0,0,0,0);
		
		BufferedImage image = new BufferedImage(xPix1-xPix0, (yPix1-yPix0), BufferedImage.TYPE_4BYTE_ABGR);
//		Graphics g = image.getGraphics();
//		g.setColor(transparentCol);
//		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		WritableRaster raster = image.getRaster();
		/*
		 *  some interesting ideas here that might be able to speed up (i.e. bypass)
		 *  a lot of the raster.setPixel slowness. 
		 *  https://stackoverflow.com/questions/6319465/fast-loading-and-drawing-of-rgb-data-in-bufferedimage
		 */
		
		
//		raster.
		/*
		 * Most of the time is spent in raster.setPixel. If I comment that line, 
		 * then loops take around 25ms. With that line, loops take 120ms, i.e. 4 - 5
		 * times longer. So is there a better call to raster.setPixel ? 
		 */
		boolean fail = true;
		try {
		/* this comes back 4 times the size of our data, so all we have to do is copy
		 * the RGB values across. Note these are in reverse order. Could probably multithread this
		 */
			byte[][] colourBytes = extractColours(colours);
			byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			int iPut = 0;
//			int[] coloured = {0,0,0,255};
//			Color[] colourValues = colours.getColours();
			for (int iy = yPix0; iy < yPix1; iy++) {
				for (int ix = xPix0; ix < xPix1; ix++) {
					short val = data[ix][iy];
					if (val < 0) {
//						raster.setPixel(ixout, iyout, transparent);
						iPut+=4;
					}
					else {
						val = (short) Math.min(255, val*gain);
						val &= 0xFF;
//						byte[] bRow = colourBytes[val];
						System.arraycopy(colourBytes[val], 0, imgData, iPut, 4);
						iPut+=4;
//						Color col = colourValues[val];
//						imgData[iPut++] = (byte) 0xFF;//col.getRed(); 
//						imgData[iPut++] = (byte) col.getBlue(); 
//						imgData[iPut++] = (byte) col.getGreen();
//						imgData[iPut++] = (byte) col.getRed();
			
					}
				}
			}
			
			
			fail = false;
		}
		catch (Exception e) {
			if (nFail++ < 5) {
				System.out.println("Error in fast image transform: " + e.getMessage());
			}
		}
			// do it the old way
		if (fail) {

			int[] coloured = {0,0,0,255};
			Color[] colourValues = colours.getColours();
			for (int ix = xPix0, ixout = 0; ix < xPix1; ix++, ixout++) {
				for (int iy = yPix0, iyout = 0; iy < yPix1; iy++, iyout++) {
					short val = data[ix][iy];
					if (val < 0) {
//						raster.setPixel(ixout, iyout, transparent);
					}
					else {
						val = (short) Math.min(255, val*gain);
						val &= 0xFF;
						Color col = colourValues[val];
//						g.
						
						coloured[0] = col.getRed();
						coloured[1] = col.getGreen();
						coloured[2] = col.getBlue();//sqrt255(val);
//											coloured[3] = val;
//						raster.set
						raster.setPixel(ixout, iyout, coloured);
					}
				}
			}
		}
//		else {
//		// multithreads slows it down. Presumably setRaster is synchronized ? 
//		int nThread = 1;
//		Thread[] threads = new Thread[nThread];
//		for (int t = 0; t < nThread; t++) {
//			int iThread = t;
//			threads[t] = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					int[] coloured = {0,0,0,255};
//					Color[] colourValues = colours.getColours();
//					for (int ix = xPix0+iThread, ixout = iThread; ix < xPix1; ix+=nThread, ixout+=nThread) {
//						for (int iy = yPix0, iyout = 0; iy < yPix1; iy++, iyout++) {
//							short val = data[ix][iy];
//							if (val < 0) {
////								raster.setPixel(ixout, iyout, transparent);
//							}
//							else {
//								val = (short) Math.min(255, val*gain);
//								val &= 0xFF;
//								Color col = colourValues[val];
////								g.
//								
//								coloured[0] = col.getRed();
//								coloured[1] = col.getGreen();
//								coloured[2] = col.getBlue();//sqrt255(val);
////													coloured[3] = val;
//								raster.setPixel(ixout, iyout, coloured);
//							}
//						}
//					}
//					
//				};
//			});
//			threads[t].start();
//		}
//		for (int t = 0; t < nThread; t++) {
//			try {
//				threads[t].join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		}
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

