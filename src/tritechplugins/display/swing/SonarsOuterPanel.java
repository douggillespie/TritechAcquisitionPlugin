package tritechplugins.display.swing;

import java.awt.BorderLayout;

import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScrollSlider;
import tritechgemini.fileio.CatalogObserver;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.offline.TritechOffline;

/**
 * Panel around a sonarspanel which has a few more controls, such as a slider bar in viewer mode
 * and possibly some DAQ controls when we get acquisition going online. 
 * @author dg50
 *
 */
public class SonarsOuterPanel {

	private PamPanel outerPanel;
	
	private TritechAcquisition tritechAcquisition;
	
	private SonarsPanel sonarsPanel;
	
	private PamScrollSlider viewerSlider; 
	
	private TritechOffline tritechOffline;

	private MultiFileCatalog geminiCatalog;
	
	public SonarsOuterPanel(TritechAcquisition tritechAcquisition) {
		this.tritechAcquisition = tritechAcquisition;
		sonarsPanel = new SonarsPanel(tritechAcquisition);
		outerPanel = new PamPanel(new BorderLayout());
		outerPanel.add(sonarsPanel.getsonarsPanel(), BorderLayout.CENTER);
		if (tritechAcquisition.isViewer()) {
			viewerSlider = new PamScrollSlider("Gemin i display", PamScrollSlider.HORIZONTAL, 5, 60, true);
			outerPanel.add(viewerSlider.getComponent(), BorderLayout.SOUTH);
			viewerSlider.addDataBlock(tritechAcquisition.getImageDataBlock());
			viewerSlider.addObserver(new ScrollObserver());
			tritechOffline = tritechAcquisition.getTritechOffline();
			geminiCatalog = tritechOffline.getMultiFileCatalog();
			geminiCatalog.addObserver(new GemCatalogObserver());
			checkCatalog();
		}
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
	
	/**
	 * New scroll position with a value set in milliseconds. 
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
		}
		
	}
}
