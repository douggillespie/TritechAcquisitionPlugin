package tritechplugins.acquire.georef;

public enum GeoRefType {

	FIXED, GPS;

	@Override
	public String toString() {
		switch (this) {
		case FIXED:
			return "Fixed Position";
		case GPS:
			return "GPS Position";
		default:
			return "None";
		}
	}


}
