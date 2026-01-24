package tritechplugins.echogram;

import java.io.Serializable;

public class EchogramSettings implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	public int nBands = 1;

	@Override
	protected EchogramSettings clone() {
		try {
			return (EchogramSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	

}
