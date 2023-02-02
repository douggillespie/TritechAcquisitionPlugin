package tritechplugins.detect.veto;

import java.io.Serializable;

abstract public class SpatialVetoParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	private String providerClassName;
	
	protected int sonarId;

	public SpatialVetoParams(SpatialVetoProvider provider) {
		providerClassName = provider.getClass().getName();
	}

	/**
	 * @return the providerName
	 */
	public String getProviderClassName() {
		return providerClassName;
	}

	/**
	 * Specific sonar id, or 0 for all sonars. 
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}

	/**
	 * Specific sonar id, or 0 for all sonars. 
	 * @param sonarId the sonarId to set
	 */
	public void setSonarId(int sonarId) {
		this.sonarId = sonarId;
	}

}
