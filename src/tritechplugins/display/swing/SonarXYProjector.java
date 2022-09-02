package tritechplugins.display.swing;

import java.awt.Point;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import tritechgemini.imagedata.GeminiImageRecordI;

/**
 * Projector for sonar data. Most of the work however is done in 
 * the SonarZoomTransform
 * @author dg50
 *
 */
public class SonarXYProjector extends GeneralProjector {

	private int imageIndex;
	private int sonarID;
	private SonarsPanel sonarsPanel;
//	private Rectangle rectangle;
	private GeminiImageRecordI imageRecord;
//	private boolean flipImage = false;
	private SonarZoomTransform sonarZoomTransform;

	public static final ParameterType[] requiredParams = {ParameterType.X, ParameterType.Y};
	
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
		Coordinate3d coord = sonarZoomTransform.imageMetresToScreen(dataObject.getCoordinate(0), dataObject.getCoordinate(1));
		Point p = new Point((int) coord.getCoordinate(0), (int) coord.getCoordinate(1));
		if (sonarZoomTransform.getScreenRectangle().contains(p)) {
			return coord;
		}
		else {
			return null;
		}
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		if (sonarZoomTransform == null) {
			return null;
		}
		Coordinate3d pos = sonarZoomTransform.screenToImageMetres(screenPosition.getCoordinate(0), 
				screenPosition.getCoordinate(1));
		return pos;
	}

//	/**
//	 * @return the flipImage
//	 */
//	public boolean isFlipImage() {
//		return flipImage;
//	}
//
//	/**
//	 * @param flipImage the flipImage to set
//	 */
//	public void setFlipImage(boolean flipImage) {
//		this.flipImage = flipImage;
//	}

	public void setLayout(SonarZoomTransform sonarZoomTransform) {
		this.sonarZoomTransform = sonarZoomTransform;
	}

	@Override
	public ParameterType[] getParameterTypes() {
		return requiredParams;
	}

}
