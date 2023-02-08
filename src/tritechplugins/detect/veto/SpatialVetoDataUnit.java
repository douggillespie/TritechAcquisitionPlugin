package tritechplugins.detect.veto;

import PamguardMVC.PamDataUnit;

/**
 * Make a data unit for every spatial veto. This is going to be 
 * the easiest way of managing overlays of their outlines on the display.
 * @author dg50
 *
 */
public class SpatialVetoDataUnit extends PamDataUnit {

	private SpatialVeto spatialVeto;
	
	public SpatialVetoDataUnit(long timeMilliseconds, SpatialVeto spatialVeto) {
		super(timeMilliseconds);
		this.spatialVeto = spatialVeto;
	}

	/**
	 * @return the spatialVeto
	 */
	public SpatialVeto getSpatialVeto() {
		return spatialVeto;
	}

	@Override
	public String getSummaryString() {
		return "Spatial Veto";
	}

}
