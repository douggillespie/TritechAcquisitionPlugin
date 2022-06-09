package tritechplugins.display.swing;

import java.awt.BorderLayout;
import java.util.List;

import PamController.PamController;
import PamController.SettingsNameProvider;
import PamUtils.PamCalendar;
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
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ConfigurationObserver;
import tritechplugins.acquire.ImageDataUnit;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.TritechDaqParams;
import tritechplugins.acquire.TritechDaqSystem;
import tritechplugins.acquire.offline.TritechOffline;
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
	
	private SonarDisplayDecoration nwDecoration, neDecoration, swDecoration, seDecoration, tbDecoration;

	public SonarsOuterPanel(TritechAcquisition tritechAcquisition, SettingsNameProvider nameProvider) {
		this.tritechAcquisition = tritechAcquisition;
		sonarsPanel = new SonarsPanel(tritechAcquisition, nameProvider);
		outerPanel = new PamPanel(new BorderLayout());
		outerPanel.add(sonarsPanel.getsonarsPanel(), BorderLayout.CENTER);
		displayControlPanel = new DisplayControlPanel(this, sonarsPanel);
		
		HidingPanel hideDisplay = new HidingPanel(sonarsPanel, displayControlPanel.getMainPanel(),
				HidingPanel.HORIZONTAL, false, "Display controls", nameProvider.getUnitName() + " Display");
		sonarsPanel.add(hideDisplay, new CornerLayoutContraint(CornerLayoutContraint.LAST_LINE_END));
		
		tritechAcquisition.addConfigurationObserver(this);
//		if (tritechAcquisition.isViewer() == false) {
//			daqControlPanel = new DaqControlPanel(tritechAcquisition);
//			HidingPanel hidingPanel = new HidingPanel(sonarsPanel, daqControlPanel.getMainPanel(),
//					HidingPanel.HORIZONTAL, false, "Online controls", nameProvider.getUnitName() + " Controls");
//			sonarsPanel.add(hidingPanel, new CornerLayoutContraint(CornerLayoutContraint.LAST_LINE_START));
//
//			sonarsStatusPanel = new SonarsStatusPanel(tritechAcquisition);
//			HidingPanel hidingStatus = new HidingPanel(sonarsPanel, sonarsStatusPanel.getMainPanel(),
//					HidingPanel.HORIZONTAL, false, "Sonar Online Status", nameProvider.getUnitName() + " Status");
////			hidingStatus.setOpaque(false);
//			sonarsPanel.add(hidingStatus, new CornerLayoutContraint(CornerLayoutContraint.FIRST_LINE_START));
//		}

		if (tritechAcquisition.isViewer()) {
			viewerSlider = new PamScrollSlider("Gemini display", PamScrollSlider.HORIZONTAL, 5, 60, true);
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
			PamDataBlock trackBlocks = PamController.getInstance().getDataBlock(TrackLinkDataUnit.class, 0);
			if (trackBlocks != null) {
				viewerSlider.addDataBlock(trackBlocks);
			}
		}
		
		displayControlPanel.setParams();
		
		sortCornerDecorations();
//		sortTaskBar();
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
		public void catalogChanged() {
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
			// won't ever get used for a slider which only has a value.
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
		if (tritechOffline == null) {
			return;
		}
		int[] sonarIDs = geminiCatalog.getSonarIDs();
		if (sonarIDs == null) {
			return;
		}
//		System.out.printf("Find image records for time %s\n", PamCalendar.formatDateTime(valueMillis));
		for (int i = 0; i < sonarIDs.length; i++) {
			GeminiImageRecordI imageRec = geminiCatalog.findRecordForTime(sonarIDs[i], valueMillis);
			sonarsPanel.setImageRecord(i, imageRec);
			geminiCatalog.freeImageData(valueMillis, 10000);
		}

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
			sonarsPanel.setImageRecord(0, imageDataUnit.getGeminiImage());
		}

	}

	@Override
	public void configurationChanged() {
		sortCornerDecorations();
	}
}
