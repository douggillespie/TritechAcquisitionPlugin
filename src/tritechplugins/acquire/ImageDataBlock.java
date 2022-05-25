package tritechplugins.acquire;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import pamScrollSystem.ViewLoadObserver;
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
public class ImageDataBlock extends PamDataBlock {

	private TritechDaqProcess tritechDaqProcess;
	
//	private MultiFileCatalog
	
	public ImageDataBlock(TritechDaqProcess parentProcess) {
		super(ImageDataUnit.class, "Tritech Image Data", parentProcess, 0);
		this.tritechDaqProcess = parentProcess;
	
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		
		super.loadViewerData(offlineDataLoadInfo, loadObserver);
		
		this.clearAll();
		MultiFileCatalog fileCatalog = findFileCatalog();
		if (fileCatalog == null) {
			return false;
		}
		
		int totalRecords = fileCatalog.getTotalRecords();
		long tStart = offlineDataLoadInfo.getStartMillis();
		long tEnd = offlineDataLoadInfo.getEndMillis();
		/*
		 * Perhaps not very efficient, but should work. May have to later add an iterator to the catalog so that it 
		 * can work through lists of records between two time so avoid always searching from the start. This will do
		 * for now though. 
		 */
		for (int i = 0; i < totalRecords; i++) {
			GeminiImageRecordI aRecord = fileCatalog.getRecord(i, false);
			if (aRecord.getRecordTime() < tStart) {
				continue;
			}
			if (aRecord.getRecordTime() >= tEnd) {
				continue;
			}
			ImageDataUnit dataUnit = new ImageDataUnit(aRecord.getRecordTime(), 1, aRecord);
			addPamData(dataUnit);
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


}
