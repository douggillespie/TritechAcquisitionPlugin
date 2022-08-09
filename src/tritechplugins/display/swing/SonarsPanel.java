package tritechplugins.display.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import Layout.PamAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.GenericSwingDialog;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.datamenus.DataMenuParent;
import PamguardMVC.superdet.SuperDetDataBlock;
import annotation.handler.AnnotationHandler;
import javafx.scene.control.MenuItem;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import pamScrollSystem.PamScrollSlider;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.detect.threshold.BackgroundRemoval;
import tritechplugins.display.swing.overlays.OverlayTailDialogPanel;
import tritechplugins.display.swing.overlays.SonarOverlayData;
import tritechplugins.display.swing.overlays.SonarOverlayManager;

/*
 * Basic panel for drawing sonar information. Could be used in a userdisplaypanel OR in a 
 * PamTabPanel depending how things go. 
 * The sonar images are drawn directly on this panel. It uses a corner layout so that other panels
 * containing controls for the display can be squished into corners. 
 */
public class SonarsPanel extends PamPanel implements DataMenuParent {

	private TritechAcquisition tritechAcquisition;

	private SonarsPanelParams sonarsPanelParams = new SonarsPanelParams();

	private ImageDataBlock imageDataBlock;

	private int numSonars = 0;

	private HashMap<Integer, BackgroundRemoval> backgroundSubtractors = new HashMap<>();

	private HashMap<Integer, Integer> imageIndexes = new HashMap<>();

	protected SonarOverlayManager sonarOverlayManager;

	private static final int NCOLOURS = 256;

	private ColourArray colourArray = ColourArray.createHotArray(NCOLOURS);

	private SettingsNameProvider nameProvider;

	private PamAxis pamAxis = new PamAxis(0, 1, 0, 1, 0, 2, false, "m", "%3.1f");

	private double zoomFactor = 1.0;

	private Coordinate3d zoomCentre = new Coordinate3d(0, 0);

	private long currentScrollTime;

	private JPanel imagesPanel;
	
	private OverlayObserver overlayObserver;

	/**
	 * Layout for the individual sonar panels. 
	 */
	SonarImageLayout sonarImageLayout;

	private SonarsOuterPanel sonarsOuterPanel;

	public SonarsPanel(TritechAcquisition tritechAcquisition, SonarsOuterPanel sonarsOuterPanel, SettingsNameProvider nameProvider) {
		super(true);
		this.tritechAcquisition = tritechAcquisition;
		this.sonarsOuterPanel = sonarsOuterPanel;
		this.nameProvider = nameProvider;
		setLayout(new CornerLayout(new CornerLayoutContraint()));
		
		overlayObserver = new OverlayObserver();
		/**
		 * ImagesPanel fills the SonarsPanel behind the corner control panels
		 * and uses it's own layout to position a panel per sonar. Most painting will 
		 * move to SonarImagePanel's which will be added to the ImagesPanel.
		 * However a small amount of information goes into ImagesPanel, so it
		 * needs it's paint function 
		 */
		imagesPanel = new ImagesPanel(this, sonarImageLayout = new SonarImageLayout());
		this.add(imagesPanel, new CornerLayoutContraint(CornerLayoutContraint.FILL));
		
		PamSettingManager.getInstance().registerSettings(new SettingsIO());
		this.imageDataBlock = tritechAcquisition.getImageDataBlock();
		setNumSonars(numSonars);
		updateColourMap(sonarsPanelParams.colourMap);
		setToolTipText("Sonar display panel");
//
		sonarOverlayManager = new SonarOverlayManager(this);
		
		overlaySelectionChange();
	}

	public SonarsPanelParams getSonarsPanelParams() {
		return sonarsPanelParams;
	}

	public void setSonarsPanelParams(SonarsPanelParams sonarsPanelParams) {
		this.sonarsPanelParams = sonarsPanelParams;
	}

