package tritechplugins.acquire.georef;

import java.util.ArrayList;

import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamController.SettingsNameProvider;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

/**
 * Support georeference for sonar data, including subscribing to 
 * GPS data and notifying updates. 
 * @author dg50
 *
 */
public class GeoReference implements PamObserver {
	
	private ArrayList<GeoRefObserver> observers;
	private GeoRefParams geoRefParams;
	
	private GPSDataBlock gpsDataBlock;
	private SettingsNameProvider settingsName;

	public GeoReference(SettingsNameProvider settingsName) {
		this.settingsName = settingsName;
		observers = new ArrayList<>();
	}
	
	public boolean prepare(GeoRefParams geoRefParams) {
		this.geoRefParams = geoRefParams;
		if (geoRefParams.getRefType() == GeoRefType.FIXED) {
			if (gpsDataBlock != null) {
				// unsubscribe if it was previously using gps data. 
				gpsDataBlock.deleteObserver(this);
				gpsDataBlock = null;
			}
		}
		else {
			gpsDataBlock = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
			if (gpsDataBlock != null) {
				gpsDataBlock.addObserver(this);
			}
		}
		
		return true;
	}
	
	/**
	 * Get position for given time (time not used for fixed position)
	 * @param timeMillis
	 * @return
	 */
	public GpsData getPosition(long timeMillis) {
		if (geoRefParams == null) {
			return null;
		}
		if (geoRefParams.getRefType() == GeoRefType.FIXED) {
			LatLong pos = geoRefParams.getFixedPosition();
			if (pos == null) {
				return null;
			}
			GpsData g = new GpsData(timeMillis, pos);
			return g;
		}
		else { // using GPS data.  
			if (gpsDataBlock == null) {
				return null;
			}
			GpsDataUnit gpsDataUnit = gpsDataBlock.findDataUnit(timeMillis, 0);
			if (gpsDataUnit != null) {
				return gpsDataUnit.getGpsData();
			}
		}
		return null;
	}
	
	/**
	 * Add an observer - will get updates if using GPS data. 
	 * @param observer
	 */
	public void addObserver(GeoRefObserver observer) {
		if (observers.contains(observer) == false) {
			observers.add(observer);
		}
	}
	
	/**
	 * Remove an observer
	 * @param observer
	 * @return
	 */
	public boolean removeObsever(GeoRefObserver observer) {
		return observers.remove(observer);
	}

	/**
	 * Notify observers when there is an updates GPS position. 
	 * @param gpsData
	 */
	private void notifyObservers(GpsData gpsData) {
		if (observers == null) {
			return;
		}
		for (GeoRefObserver obs : observers) {
			obs.updatePosition(gpsData);
		}
	}

	@Override
	public long getRequiredDataHistory(PamObservable observable, Object arg) {
		return 0;
	}

	@Override
	public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
		GpsDataUnit gpsDataUnit = (GpsDataUnit) pamDataUnit;
		GpsData gpsData = gpsDataUnit.getGpsData();
		if (gpsData != null && gpsData.isDataOk()) {
			notifyObservers(gpsData);
		}
	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}

	@Override
	public void removeObservable(PamObservable observable) {
		
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		
	}

	@Override
	public void noteNewSettings() {
		
	}

	@Override
	public String getObserverName() {
		return "GPS Observer: " + settingsName.getUnitName();
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		
	}

}
