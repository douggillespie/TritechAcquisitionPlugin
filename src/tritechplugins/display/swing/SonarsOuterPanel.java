package tritechplugins.display.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import PamController.PamController;
import PamController.SettingsNameProvider;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScrollSlider;
import tritechgemini.fileio.CatalogObserver;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.fileio.OfflineCatalogProgress;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ConfigurationObserver;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqSystem;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.acquire.swing.CornerPanel;
import tritechplugins.acquire.swing.DaqControlPanel;
import tritechplugins.acquire.swing.SonarsStatusPanel;
import tritechplugins.detect.track.TrackLinkDataUnit;

/**
 * Panel around a sonarspanel which has a few more controls, such as a slider
 * bar in viewer mode and possibly some DAQ controls when we get acquisition
 * going online.
 * 
 * @author dg50
 *
 */
public class SonarsOuterPanel implements ConfigurationObserver {

	private PamPanel outerPanel;

	private TritechAcquisition tritechAcquisition;

	private SonarsPanel sonarsPanel;

	private PamScrollSlider viewerSlider;

	private TritechOffline tritechOffline;

	private MultiFileCatalog geminiCatalog;

	private DisplayControlPanel displayControlPanel;

	private DaqControlPanel daqControlPanel;

	private SonarsStatusPanel sonarsStatusPanel;
	
	private GeminiTaskBar geminiTaskBar;
	
	private TritechDaqSystem currentDaqSystem;

	private HashMap<Integer, Integer> imageIndexes = new HashMap<>();
	
	private SonarDisplayDecoration nwDecoration, neDecoration, swDecoration, seDecoration, tbDecoration;

	public SonarsOuterPanel(TritechAcquisition tritechAcquisition, SettingsNameProvider nameProvider) {
		this.tritechAcquisition = tritechAcquisition;
		sonarsPanel = new SonarsPanel(tritechAcquisition, this, nameProvider);
		outerPanel = new PamPanel(new BorderLayout());
		outerPanel.add(sonarsPanel, BorderLayout.CENTER);
		displayControlPanel = new DisplayControlPanel(this, sonarsPanel);
		
		HidingPanel hideDisplay = new HidingPanel(sonarsPanel, displayControlPanel.getMainPanel(),
				HidingPanel.HORIZONTAL, false, "Display controls", nameProvider.getUnitName() + " Display");
		sonarsPanel.add(hideDisplay, new CornerLayoutContraint(CornerLayoutContraint.LAST_LINE_END));
		
		tritechAcquisition.addConfigurationObserver(this);

		if (tritechAcquisition.isViewer()) {
			FineScrollControl fsc = new FineScrollControl(this);
			sonarsPanel.add(fsc.getComponent(), new CornerLayoutContraint(CornerLayoutContraint.FIRST_LINE_END));
			
			viewerSlider = new PamScrollSlider(nameProvider.getUnitName(), PamScrollSlider.HORIZONTAL, 10, 600000, true);
			outerPanel.add(viewerSlider.getComponent(), BorderLayout.SOUTH);
			viewerSlider.addDataBlock(tritechAcquisition.getImageDataBlock());
			viewerSlider.addObserver(new ScrollObserver());
			tritechOffline = tritechAcquisition.getTritechOffline();
			geminiCatalog = tritechOffline.getMultiFileCatalog();
			geminiCatalog.addObserver(new GemCatalogObserver());
			checkCatalog();
		} else {
			tritechAcquisition.getImageDataBlock().addObserver(new ImageObserver());
		}

		if (viewerSlider != null) {
			List<PamDataBlock> datas = sonarsPanel.sonarOverlayManager.listDataBlocks(true);
			if (datas != null) {
				for (PamDataBlock aBlock : datas) {
					viewerSlider.addDataBlock(aBlock);
				}
			}
			ArrayList<PamDataBlock> trackBlocks = PamController.getInstance().getDataBlocks(TrackLinkDataUnit.class, false);
			for (PamDataBlock aBlock : trackBlocks) {
				viewerSlider.addDataBlock(aBlock);
			}
		}
		
		displayControlPanel.setParams();
		
		sortCornerDecorations();
//		sortTaskBar();
		sonarsPanel.checkMainZPosition();
	}
	
