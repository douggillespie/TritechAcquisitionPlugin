package tritechplugins.acquire;

import java.util.ArrayList;
import java.util.ListIterator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import pamScrollSystem.ViewLoadObserver;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.offline.TritechOffline;

/**
 * Datablock for image data. This has slightly odd / bespoke behaviour since
 * the viewer data are already managed by the MultiFileCatalog held by the Daq process
 * but this will need to get data out of that catalog in a 'standard' way so that 
 * data can be used with stuff like offline tasks. 
 * @author dg50
 *
 */
public class ImageDataBlock extends PamDataBlock<ImageDataUnit> {

	private TritechDaqProcess tritechDaqProcess;
	
//	private MultiFileCatalog
	
	public ImageDataBlock(TritechDaqProcess parentProcess) {
		super(ImageDataUnit.class, "Tritech Image Data", parentProcess, 0);
		this.tritechDaqProcess = parentProcess;
	
	}
	public ImageDataBlock(TritechDaqProcess parentProcess, String blockName) {
		super(ImageDataUnit.class, blockName, parentProcess, 0);
		this.tritechDaqProcess = parentProcess;
	
	}

//	@SuppressWarnings("unchecked")
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		
		super.loadViewerData(offlineDataLoadInfo, loadObserver);
		
		this.clearAll();
		MultiFileCatalog fileCatalog = findFileCatalog();
		if (fileCatalog == null) {
			return false;
		}
		
//		int totalRecords = fileCatalog.getTotalRecords();
		long tStart = offlineDataLoadInfo.getStartMillis();
		long tEnd = offlineDataLoadInfo.getEndMillis();
		/*
		 * Perhaps not very efficient, but should work. May have to later add an iterator to the catalog so that it 
		 * can work through lists of records between two time so avoid always searching from the start. This will do
		 * for now though. 
		 * Update this to skip points as much as it possibly can !
		 */
		ArrayList<GeminiFileCatalog> glfCats = fileCatalog.getCatalogList();
		for (GeminiFileCatalog glfCat : glfCats) {
			int catRecords = glfCat.getNumRecords();
			long catStart = glfCat.getFirstRecordTime();
			long catEnd = glfCat.getLastRecordTime();
			if (tStart > catEnd) {
				// the glf is earlier, so skip it
				continue;
			}
			if (tEnd < catStart) {
				// the glf is later, so we should be done and can break
				break;
			}
			for (int i = 0; i < catRecords; i++) {
				GeminiImageRecordI aRecord = glfCat.getRecord(i);
				if (aRecord.getRecordTime() < tStart) {
					continue;
				}
				if (aRecord.getRecordTime() >= tEnd) {
					continue;
				}
				ImageDataUnit dataUnit = new ImageDataUnit(aRecord.getRecordTime(), 1, aRecord);
				addPamData(dataUnit);
			}
		}
		
//		this.
		
		return true;
	}

	public MultiFileCatalog findFileCatalog() {
		TritechOffline tritechOffline = tritechDaqProcess.getTritechAcquisition().getTritechOffline();
		if (tritechOffline == null) {
			return null;
		}
		MultiFileCatalog fileCatalog = tritechOffline.getMultiFileCatalog();
		return fileCatalog;		
	}

	@Override
	public float getSampleRate() {
		// TODO Auto-generated method stub
		return 0;
	}
	
//	/**
//	 * Find a data unit in the loaded data. 
//	 */
//	public ImageDataUnit findDataUnit(long timeMillis, int sonarId) {
//		synchronized(this.getSynchLock()) {
//			ListIterator<ImageDataUnit> iterator = pamDataUnits.listIterator();
//			for 
//		}
//	}


}
