package tritechplugins.display.swing;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;

public class CombinedXYProjector extends GeneralProjector {

	private SonarsPanel sonarsPanel;
	
	public CombinedXYProjector(SonarsPanel sonarsPanel) {
		super();
		this.sonarsPanel = sonarsPanel;
	}

	@Override
	public Coordinate3d getCoord3d(double d1, double d2, double d3) {
		SonarXYProjector[] projectors = sonarsPanel.getXyProjectors();
		if (projectors == null) {
			return null;
		}
		for (int i = 0; i < projectors.length; i++) {
			SonarXYProjector xyProj = projectors[i];
			if (xyProj == null) {
				continue;
			}
			Coordinate3d coord = xyProj.getCoord3d(d1, d2, d3);
			if (coord != null) {
				return coord;
			}
		}
		return null;
	}

	@Override
	public Coordinate3d getCoord3d(PamCoordinate dataObject) {
		SonarXYProjector[] projectors = sonarsPanel.getXyProjectors();
		if (projectors == null) {
			return null;
		}
		for (int i = 0; i < projectors.length; i++) {
			SonarXYProjector xyProj = projectors[i];
			if (xyProj == null) {
				continue;
			}
			Coordinate3d coord = xyProj.getCoord3d(dataObject);
			if (coord != null) {
				return coord;
			}
		}
		return null;
	}

	@Override
	public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
		SonarXYProjector[] projectors = sonarsPanel.getXyProjectors();
		if (projectors == null) {
			return null;
		}
		for (int i = 0; i < projectors.length; i++) {
			SonarXYProjector xyProj = projectors[i];
			if (xyProj == null) {
				continue;
			}
			PamCoordinate coord = xyProj.getDataPosition(screenPosition);
			if (coord != null) {
				return coord;
			}
		}
		return null;
	}
	

}
