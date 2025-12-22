package tritechplugins.acquire.georef;

import java.io.Serializable;

import PamUtils.LatLong;

public class GeoRefParams implements Serializable, Cloneable {
	
	public static final long serialVersionUID = 1L;

	private GeoRefType refType = GeoRefType.FIXED;
	
	@Override
	protected GeoRefParams clone() {
		try {
			return (GeoRefParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private LatLong fixedPosition;

	/**
	 * @return the refType
	 */
	public GeoRefType getRefType() {
		return refType;
	}

	/**
	 * @param refType the refType to set
	 */
	public void setRefType(GeoRefType refType) {
		this.refType = refType;
	}

	/**
	 * @return the fixedPosition
	 */
	public LatLong getFixedPosition() {
		return fixedPosition;
	}

	/**
	 * @param fixedPosition the fixedPosition to set
	 */
	public void setFixedPosition(LatLong fixedPosition) {
		this.fixedPosition = fixedPosition;
	}


}
