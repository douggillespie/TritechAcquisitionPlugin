package tritechplugins.detect.threshold.dataselect;

import java.io.Serializable;

import PamguardMVC.dataSelector.DataSelectParams;

public class RegionDataSelectorParams extends DataSelectParams implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	
	public double minSize = 0.;
	
	public double maxSize = 100.;
	
	public double minOccupancy = 0.;

	public RegionDataSelectorParams() {
		// TODO Auto-generated constructor stub
	}

}