	public void setNumSonars(int numSonars) {
		this.numSonars = numSonars;
		while (imagesPanel.getComponentCount() < numSonars) {
			imagesPanel.add(new SonarImagePanel(this, imagesPanel.getComponentCount()));
		}
		while (imagesPanel.getComponentCount() > numSonars) {
			imagesPanel.remove(imagesPanel.getComponentCount()-1);
		}
		
	}
	
	/**
	 * Get the number of sonar panels. 
	 * @return number of sonar panel components
	 */
	int getNumImagePanels() {
		if (imagesPanel == null) {
			return 0;
		}
		else {
			return imagesPanel.getComponentCount();
		}
	}

	/**
	 * Used in real time operations. Needs rewriting with 
	 * new layout system
	 * @param sonarIndex
	 * @param imageRecord
	 */
	public void setImageRecord(int sonarIndex, GeminiImageRecordI imageRecord) {
//		 System.out.printf("New image record for id %d %s\n", sonarIndex,
//		 imageRecord);
//		if (imageRecord != null) {
//			sonarIndex = checkSonarIndex(imageRecord.getDeviceId());
//		}
		 if (sonarIndex >= numSonars) {
			 setNumSonars(sonarIndex+1);
		 }
		if (sonarIndex < numSonars) {
			SonarImagePanel imagePanel = getImagePanel(sonarIndex);
			if (imagePanel != null) {
				imagePanel.setImageRecord(imageRecord);
				imagePanel.repaint();
			}
//			if (imageRecord != null && sonarsPanelParams.subtractBackground) {
//				BackgroundRemoval backgroundSub = findBackgroundSub(imageRecord.getDeviceId());
//				backgroundSub.setTimeConstant(sonarsPanelParams.backgroundTimeFactor);
//				backgroundSub.setRemovalScale(sonarsPanelParams.backgroundScale);
//				imageRecord = backgroundSub.removeBackground(imageRecord, true);
//			}
//			prepareSonarImage(sonarIndex, imageRecord);
//			if (imageRecord != null && PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//				/*
//				 * Only free data in viewer since in normal mode data can always be recovered.
//				 * Probably
//				 *
//				 */
//				//				imageRecord.freeImageData();
//			}
		}
	}

	/**
	 * Gets an index for each sonar, allowing for new ones coming online after
	 * start. Will update number of plots.
	 * 
	 * @param deviceId device unique id.
	 * @return 0,1, etc. index for image drawing.
	 */
	private int checkSonarIndex(int deviceId) {
		Integer ind = imageIndexes.get(deviceId);
		if (ind == null) {
			ind = imageIndexes.size();
			imageIndexes.put(deviceId, ind);
			setNumSonars(imageIndexes.size());
		}
		return ind;
	}

	/**
	 * Find background subtractor for device, creating if necessary.
	 * 
	 * @param deviceID Device ID
	 * @return background subtractor
	 */
	private BackgroundRemoval findBackgroundSub(int deviceID) {
		BackgroundRemoval bs = backgroundSubtractors.get(deviceID);
		if (bs == null) {
			bs = new BackgroundRemoval();
			backgroundSubtractors.put(deviceID, bs);
		}
		return bs;
	}
	
	/**
	 * Scroll to this time. 
	 * @param timeMilliseconds
	 */
	public void setScrollTime(long valueMillis) {
		
		this.currentScrollTime = valueMillis;
		
		TritechOffline tritechOffline = tritechAcquisition.getTritechOffline();
		if (tritechOffline == null) {
			return;
		}
		MultiFileCatalog geminiCatalog = tritechOffline.getMultiFileCatalog();
		int[] sonarIDs = geminiCatalog.getSonarIDs();
		if (sonarIDs == null) {
//			System.out.println("No sonars");
			return;
		}
		
//		System.out.printf("Find image records for time %s\n", PamCalendar.formatDateTime(valueMillis));
		for (int i = 0; i < sonarIDs.length; i++) {
			GeminiImageRecordI imageRec = geminiCatalog.findRecordForTime(sonarIDs[i], valueMillis);
//			if (imageRec == null) {
//				System.out.println("No image for sonar " + sonarIDs[i]);
//			}
			SonarImagePanel imagePanel = getImagePanel(i);
			if (imagePanel != null) {
				getImagePanel(i).setImageRecord(imageRec);
			}
		}
		imagesPanel.repaint();
		geminiCatalog.freeImageData(valueMillis, 10000);
	}

