package tritechplugins.acquire.georef;

import GPS.GpsData;

public interface GeoRefObserver {

	public void updatePosition(GpsData gpsData);
}
