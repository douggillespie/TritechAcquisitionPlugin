package tritechplugins.display.swing;

import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import tritechgemini.imagedata.GeminiImageRecordI;

public class SonarXYProjector extends GeneralProjector {

	private int imageIndex;
	private int sonarID;
	private SonarsPanel sonarsPanel;
//	private Rectangle rectangle;
	private GeminiImageRecordI imageRecord;
	private boolean flipImage = false;
	private SonarZoomTransform sonarZoomTransform;

	public SonarXYProjector(SonarsPanel sonarsPanel, int imageIndex, int sonarID) {
		this.sonarsPanel = sonarsPanel;
		this.imageIndex = imageIndex;
		this.sonarID = sonarID;
	}

	@Override
	public Coordinate3d getCoord3d(double x, double y, double z) {
		
		return getCoord3d(new Coordinate3d(x, y, z));
	}
	
//	public void setLayout(Rectangle rectangle, GeminiImageRecordI imageRecord) {
//		this.rectangle = rectangle;
//		this.imageRecord = imageRecord;
//		
//	}
//
//	@Override
//	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
//		if (rectangle == null || imageRecord == null) {
//			return null;
//		}
//		double maxRange = imageRecord.getMaxRange();
//		double pixScale = rectangle.height/maxRange;
//		double xCoord = -dataObject.getCoordinate(0);
//		if (flipImage) {
//			xCoord = - xCoord;
//		}
//		double x = rectangle.getCenterX() + xCoord*pixScale;
//		double y = rectangle.getMaxY() - dataObject.getCoordinate(1)*pixScale;
//		return new Coordinate3d(x,y);
	//	}
	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
		if (sonarZoomTransform == null) {
			return null;
		}
		return sonarZoomTransform.imageMetresToScreen(-dataObject.getCoordinate(0), dataObject.getCoordinate(1));
		
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the flipImage
	 */
	public boolean isFlipImage() {
		return flipImage;
	}

	/**
	 * @param flipImage the flipImage to set
	 */
	public void setFlipImage(boolean flipImage) {
		this.flipImage = flipImage;
	}

	public void setLayout(SonarZoomTransform sonarZoomTransform) {
		this.sonarZoomTransform = sonarZoomTransform;
	}

}
