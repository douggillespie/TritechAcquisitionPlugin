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
import tritechgemini.fileio.GLFFileCatalog;
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.fileio.OfflineCatalogProgress;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechRunMode;
import tritechplugins.acquire.swing.CatalogCheckDialog;
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

	private CatalogWorker catalogWorker;
	
	private Object catalogSynchObject = new Object();
	

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
		
		menuItem = new JMenuItem("Check file catalogue");
		menuItem.setToolTipText("Check catalogueing of glf and ecd file creating fast access index files as necessary");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				checkFileCatalogue(parentFrame);
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

	/**
	 * Checks all cataloge files. 
	 * @param parentFrame
	 */
	protected void checkFileCatalogue(Frame parentFrame) {
		CatalogCheckDialog.showDialog(parentFrame, this);
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
			firstUpdateCatalog();
			break;
		}
		
	}
	
	/**
	 * Annoying in viewer mode when you've done a big
	 * drive and it then tries to catalog all files the
	 * first time you open that set in viewer mode. Can
	 * take a long time and run out of memory, so just like
	 * we do for binary storage, ask before cataloging. 
	 */
	public void firstUpdateCatalog() {
		showOfflineFilesDialog(tritechAcquisition.getGuiFrame());
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
		GLFFileCatalog.setTimeZone(params.getOfflineTimeZone());
		offlineFileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		offlineFileList.sortByFileName();
		String[] fileNames = offlineFileList.asStringList();
		
		stopCatalogWorker();
		
		clearOfflineDataMap();
		
		catalogWorker = new CatalogWorker(fileNames);
		catalogWorker.execute();
//		multiFileCatalog.catalogFiles(fileNames);
//
//		createOfflineDataMap(tritechAcquisition.getGuiFrame());
	}
	
	/**
	 * It's possible that a catalog worker is already running, in which 
	 * case it must be stopped. this will happen if the user selects a new
	 * data directory while a current one is being catalogued. 
	 */
	private void stopCatalogWorker() {
		synchronized (catalogSynchObject) {
			if (catalogWorker == null) {
				return;
			}
			catalogWorker.stop();
		}		
	}

	class CatalogWorker extends SwingWorker<Integer, OfflineCatalogProgress> implements CatalogObserver {

		private String[] fileNames;
		
		private PamWarning warning = new PamWarning("Gemini Catalog", "Gemini Catalog", 1);
		
		public CatalogWorker(String[] fileNames) {
			super();
			this.fileNames = fileNames;
			multiFileCatalog.addObserver(this);
		}

		/**
		 * Called from AWT to stop the worker. 
		 */
		public void stop() {
			multiFileCatalog.stopCataloging();
			int waitCount = 0;
			while (catalogWorker != null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (waitCount > 100) {
					System.out.println("Wait to abandon Gemini file catalog #" + ++waitCount);
					catalogWorker = null;
					stop();
					break;
				}
			}
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
				if (catProg.getLastFileName() != null) {
					warning.setWarnignLevel(0);
					warning.setWarningMessage(String.format("Scanning file %d of %d: %s", catProg.getCurrentFile(), catProg.getTotalFiles(), catProg.getLastFileName()));
					WarningSystem.getWarningSystem().addWarning(warning);
				}
				// add that point immediately to the catalog. 
				addOfflineCatalogMapPoint(catProg.getNewCatalog());
			}
		}

		@Override
		public void catalogChanged(OfflineCatalogProgress offlineCatalogProgress) {
			/*
			 *  called back in worker thread. Publishes message which
			 *  will cleverly appear in AWT thread in the above process(chunks)
			 *  function 
			 */
			publish(offlineCatalogProgress);			
		}

		@Override
		protected void done() {
			multiFileCatalog.removeObserver(this);
//			createOfflineDataMap(tritechAcquisition.getGuiFrame());
			finaliseOfflineCatalog();
			WarningSystem.getWarningSystem().removeWarning(warning);
			catalogWorker = null;
		}

	}

	/**
	 * Clear the offline datamap, if it exists. It will then hopefully
	 * get rebuilt from the CatalogWorker. 
	 */
	private void clearOfflineDataMap() {
		ImageDataBlock imageDataBlock = tritechAcquisition.getTritechDaqProcess().getImageDataBlock();
		OfflineDataMap<ImageMapPoint> imageDataMap = imageDataBlock.getOfflineDataMap(tritechAcquisition);
		if (imageDataMap != null) {
			imageDataMap.clear();
		}
	}
	
	/**
	 * Called from callback from Swing worker to add another map point. 
	 * @param singleFileCatalog
	 */
	private void addOfflineCatalogMapPoint(GeminiFileCatalog singleFileCatalog) {
		if (singleFileCatalog == null) {
			return; // might happen on final notification
		}
		ImageDataBlock imageDataBlock = tritechAcquisition.getTritechDaqProcess().getImageDataBlock();
		OfflineDataMap<ImageMapPoint> imageDataMap = imageDataBlock.getOfflineDataMap(tritechAcquisition);
		if (imageDataMap == null) {
			imageDataMap = new ImageDataMap(tritechAcquisition, imageDataBlock);
			imageDataBlock.addOfflineDataMap(imageDataMap);
		}
		imageDataMap.addDataPoint(new ImageMapPoint(singleFileCatalog));
		
		repaintDataMap(imageDataMap);
	}
	
	private void repaintDataMap(OfflineDataMap<ImageMapPoint> imageDataMap) {
		// try to find the datamap panel and repaint it. 
		DataMapControl dataMap = DataMapControl.getDataMapControl();
		if (dataMap == null) {
			return;
		}
		// this will cause a repaint, but possibly not a change of limits ? 
		dataMap.notifyModelChanged(PamControllerInterface.OFFLINE_DATA_LOADED);
		dataMap.updateSingleDataMap(imageDataMap);
	}

	
	/**
	 * Called once, from swing worker, when all the datamap points are added. 
	 */
	private void finaliseOfflineCatalog() {
		ImageDataBlock imageDataBlock = tritechAcquisition.getTritechDaqProcess().getImageDataBlock();
		OfflineDataMap<ImageMapPoint> imageDataMap = imageDataBlock.getOfflineDataMap(tritechAcquisition);
		if (imageDataMap == null) {
			return;
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
	
	/**
	 * Implementation of OfflineDataStore. Not surrently used since we're doing 
	 * a fancy rethreading thing with a swing worker to avoid blocking AWT.
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
	 * @return the tritechAcquisition
	 */
	public TritechAcquisition getTritechAcquisition() {
		return tritechAcquisition;
	}

	@Override
	public String getDataLocation() {
		return tritechAcquisition.getDataLocation();
	}



	/**
	 * END Implementation of OfflineDataStore
	 */
}
