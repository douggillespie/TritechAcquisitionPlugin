package tritechplugins.acquire.offline;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import PamController.OfflineDataStore;
import PamController.OfflineFileDataStore;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataGram.DatagramManager;
import dataMap.DataMapControl;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import fileOfflineData.OfflineFileList;
import pamScrollSystem.ViewLoadObserver;
import tritechgemini.fileio.CatalogObserver;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.fileio.MultiFileCatalog;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechRunMode;
import tritechplugins.acquire.swing.TritechOfflineDialog;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Functions for offline file handling in PAMGuard viewer. 
 * @author dg50
 *
 */
public class TritechOffline implements TritechRunMode, OfflineDataStore {

	private TritechAcquisition tritechAcquisition;
	
	private MultiFileCatalog multiFileCatalog;
	
	private OfflineFileList offlineFileList;
	

	public TritechOffline(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		multiFileCatalog = new MultiFileCatalog();
	}

	public JMenuItem createViewerMenu(Frame parentFrame) {
		JMenuItem menu = new JMenu(tritechAcquisition.getUnitName());
		JMenuItem menuItem = new JMenuItem("Offline files ...");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showOfflineFilesDialog(parentFrame);
			}
		});
		menu.add(menuItem);
		return menu;
	}

	protected void showOfflineFilesDialog(Frame parentFrame) {
		TritechDaqParams newParams = TritechOfflineDialog.showDialog(parentFrame, tritechAcquisition.getDaqParams());
		if (newParams != null) {
			tritechAcquisition.setDaqParams(newParams);
			updateCatalog();
		}
	}
	
//	@Override
	public int getNumSonars() {
		int[] ids = multiFileCatalog.getSonarIDs();
		if (ids == null) {
			return 0;
		}
		else {
			return ids.length;
		}
	}

//	@Override
	public int[] getSonarIDs() {
		return multiFileCatalog.getSonarIDs();
	}

	/**
	 * Get notifications from the main TritechAcquisition module. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			updateCatalog();
			break;
		}
		
	}

	/**
	 * Update the file catalog. This takes forever, so we need to 
	 * put the call to multiFileCatalog.catalogFiles into a worker 
	 * thread and have an observer update the datamap as new files
	 * slowly get added, with some kind of status message somewhere 
	 * (in error report bar as text or as a modeless dialog?). 
	 */
	public void updateCatalog() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		offlineFileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		String[] fileNames = offlineFileList.asStringList();
		
		CatalogWorker catalogWorker = new CatalogWorker(fileNames);
		catalogWorker.execute();
//		multiFileCatalog.catalogFiles(fileNames);
//
//		createOfflineDataMap(tritechAcquisition.getGuiFrame());
	}
	
	private class CatalogWorker extends SwingWorker<Integer, OfflineCatalogProgress> implements CatalogObserver {

		private String[] fileNames;
		
		private PamWarning warning = new PamWarning("Gemini Catalog", "Gemini Catalog", 1);
		
		public CatalogWorker(String[] fileNames) {
			super();
			this.fileNames = fileNames;
			multiFileCatalog.addObserver(this);
		}

		@Override
		protected Integer doInBackground() throws Exception {
			multiFileCatalog.catalogFiles(fileNames);
			return multiFileCatalog.getTotalRecords();
		}

		@Override
		protected void process(List<OfflineCatalogProgress> chunks) {
			super.process(chunks);
			for (OfflineCatalogProgress catProg : chunks) {
				warning.setWarnignLevel(0);
				warning.setWarningMessage(String.format("File %d: %s added", catProg.nFiles, catProg.lastFile));
				WarningSystem.getWarningSystem().addWarning(warning);
			}
		}

		@Override
		public void catalogChanged(int state, int nFiles, String lastFile) {
			OfflineCatalogProgress cp = new OfflineCatalogProgress(state, nFiles, lastFile);
			publish(cp);			
		}

		@Override
		protected void done() {
			multiFileCatalog.removeObserver(this);
			createOfflineDataMap(tritechAcquisition.getGuiFrame());
			WarningSystem.getWarningSystem().removeWarning(warning);
		}
		
	}

	/**
	 * Implementation of OfflineDataStore
	 */
	@Override
	public void createOfflineDataMap(Window parentFrame) {
		ImageDataBlock imageDataBlock = tritechAcquisition.getTritechDaqProcess().getImageDataBlock();
		OfflineDataMap<ImageMapPoint> imageDataMap = imageDataBlock.getOfflineDataMap(tritechAcquisition);
		if (imageDataMap == null) {
			imageDataMap = new ImageDataMap(tritechAcquisition, imageDataBlock);
			imageDataBlock.addOfflineDataMap(imageDataMap);
		}
		imageDataMap.clear();
		ArrayList<GeminiFileCatalog> catalogList = multiFileCatalog.getCatalogList();
		for (GeminiFileCatalog cat : catalogList) {
			imageDataMap.addDataPoint(new ImageMapPoint(cat));
		}
		imageDataMap.sortMapPoints();
		imageDataMap.sortRanges();
		
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					DataMapControl mapControl = DataMapControl.getDataMapControl();
					if (mapControl != null) {
						mapControl.notifyModelChanged(PamControllerInterface.EXTERNAL_DATA_IMPORTED);
					}
				}
			});
	}

	public MultiFileCatalog getMultiFileCatalog() {
		return multiFileCatalog;
	}

	@Override
	public String getDataSourceName() {
		return tritechAcquisition.getTritechDaqProcess().getImageDataBlock().getDataName();
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * END Implementation of OfflineDataStore
	 */
}
