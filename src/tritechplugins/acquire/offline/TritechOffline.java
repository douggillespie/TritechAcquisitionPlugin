package tritechplugins.acquire.offline;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

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
import tritechgemini.fileio.GeminiFileCatalog;
import tritechgemini.fileio.MultiFileCatalog;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechRunMode;
import tritechplugins.acquire.swing.TritechOfflineDialog;

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
	
	@Override
	public int getNumSonars() {
		int[] ids = multiFileCatalog.getSonarIDs();
		if (ids == null) {
			return 0;
		}
		else {
			return ids.length;
		}
	}

	@Override
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
	 * Update the file catalog
	 */
	public void updateCatalog() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		offlineFileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		String[] fileNames = offlineFileList.asStringList();
		multiFileCatalog.catalogFiles(fileNames);

		createOfflineDataMap(tritechAcquisition.getGuiFrame());
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
