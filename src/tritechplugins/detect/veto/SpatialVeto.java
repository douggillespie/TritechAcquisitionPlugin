package tritechplugins.detect.veto;

import java.awt.Window;

/**
 * Spatial vetos for Tritech data. Concrete classes will
 * be arbitrary shapes. May apply to one of more sonars. 
 * @author dg50
 *
 */
public abstract class SpatialVeto {

	private SpatialVetoProvider vetoProvider;

	public SpatialVeto(SpatialVetoProvider vetoProvider) {
		this.vetoProvider = vetoProvider;
	}
	
	public String getName() {
		return vetoProvider.getName();
	}
	
	/**
	 * Should this veto run on data from this sonar ? 
	 * @param sonarId
	 * @return true if this veto should run on this sonar
	 */
	public boolean doSonar(int sonarId) {
		int useSonar = getParams().sonarId;
		return (useSonar == 0 || useSonar == sonarId);
	}
	
	/**
	 * configure the veto. Will probably require opening a bespoke dialog
	 * @param owner
	 * @return true if the configuration has changed
	 */
	abstract public boolean configureVeto(Window owner);
	
	abstract public boolean isInVeto(double x, double y);
	
	abstract public SpatialVetoParams getParams();
	
	abstract public void setParams(SpatialVetoParams params);

	/**
	 * @return the vetoProvider
	 */
	public SpatialVetoProvider getVetoProvider() {
		return vetoProvider;
	}
	
	

}
