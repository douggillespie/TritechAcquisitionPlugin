package tritechplugins.detect.veto;

import PamView.PanelOverlayDraw;

public interface SpatialVetoProvider {

//	public SpatialVetoProvider() {
//	}

	/**
	 * Name for this veto. 
	 * @return name for this type of veto. 
	 */
	abstract public String getName();

	/**
	 * Create a veto for a specified sonar. Use id = 0 for all sonars. 
	 * @param userName Name for the veto user, will identify it for settings storage, etc.  
	 * @param sonarId sonar id to apply the veto to. Either the id of one sonar, or 0 for all sonars. 
	 * @return a spatial veto 
	 */
	abstract public SpatialVeto createVeto();
	
	/**
	 * Get an overlay draw for this type of veto for use on PAMGuard displays. 
	 * @return overlay draw
	 */
	abstract public PanelOverlayDraw getVetoOverlayDraw();

}