	/*
	 * Sort out the corner decorations for the current acquisition system. 
	 */
	private void sortCornerDecorations() {
		TritechDaqSystem daqSystem = tritechAcquisition.getTritechDaqProcess().getTritechDaqSystem();
		if (daqSystem != currentDaqSystem) {
			currentDaqSystem = daqSystem;
			sortCornerDecorations(currentDaqSystem);
		}
	}
	
	/**
	 * Sort all the decorations for all the corners. 
	 * @param tritechDaqSystem
	 */
	private void sortCornerDecorations(TritechDaqSystem tritechDaqSystem) {
		SonarDisplayDecorations decorations = null; 
		if (tritechDaqSystem != null) {
			decorations = tritechDaqSystem.getSwingDecorations();
		}
		if (decorations == null) {
			// clear everything
			setDecoration(null, null, nwDecoration);
			setDecoration(null, null, neDecoration);
			setDecoration(null, null, swDecoration);
			setDecoration(null, null, seDecoration);
			if (tbDecoration != null) {
				outerPanel.remove(tbDecoration.getComponent());
				tbDecoration = null;
			}
		}
		else {
			nwDecoration = setDecoration(decorations.getNorthWestInset(), new CornerLayoutContraint(CornerLayoutContraint.FIRST_LINE_START), nwDecoration);
			swDecoration = setDecoration(decorations.getNorthEastInset(), new CornerLayoutContraint(CornerLayoutContraint.FIRST_LINE_END), neDecoration);
			swDecoration = setDecoration(decorations.getSouthWestInset(), new CornerLayoutContraint(CornerLayoutContraint.LAST_LINE_START), swDecoration);
			seDecoration = setDecoration(decorations.getSouthEastInset(), new CornerLayoutContraint(CornerLayoutContraint.FIRST_LINE_END), seDecoration);
			if (tbDecoration != null) {
				tbDecoration.destroyComponent();
				outerPanel.remove(tbDecoration.getComponent());
			}
			tbDecoration = decorations.getTopBar();
			if (tbDecoration != null) {
				outerPanel.add(BorderLayout.NORTH, tbDecoration.getComponent());
			}
		}
		sonarsPanel.invalidate();
		sonarsPanel.doLayout();
	}
	
	private SonarDisplayDecoration setDecoration(SonarDisplayDecoration newDecoration, CornerLayoutContraint cornerLayoutContraint,
			SonarDisplayDecoration oldDecoration) {
		if (oldDecoration != null) {
			oldDecoration.destroyComponent();
			sonarsPanel.remove(oldDecoration.getComponent());
		}
		if (newDecoration != null) {
			sonarsPanel.add(newDecoration.getComponent(), cornerLayoutContraint);
			sonarsPanel.checkMainZPosition();
		}
		return newDecoration;
	}

	/**
	 * @return the outerPanel
	 */
	public PamPanel getOuterPanel() {
		return outerPanel;
	}

	private class GemCatalogObserver implements CatalogObserver {
		@Override
		public void catalogChanged(OfflineCatalogProgress offlineCatalogProgress) {
			checkCatalog();
		}
	}

	private class ScrollObserver implements PamScrollObserver {

		@Override
		public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
			newScrollValue(viewerSlider.getValueMillis());
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			/*
			 *  This get's changed when the blue forwards / backwards buttons are 
			 *  pressed. 
			 */
			newScrollRange(pamScroller.getMinimumMillis(), pamScroller.getMaximumMillis());
		}

	}

	private void checkCatalog() {
		int[] sonarIDs = geminiCatalog.getSonarIDs();
		if (sonarIDs != null && sonarIDs.length > 0) {
			sonarsPanel.setNumSonars(sonarIDs.length);
		}
	}
	