	/**
		 * Scroll range has changed. 
		 * @param minimumMillis min data load time
		 * @param maximumMillis max data load time
		 */
		public void newScrollRange(long minimumMillis, long maximumMillis) {
			/**
			 * The main thing we need to do here is to make transparent overlay images of all
			 * the detection data to show when alldata is selected. This may also need to 
			 * be redone whenever rectangle sizes change. 
			 */
	//		remakeDataOverlays();
			int n = getNumImagePanels();
			for (int i = 0; i < n; i++) {
				getImagePanel(i).newScrollRange(minimumMillis, maximumMillis);
			}
		}

	/**
	 * Get an images panel by index. This is a cast of the panels 
	 * in the images panel. There sholdn't be anything else in there.
	 * @param panelIndex
	 * @return
	 */
	protected SonarImagePanel getImagePanel(int panelIndex) {
		if (panelIndex >= imagesPanel.getComponentCount()) {
			return null;
		}
		return (SonarImagePanel) imagesPanel.getComponent(panelIndex);
	}
	
	

	/**
	 * remake the images from fan data, e.g. after a colour map change.
	 * All this really does is set the images to null and they will get
	 * rebuilt in the paint thread when it runs. 
	 */
	public void remakeImages() {
//		for (int i = 0; i < imageFanData.length; i++) {
//			imageFanData[i] = null;
//			images[i] = null;
//			//			if (imageFanData != null) {
//			//				FanDataImage fanImage = new FanDataImage(imageFanData[i], colourArray, false, sonarsPanelParams.displayGain);
//			//				images[i] = fanImage.getBufferedImage();
//			//			}
//		}
//		clearDataOverlays();
		repaint();
	}

	/**
	 * Get the number of image pixels based on the number of bearings in the image
	 * and the size on screen and the low, medium or high resolutions options for
	 * the display.
	 * 
	 * @param nBearing
	 * @param nXPix
	 * @return pixels width of image
	 */
	protected int getImagePixels(int nBearing, int nXPix) {
		switch (sonarsPanelParams.resolution) {
		case SonarsPanelParams.RESOLUTION_DEFAULT:
			return nBearing;
		case SonarsPanelParams.RESOLUTION_HIGH:
			return Math.max(nBearing, nXPix / 2);
		case SonarsPanelParams.RESOLUTION_BEST:
			return Math.max(nBearing, nXPix);
		}
		return nBearing;
	}

	@Override
	public void paintComponent(Graphics g) {
		long paintStart = System.currentTimeMillis();
		super.paintComponent(g);
		
		Font basicFont = g.getFont();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (currentScrollTime != 0) {
			Font font = g2d.getFont();
			g2d.setColor(getForeground());
			String timeString = PamCalendar.formatDBDateTime(currentScrollTime, true);
			int sz = font.getSize();
			FontMetrics fm = g2d.getFontMetrics();
			Rectangle2D stringRect = fm.getStringBounds(timeString, g2d);
			if (stringRect.getWidth() < getWidth() / 8) {
				sz *=2;
			}
			if (stringRect.getWidth() < getWidth() / 4) {
				sz = sz*3/2;
			}
			
			font = new Font(font.getName(), Font.BOLD, sz);
			g2d.setFont(font);
			g2d.drawString(timeString, 3, getHeight()-3);
		}
		long paintEnd = System.currentTimeMillis();
//		g2d.setFont(basicFont);
//		String str = String.format(" Paint %sms", paintEnd-paintStart);
//		FontMetrics fm = g2d.getFontMetrics();
//		g2d.drawString(str, 0, fm.getAscent());
	}

