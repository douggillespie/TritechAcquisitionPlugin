package tritechplugins.display.swing;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import tritechplugins.display.swing.overlays.SonarOverlayManager;

/**
 * Project range and bearing
 * coordinate[0] is range, [1] bearing in radians, anticlockwise I think.
 * @author dg50
 *
 */
public class SonarRThiProjector extends SonarXYProjector {

	public static final ParameterType[] requiredParams = SonarOverlayManager.paramTypes;
	
	private SonarImagePanel sonarImagePanel;
		
	public SonarRThiProjector(SonarImagePanel sonarImagePanel) {
		super(sonarImagePanel);
		this.sonarImagePanel = sonarImagePanel;
		for (int i = 0; i < requiredParams.length; i++) {
			setParmeterType(i, requiredParams[i]);
		}
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

	/**
	 * @return the sonarImagePanel
	 */
	public SonarImagePanel getSonarImagePanel() {
		return sonarImagePanel;
	}


}
