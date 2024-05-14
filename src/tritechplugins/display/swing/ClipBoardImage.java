package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClipBoardImage implements Transferable {

	private Image i;
	
	public ClipBoardImage(Image image) {
		this.i = image;
	}
	
	/*
	 * Flip an image vertically
	 */
	public static BufferedImage flipImageV(BufferedImage image) {
		if (image == null) {
			return null;
		}
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = newImage.getGraphics();
		g.drawImage(image, 0, h, w, 0, 0, 0, w, h, null);
		return newImage;
	}

	public Object getTransferData( DataFlavor flavor )
			throws UnsupportedFlavorException, IOException {
		if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
			return i;
		}
		else {
			throw new UnsupportedFlavorException( flavor );
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[ 1 ];
		flavors[ 0 ] = DataFlavor.imageFlavor;
		return flavors;
	}

	public boolean isDataFlavorSupported( DataFlavor flavor ) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for ( int i = 0; i < flavors.length; i++ ) {
			if ( flavor.equals( flavors[ i ] ) ) {
				return true;
			}
		}

		return false;
	}

}
