package tritechplugins.mark;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import PamController.SettingsNameProvider;
import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import detectiongrouplocaliser.DetectionGroupSummary;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import javafx.scene.input.MouseEvent;
import tritechgemini.detect.DetectedRegion;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqProcess;
import tritechplugins.detect.swing.RegionOverlayDraw;
import tritechplugins.detect.threshold.RegionDataBlock;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.threshold.RegionLogging;
import tritechplugins.display.swing.SonarImagePanel;
import tritechplugins.display.swing.SonarsPanel;
import tritechplugins.display.swing.SonarsPanelMarker;
import tritechplugins.display.swing.overlays.SonarSymbolManager;

/**
 * Marking functions for sonar fan images. Takes mouse actions and adds items to database
 * table, similar format to automatic detections for each mark. <br>
 * Unlike the main detectors which (are currently) only saving tracks and ignoring single
 * points, this is only dealing with single points. <br>
 * Is this being added to a display or to the acquisition process ? 
 * @author dg50
 *
 */
public class SonarMarker implements SettingsNameProvider, OverlayMarkObserver {

	private TritechAcquisition tritechAcquisition;
	
	/**
	 * Can use same data type as for manual detections. 
	 */
	private RegionDataBlock regionDataBlock;

	private TritechDaqProcess daqProcess;

	private RegionLogging regionLogging;

	public SonarMarker(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		daqProcess = tritechAcquisition.getTritechDaqProcess();
		regionDataBlock = new RegionDataBlock("Manual selections", daqProcess);
		regionDataBlock.SetLogging(regionLogging = new RegionLogging(this, regionDataBlock));
		regionDataBlock.setPamSymbolManager(new SonarSymbolManager(regionDataBlock));
		RegionOverlayDraw overlay = new RegionOverlayDraw(tritechAcquisition);
		regionDataBlock.setOverlayDraw(overlay);
		daqProcess.addOutputDataBlock(regionDataBlock);
	}

	@Override
	public String getUnitName() {
		return "Manual Sonar";
	}

	@Override
	public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker,
			OverlayMark overlayMark) {
		return false;
	}

	@Override
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
		List<PamDataUnit> dataList = markSummaryData.getDataList();
		if (markSummaryData.getOverlayMark() == null) {
			return null;
		}
		List<RegionDataUnit> manualList = getManualDataList(dataList);
		List<PamDataUnit> nonManualList = getNonManualDataList(dataList);
		
		/*
		 * Dec 25, these lines dont' make sense and seem to stop
		 * creation of a popup menu. On second thoughts, this does
		 * seem to make sense for creating manual detections, but I 
		 * can't work out how we're failing to create a detection group menu.
		 */
		if (nonManualList != null && nonManualList.size() > 0) {
			return null;
		}
