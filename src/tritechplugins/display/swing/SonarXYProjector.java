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
	private Rectangle rectangle;
	private GeminiImageRecordI imageRecord;

	public SonarXYProjector(SonarsPanel sonarsPanel, int imageIndex, int sonarID) {
		this.sonarsPanel = sonarsPanel;
		this.imageIndex = imageIndex;
		this.sonarID = sonarID;
	}

	@Override
	public Coordinate3d getCoord3d(double x, double y, double z) {
		
		return getCoord3d(new Coordinate3d(x, y, z));
	}
	
	public void setLayout(Rectangle rectangle, GeminiImageRecordI imageRecord) {
		this.rectangle = rectangle;
		this.imageRecord = imageRecord;
		
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
		if (rectangle == null || imageRecord == null) {
			return null;
		}
		double maxRange = imageRecord.getMaxRange();
		double pixScale = rectangle.height/maxRange;
		double x = rectangle.getCenterX() + dataObject.getCoordinate(0)*pixScale;
		double y = rectangle.getMaxY() - dataObject.getCoordinate(1)*pixScale;
		return new Coordinate3d(x,y);
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		// TODO Auto-generated method stub
		return null;
	}

}
