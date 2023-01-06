package tritechplugins.acquire;

import java.io.Serializable;

import geminisdk.structures.ChirpMode;

public class SonarDaqParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public SonarDaqParams() {
	}
	

	private int range = 60;
	
	private int gain = 50;
	
	private int chirpMode = ChirpMode.CHIRP_DISABLED;
	
	

}
