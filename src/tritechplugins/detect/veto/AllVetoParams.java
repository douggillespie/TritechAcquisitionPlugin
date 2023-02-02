package tritechplugins.detect.veto;

import java.io.Serializable;
import java.util.ArrayList;

public class AllVetoParams implements Serializable {

	public static final long serialVersionUID = 1L;
	
	protected ArrayList<SpatialVetoParams> allParams;

	public AllVetoParams() {
		allParams = new ArrayList<>();
	}

}