//	private void sortTaskBar() {
//		TritechDaqParams params = tritechAcquisition.getDaqParams();
//		int cuurrentRunMode = params.getRunMode();
//		
//		if (cuurrentRunMode == TritechDaqParams.RUN_ACQUIRE) {
////			if (geminiTaskBar instanceof )
//		}
//		else if (cuurrentRunMode == TritechDaqParams.RUN_REPROCESS) {
//			if (geminiTaskBar instanceof PlaybackTaskBar == false) {
//				setTaskBar(new PlaybackTaskBar(tritechAcquisition));
//			}
//		}
//	}
//
//	/**
//	 * Set the task bar. don't call this often. Should be called via sortTaskBar so 
//	 * it's only done when necessary. 
//	 * @param newTaskBar
//	 */
//	private void setTaskBar(GeminiTaskBar newTaskBar) {
//		if (geminiTaskBar != null) {
//			outerPanel.remove(geminiTaskBar.getComponent());
//			geminiTaskBar.closeTaskBar();
//		}
//		geminiTaskBar = newTaskBar;
//		outerPanel.add(BorderLayout.NORTH, geminiTaskBar.getComponent());
//	}

	/**
	 * New scroll position with a value set in milliseconds.
	 * 
	 * @param valueMillis milliseconds.
	 */
	public void newScrollValue(long valueMillis) {		
		sonarsPanel.setScrollTime(valueMillis);
		/*
		 *  see if the slider has changed it's limits, in which case
		 *  data have been loaded and we need to do some remedial drawing 
		 *  of some overlays.  
		 */
//		if (viewerSlider.getMinimumMillis())
	}
	
	/**
	 * Scroll range has changed. 
	 * @param minimumMillis min data load time
	 * @param maximumMillis max data load time
	 */
	public void newScrollRange(long minimumMillis, long maximumMillis) {
		sonarsPanel.newScrollRange(minimumMillis, maximumMillis);
	}

	private class ImageObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Tritech display";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			ImageDataUnit imageDataUnit = (ImageDataUnit) pamDataUnit;
//			sonarsPanel.setNumSonars(tritechAcquisition.get);
			int index = getSonarIndex(imageDataUnit.getGeminiImage().getDeviceId());
			sonarsPanel.setImageRecord(index, imageDataUnit.getGeminiImage());
		}

	}

	/**
	 * Gets an index for each sonar, allowing for new ones coming online after
	 * start. Will update number of plots.
	 * 
	 * @param deviceId device unique id.
	 * @return 0,1, etc. index for image drawing.
	 */
	private int getSonarIndex(int deviceId) {
		Integer ind = imageIndexes.get(deviceId);
		if (ind == null) {
			ind = imageIndexes.size();
			imageIndexes.put(deviceId, ind);
		}
		return ind;
	}
	
	@Override
	public void configurationChanged() {
		sortCornerDecorations();
		imageIndexes.clear();
		sonarsPanel.setNumSonars(0);
		sonarsPanel.repaint();
	}

	/**
	 * @return the viewerSlider
	 */
	public PamScrollSlider getViewerSlider() {
		return viewerSlider;
	}

	/**
	 * Used in viewer mode to scroll forwards or backwards by a given number of frames. 
	 * @param scrollFrames
	 */
	public void scrollByFrames(int scrollFrames) {
		/**
		 * Need to work out current frame numbers and jump that many ...
		 * May need a bit of dicking about in the catalog. 
		 * What if there are two displays, do we leave on frame still and just move
		 * the other. 
		 */
//		System.out.println("Scroll by n Frames: " + scrollFrames);
		TritechOffline tritechOffline = tritechAcquisition.getTritechOffline();
		if (tritechOffline == null) {
			return;
		}
		MultiFileCatalog geminiCatalog = tritechOffline.getMultiFileCatalog();
		if (geminiCatalog == null) {
			return;
		}
		// get current frame numbers
		GeminiImageRecordI[] currentImages = sonarsPanel.getCurrentImages();
		if (currentImages == null || currentImages.length == 0) {
			return;
		}
		/**
		 * Strategy is to find the relative records, then set the average time 
		 * of those in the scroll bar
		 */
		long totalTime = 0;
		int nImage = 0;
		for (int i = 0; i < currentImages.length; i++) {
			if (currentImages[i] == null) {
				continue;
			}
			GeminiImageRecordI relImage = geminiCatalog.findRelativeRecord(currentImages[i], scrollFrames);
			if (relImage == null) {
				continue;
			}
			nImage++;
			totalTime += relImage.getRecordTime();
		}
		if (nImage == 0) {
			return;
		}
		long aveTime = totalTime/nImage;
		viewerSlider.setValueMillis(aveTime);
	}
}