//		if (manualList.size() == 0 && nonManualList.size() == 0) {
//			return null;
//		}
		
		// it's a mark with no detections in it, so proceed to make a new region. 
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Create manual detection");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				createManualRegion(markSummaryData);
			}
		});
		if (manualList.size() > 0) {
			String str = String.format("Delete %d manual detection(s)", manualList.size());
			menuItem = new JMenuItem(str);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteManuals(markSummaryData);
				}
			});
			menu.add(menuItem);
		}
		
		return menu;
	}

	protected void deleteManuals(DetectionGroupSummary markSummaryData) {
		List<RegionDataUnit> manualList = getManualDataList(markSummaryData.getDataList());
		if (manualList == null || manualList.size() == 0) {
			return;
		}
		for (RegionDataUnit rdu : manualList) {
			regionDataBlock.remove(rdu, true);
		}
		repaintPanel(markSummaryData);
	}
	
	private void repaintPanel(DetectionGroupSummary markSummaryData) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SonarsPanel sonarsPanel = findSonarsPanel(markSummaryData);
				if (sonarsPanel != null) {
					sonarsPanel.remakeImages();
				}
				else {
					Object source = markSummaryData.getOverlayMark().getMarkSource();
					if (source instanceof Component) {
						Component component = (Component) source;
						component.repaint();
					}
				}
			}
		});
	}

	/**
	 * Get a list of  data units which were't manual (i.e. automatic detections). 
	 * @param dataList
	 * @return
	 */
	private List<PamDataUnit> getNonManualDataList(List<PamDataUnit> dataList) {
		ArrayList<PamDataUnit> nonManualList = new ArrayList<>();
		if (dataList == null) {
			return nonManualList;
		}
		for (PamDataUnit aUnit : dataList) {
			if (aUnit.getParentDataBlock() != regionDataBlock) {
				nonManualList.add(aUnit);
			}
		}
		return nonManualList;
	}
	/**
	 * Get a list of manual data units. 
	 * @param dataList
	 * @return
	 */
	private List<RegionDataUnit> getManualDataList(List<PamDataUnit> dataList) {
		ArrayList<RegionDataUnit> manualList = new ArrayList<>();
		if (dataList == null) {
			return manualList;
		}
		for (PamDataUnit aUnit : dataList) {
			if (aUnit.getParentDataBlock() == regionDataBlock) {
				manualList.add((RegionDataUnit) aUnit);
			}
		}
		return manualList;
	}
	
	private SonarsPanel findSonarsPanel(DetectionGroupSummary markSummaryData) {
		OverlayMark mark = markSummaryData.getOverlayMark();
		Object source = mark.getMarkSource();
		if (source instanceof SonarsPanel) {
			return (SonarsPanel) source;
		}
		return null;
	}

	protected void createManualRegion(DetectionGroupSummary markSummaryData) {
		OverlayMark mark = markSummaryData.getOverlayMark();
		Object source = mark.getMarkSource();
		if (source instanceof SonarsPanel == false) {
			return;
		}
		SonarsPanel sonarsPanel = (SonarsPanel) source;
		SonarsPanelMarker sonarsMarker = (SonarsPanelMarker) markSummaryData.getOverlayMarker();
		int imageIndex = sonarsMarker.getImageIndex();
		if (imageIndex < 0) {
			return;
		}
		GeminiImageRecordI[] currentImages = sonarsPanel.getCurrentImages();
		if (currentImages == null || imageIndex >= currentImages.length) {
			return;
		}
		// the image array may exist, but contain null values.
		GeminiImageRecordI sonarImage = currentImages[imageIndex];
		if (sonarImage == null) {
			return;
		}
		// use mark limits since they work for any shaped mark;
		double[] markLimits = mark.getLimits();
		// coordinate units are range and bearing (in radians)
		// need to convert the markLimits to angle and range bins in the image. 
		int bInd1 = sonarImage.getBearingIndex(markLimits[2]);
		int bInd2 = sonarImage.getBearingIndex(markLimits[3]);
		if (bInd2 < bInd1) {
			int tmp = bInd2;
			bInd2 = bInd1;
			bInd1 = tmp;
		}
		int rInd1 = sonarImage.getRangeIndex(markLimits[0]);
		int rInd2 = sonarImage.getRangeIndex(markLimits[1]);
		if (rInd2 < rInd1) {
			int tmp = rInd2;
			rInd2 = rInd1;
			rInd1 = tmp;
		}
		// can now pull data out of the part of the image data. 
		short[] raw = sonarImage.getShortImageData();
		if (raw == null) {
			return;
		}
		int maxVal = 0;
		int totVal = 0;
		int nP = 0;
		int peakB = bInd1;
		int peakR = rInd1;
		for (int i = bInd1; i <= bInd2; i++) {
			for (int j = rInd1; j <= rInd2; j++) {
				int ind = i + j*sonarImage.getnBeam();
				if (ind >= raw.length) {
					continue;
				}
				short val = raw[ind];
				if (val > maxVal) {
					peakB = i;
					peakR = j;
					maxVal = val;
				}
				totVal += val;
				nP++;
			}
		}
		/*
		 * 
	public DetectedRegion(long timeMilliseconds, int sonarId, double minB, double maxB, double peakB, 
			double minR, double maxR, double peakR, double objectSize, int meanV, int totV,
			int maxV, double occupancy) {
		 */
		// work out the size.
		double x1 = markLimits[0]*Math.cos(markLimits[2]);
		double y1 = markLimits[0]*Math.sin(markLimits[2]);
		double x2 = markLimits[1]*Math.cos(markLimits[3]);
		double y2 = markLimits[1]*Math.sin(markLimits[3]);
		double sz = Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
		
		double[] bearingTable = sonarImage.getBearingTable();
		double peakRange = peakR * sonarImage.getMaxRange()/sonarImage.getnRange();
		DetectedRegion dr = new DetectedRegion(sonarImage.getRecordTime(), sonarImage.getDeviceId(),
				Math.min(markLimits[2], markLimits[3]), Math.max(markLimits[2], markLimits[3]), 
				bearingTable[peakB], Math.min(markLimits[0], markLimits[1]), Math.max(markLimits[0], markLimits[1]),
				peakRange, sz, totVal/nP, totVal, maxVal, 100);
		RegionDataUnit rdu = new RegionDataUnit(sonarImage.getRecordTime(), sonarImage.getDeviceId(), dr);
		regionDataBlock.addPamData(rdu);
		sonarsPanel.repaint();
//		regionLogging.logData(DBControlUnit.findConnection(), rdu);

		repaintPanel(markSummaryData);
	}

	@Override
	public ParameterType[] getRequiredParameterTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObserverName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}

}
