package tritechplugins.record;

import java.io.Serializable;

/**
 * Information on GLF trigger data - there may be more than one. 
 * @author dg50
 *
 */
public class GLFTriggerData implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public GLFTriggerData(String triggerDataName) {
		super();
		this.triggerDataName = triggerDataName;
	}

	public String triggerDataName;

	public int preSeconds;
	
	public int postSeconds;
	
	public boolean enabled;
}
