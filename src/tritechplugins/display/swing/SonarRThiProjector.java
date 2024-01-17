package tritechplugins.display.swing;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;

public class SonarRThiProjector extends SonarXYProjector {

	public static final ParameterType[] requiredParams = {ParameterType.BEARING, ParameterType.RANGE};
	
	public SonarRThiProjector(SonarsPanel sonarsPanel, int imageIndex, int sonarID) {
		super(sonarsPanel, imageIndex, sonarID);
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject, boolean clipOuter) {
		double x = -dataObject.getCoordinate(0)*Math.sin(dataObject.getCoordinate(1));
		double y = dataObject.getCoordinate(0)*Math.cos(dataObject.getCoordinate(1));
		Coordinate3d xyCoord = new Coordinate3d(x,y);
		return super.getCoord3d(xyCoord, clipOuter);
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		PamCoordinate pos = super.getDataPosition(screenPosition);
		if (pos == null) {
			return null;
		}
		double x = -pos.getCoordinate(0);
		double y = pos.getCoordinate(1);
		double r = Math.sqrt(x*x+y*y);
		double thet = Math.atan2(x, y);
		return new Coordinate3d(r, thet);
	}

	@Override
	public ParameterType[] getParameterTypes() {
		return requiredParams;
	}

}