	/**
	 * Get the range for the given image based on image index or a image record. 
	 * Ideally, record is used and will set the range for any data not having an image 
	 * to hand. 
	 * @param imageIndex
	 * @param imageRecord
	 * @return max range. 
	 */
	public double getImageRange(int imageIndex, GeminiImageRecordI imageRecord) {
		if (imageRecord != null) {
			double r = imageRecord.getMaxRange();
			sonarsPanelParams.setLastKnownRange(imageIndex, r);
			return r;
		}
		else {
			return sonarsPanelParams.getLastKnownRange(imageIndex);
		}
	}
	
	/**
	 * work out a vaguely sensible range step.
	 * 
	 * @param range
	 * @param height
	 * @return range step in range units.
	 */
	private double getRangeStep(double range, int height) {
		double llr = Math.ceil(Math.log10(range));
		return 0;
	}

	public void updateColourMap(ColourArrayType colourMap) {
		colourArray = ColourArray.createStandardColourArray(NCOLOURS, colourMap);
		remakeImages();
	}

	public boolean showAnottationMenu(MouseEvent e, PamDataUnit dataUnit) {
		/*
		 * Needs moving across to SonarImagePanel
		 */
		if (dataUnit == null) {
			return false;
		}
		/**
		 * Need to see if the data unit we're hovered over or any super 
		 * detections of that data unit have annotation managers. 
		 */
		JPopupMenu annotMenu = new JPopupMenu();
		PamDataUnit du = dataUnit;
		int nItems = 0;
		while (du != null) {
			PamDataBlock db = du.getParentDataBlock();
			if (db != null) {
				AnnotationHandler ah = db.getAnnotationHandler();
				if (ah != null) {
					List<JMenuItem> menuItems = ah.getAnnotationMenuItems(this, e.getPoint(), du);
					if (menuItems != null) {
						nItems++;
						for (JMenuItem mi : menuItems) {
							annotMenu.add(mi);
						}
					}
				}
			}
			du = du.getSuperDetection(0);
		}
		if (nItems == 0) {
			return false;
		}
		annotMenu.show(e.getComponent(), e.getX(), e.getY());
		return true;
	}

	protected void setTailOption(int tailOpt) {
		sonarsPanelParams.tailOption = tailOpt;
		repaint();
	}

	protected void overlayOptions(ActionEvent e) {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		boolean ans = GenericSwingDialog.showDialog(getFrame(1), "Overlay tail options",
				mouse,
				new OverlayTailDialogPanel(this));
		if (ans) {
			repaint();
		}
	}

	protected void resetZoom() {
		zoomFactor = 1.;
		zoomCentre = new Coordinate3d(0, 0);
		clearDataOverlays();
		repaint();
	}

	/**
	 * Tell the panels to clear their overlays since they will 
	 * no longer be valid and need recreating. 
	 */
	private void clearDataOverlays() {
		int n = getNumImagePanels();
		for (int i = 0; i < n; i++) {
			getImagePanel(i).clearOverlayImage();
		}
	}

	public void zoomDisplay(SonarCoordinate sonarPos, double zoom) {
		if (sonarPos == null) {
			return;
		}
		if (zoomCentre == null) {
			zoomCentre = new Coordinate3d(0, 0);
		}
		double flip = sonarsPanelParams.flipLeftRight ? -1 : 1;
		double mouseX = sonarPos.getX() * flip;
		double mouseY = sonarPos.getY();
		double dx = mouseX - zoomCentre.x;
		double dy = mouseY - zoomCentre.y;
		dx /= zoom;
		dy /= zoom;
		double newX = mouseX - dx;
		double newY = mouseY - dy;

		zoomCentre = new Coordinate3d(newX, newY);

		zoomFactor *= zoom;
		zoomFactor = Math.max(1.0, zoomFactor);
		if (zoomFactor == 1.) {
			zoomCentre = new Coordinate3d(0, 0);
		}

		clearDataOverlays();
		repaint();
	}

