package tritechplugins.acquire.offline;

import java.io.File;

import dataMap.OfflineDataMapPoint;
import tritechgemini.fileio.GeminiFileCatalog;

public class ImageMapPoint extends OfflineDataMapPoint {

	private GeminiFileCatalog geminiFileCatalog;

	public GeminiFileCatalog getGeminiFileCatalog() {
		return geminiFileCatalog;
	}

	public ImageMapPoint(GeminiFileCatalog geminiFileCatalog) {
		super(geminiFileCatalog.getFirstRecordTime(), geminiFileCatalog.getLastRecordTime(), geminiFileCatalog.getNumRecords(), 0);
		this.geminiFileCatalog = geminiFileCatalog;
	}

	@Override
	public String getName() {
		return geminiFileCatalog.getFilePath();
	}

	@Override
	public String toString() {
		String str =  super.toString();
		String file = geminiFileCatalog.getFilePath();
		return str + "<br>" + file;
	}

	@Override
	public Long getLowestUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLowestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getHighestUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHighestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

}