	private class SettingsIO implements PamSettings {

		@Override
		public String getUnitName() {
			if (nameProvider != null) {
				return nameProvider.getUnitName();
			} else {
				return getUnitType();
			}
		}

		@Override
		public String getUnitType() {
			return "Tritech Sonars Panel";
		}

		@Override
		public Serializable getSettingsReference() {
			return sonarsPanelParams;
		}

		@Override
		public long getSettingsVersion() {
			return SonarsPanelParams.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			sonarsPanelParams = (SonarsPanelParams) pamControlledUnitSettings.getSettings();
			return true;
		}

	}

	public String getDataSelectorName() {
		return nameProvider.getUnitName();
	}

	@Override
	public String getDisplayName() {
		return getDataSelectorName();
	}

	/**
	 * Important to get the main images panel to the top 
	 * of the z order since that one is drawn first. that way
	 * the corner controls all get drawn on top of it, otherwise
	 * it get's a bit messy with the main panel drawing over the 
	 * smaller control panels. 
	 */
	public void checkMainZPosition() {
		if (imagesPanel == null) {
			return;
		}
		int n = this.getComponentCount();
		setComponentZOrder(imagesPanel, n-1);
	}

	public ColourArray getColourMap() {
		return colourArray;
	}

	/**
	 * @return the zoomFactor
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}

	/**
	 * @return the zoomCentre
	 */
	public Coordinate3d getZoomCentre() {
		return zoomCentre;
	}

	/**
	 * @return the currentScrollTime
	 */
	public long getCurrentScrollTime() {
		return currentScrollTime;
	}

	/**
	 * Called back when overlay selection changes. 
	 */
	public void overlaySelectionChange() {
		clearDataOverlays();
		repaint();
		Collection<SonarOverlayData> selBlocks = sonarOverlayManager.getSelectedDataBlocks();
		for (SonarOverlayData selData : selBlocks) {
			PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(selData.dataName);
			if (dataBlock != null) {
				dataBlock.addObserver(overlayObserver);
				ArrayList<SuperDetDataBlock> superBlocks = OfflineSuperDetFilter.findPossibleSuperDetections(dataBlock);
				if (superBlocks != null) {
					for (PamDataBlock ablock : superBlocks) {
						ablock.addObserver(overlayObserver);
					}
				}
			}
		}
	}
	
	/**
	 * Observer which gets added to any super detection datablocks. This
	 * will cause the overlays to repaint if ever there is a change
	 * to things like track groups. Needed because of the creation of
	 * a static image of the overlays in viewer mode so need to force
	 * this to redraw. 
	 * @author dg50
	 *
	 */
	private class OverlayObserver extends PamObserverAdapter implements PamObserver {

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			clearDataOverlays();
			repaint();
		}

		@Override
		public String getObserverName() {
			return getDisplayName();
		}
		
	}

	/**
	 * Get a projector. For some reason this is needed for the SonarOverlayManager
	 * to get sort out the correct data selector and symbol selector for overlays. 
	 * @return a projector from the first available image. 
	 */
	public SonarXYProjector getFirstXYProjector() {
		int n = getNumImagePanels();
		for (int i = 0; i < n; i++) {
			SonarXYProjector proj = getImagePanel(i).getXyProjector();
			if (proj != null) {
				return proj;
			}
		}
		return null;
	}

	/**
	 * Move teh current scroll. Not the outer one. 
	 * @param t
	 */
	public void scrollTo(long t) {
		PamScrollSlider slider = sonarsOuterPanel.getViewerSlider();
		if (slider != null) {
			slider.setValueMillis(t);
		}
		
	}


}
