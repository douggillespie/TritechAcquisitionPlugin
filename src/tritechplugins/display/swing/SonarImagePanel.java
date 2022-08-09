package tritechplugins.display.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.PamColors.PamColor;
import PamView.PamColors;
import PamView.PanelOverlayDraw;
import PamView.paneloverlay.overlaymark.ExtMapMouseHandler;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkOverlayDraw;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import annotation.handler.AnnotationHandler;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.MenuItem;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.display.swing.overlays.SonarOverlayData;

/**
 * Put everything to do with each sonar image into subclass of JPanel.
 * This means that layouts, mouse managers and mark overlays become
 * specific to the panel and a lot easier to manage.  
 * @author dg50
 *
 */
public class SonarImagePanel extends JPanel {

	private int sonarId;
	
	private FanImageData fanImageData;
	
	private ImageFanMaker imageFanMaker;
	
	private FanDataImage fanImage;

	private GeminiImageRecordI imageRecord;
	
	private boolean isViewer;

	private SonarsPanel sonarsPanel;
	
	private SonarXYProjector xyProjector;
	
	private SonarZoomTransform sonarZoomTransform;

	private ExtMapMouseHandler externalMouseHandler;

	private SonarPanelMouse sonarsPanelMouse;
	
	private SonarsPanelMarker sonarPanelMarker;
	
	private MarkOverlayDraw markOverlayDraw;

	public MouseEvent mousePressPoint;

	public MouseEvent mouseDragPoint;

	private int panelIndex;

	private long imageTime;

	private long paintStart;
	
	private ArrayList<TextTip> textTips = new ArrayList();
	
	/**
	 * Overlay image which get's used when all data are displayed 
	 */
	private BufferedImage overlayImage;
	
	private Object overlaySynch = new Object();
	
	public SonarImagePanel(SonarsPanel sonarsPanel, int panelIndex) {
		this.panelIndex = panelIndex;
		this.sonarsPanel = sonarsPanel;
//		this.setSonarId(sonarId);
		setBackground(new Color(0.f,0.f,0.f,0.f)); // make it transparent. 
		setOpaque(false);
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		imageFanMaker = new FanPicksFromData(4);
		xyProjector = new SonarXYProjector(sonarsPanel, sonarId, sonarId);
		externalMouseHandler = new ExtMapMouseHandler(PamController.getMainFrame(), false);
		sonarPanelMarker = new SonarsPanelMarker(sonarsPanel, xyProjector, panelIndex);
		OverlayMarkProviders.singleInstance().addProvider(sonarPanelMarker);
		externalMouseHandler.addMouseHandler(sonarPanelMarker);
		sonarPanelMarker.addObserver(new OverlayMarkObserver());
		markOverlayDraw = new MarkOverlayDraw(sonarPanelMarker);

		sonarsPanelMouse = new SonarPanelMouse();
		this.addMouseListener(sonarsPanelMouse);
		this.addMouseMotionListener(sonarsPanelMouse);
		this.addMouseWheelListener(sonarsPanelMouse);
		setToolTipText("Sonar display panel. No data");
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintEverything(g);
	}

	public void paintEverything(Graphics g) {

		paintStart = System.nanoTime();
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		SonarsPanelParams sonarsPanelParams = sonarsPanel.getSonarsPanelParams();
		/*
		 * sort out zoom transforms and projectors
		 */
		double maxRange = 50;
		Rectangle panelRectangle = new Rectangle(0,0,getWidth(),getHeight());
		Rectangle imageBounds = panelRectangle;
		if (fanImage != null) {
			maxRange = fanImage.getFanData().getGeminiRecord().getMaxRange();
			BufferedImage bufIm = fanImage.getBufferedImage();
			if (bufIm != null) {
				imageBounds = new Rectangle(0,0,bufIm.getWidth(),bufIm.getHeight());
			}
		}
		sonarZoomTransform = new SonarZoomTransform(maxRange, panelRectangle, imageBounds, 
				sonarsPanel.getZoomFactor(), sonarsPanel.getZoomCentre(),
				sonarsPanelParams.flipLeftRight);
		xyProjector.setLayout(sonarZoomTransform);
//		xyProjector.setFlipImage(sonarsPanelParams.flipLeftRight);
		
		paintSonarImage(g);
		
		if (sonarsPanelParams.tailOption == SonarsPanelParams.OVERLAY_TAIL_ALL) {
			BufferedImage image = getOverlayImage();
			if (image != null) {
				g2d.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			}
		}
		else {
			paintDetectorData(g, sonarsPanel.getCurrentScrollTime());
		}
		
		paintMouseDragLine(g);
		
		markOverlayDraw.drawDataUnit(g2d, null, xyProjector);
		
		Point textPoint = new Point(g.getFontMetrics().charWidth(' '),0);
		paintTextinformation(g, textPoint, imageRecord);
	}
	
	/**
	 * paint the main sonar image. 
	 * @param g
	 */
	private void paintSonarImage(Graphics g) {
		if (fanImage == null) {
			return;
		}
		BufferedImage image = fanImage.getBufferedImage();
		if (image == null) {
			return;
		}
		boolean flip = sonarsPanel.getSonarsPanelParams().flipLeftRight;
		Rectangle imageClip = sonarZoomTransform.getImageClipRect();
		if (flip) {
//			g.drawImage(image, getWidth()+1, getHeight(), 1, 0, 0, 0, image.getWidth(), image.getHeight(), null);	
			g.drawImage(image, getWidth()+1, getHeight(), 1, 0, 
					imageClip.x, imageClip.y, imageClip.x + imageClip.width, imageClip.y + imageClip.height, null);
		}
		else {
//			g.drawImage(image, 0, getHeight(), getWidth(), 0, 0, 0, image.getWidth(), image.getHeight(), null);
			g.drawImage(image, 0, getHeight(), getWidth(), 0,
					imageClip.x, imageClip.y, imageClip.x + imageClip.width, imageClip.y + imageClip.height, null);	
		}
	}

	/**
	 * Paint all overlay data into the current graphics handle. This 
	 * may be the main panel (this) but might also be into a buffered
	 * image to speed drawing of all data.
	 * @param g
	 * @param currentTime
	 */
	private void paintDetectorData(Graphics g, long currentTime) {
		Collection<SonarOverlayData> selBlocks = sonarsPanel.sonarOverlayManager.getSelectedDataBlocks();
		xyProjector.clearHoverList();
		for (SonarOverlayData selBlock : selBlocks) {
			paintDetectorData(g, selBlock, currentTime);
		}
	}
	
	private void paintDetectorData(Graphics g, SonarOverlayData selBlock, long currentTime) {
		PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(selBlock.dataName);
		if (dataBlock == null) {
			return;
		}

		DataSelector dataSelector = dataBlock.getDataSelector(sonarsPanel.getDataSelectorName(), false);

		if (dataBlock.getPamSymbolManager() != null) {
			xyProjector.setPamSymbolChooser(
					dataBlock.getPamSymbolManager().getSymbolChooser(sonarsPanel.getDataSelectorName(), xyProjector));
		} else {
			xyProjector.setPamSymbolChooser(null);
		}
		PanelOverlayDraw overlayDraw = dataBlock.getOverlayDraw();
		if (overlayDraw == null) {
			return;
		}
		ArrayList<PamDataUnit> dataCopy = null;
		synchronized (dataBlock.getSynchLock()) {
			dataCopy = dataBlock.getDataCopy();
		}
		long tailEnd = currentTime;
		long tailStart = 0;
		switch (sonarsPanel.getSonarsPanelParams().tailOption) {
		case SonarsPanelParams.OVERLAY_TAIL_ALL:
			tailStart = 0;
			tailEnd = Long.MAX_VALUE;
			break;
		case SonarsPanelParams.OVERLAY_TAIL_NONE:
			if (imageRecord != null) {
				tailStart = tailEnd = imageRecord.getRecordTime();
			}
			break;
		case SonarsPanelParams.OVERLAY_TAIL_TIME:
			tailStart = (long) (tailEnd - sonarsPanel.getSonarsPanelParams().tailTime * 1000.);
			break;
		}
		for (PamDataUnit aUnit : dataCopy) {
			if (aUnit instanceof RegionDataUnit) {
				if (((RegionDataUnit) aUnit).getSonarId() != sonarId) {
					continue;
				}
			}
			if (aUnit.getTimeMilliseconds() < tailStart || aUnit.getTimeMilliseconds() > tailEnd) {
				continue;
			}
			overlayDraw.drawDataUnit(g, aUnit, xyProjector);
		}
		
	}
	private void paintMouseDragLine(Graphics g) {
		// txt will be null if line is outside the image.
		Color baseCol = g.getColor();
		String txt = getDragText();
		if (txt == null) {
			return;
		}
		g.setColor(sonarsPanel.getColourMap().getContrastingColour());
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		g.drawLine(mousePressPoint.getX(), mousePressPoint.getY(), mouseDragPoint.getX(), mouseDragPoint.getY());
		if (txt != null) {
			txt = " " + txt + " ";
			g.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
//			g.drawString(txt, mouseDragPoint.getX(), mouseDragPoint.getY());
			paintTextLine(g2d, txt, mouseDragPoint.getX(), mouseDragPoint.getY());
		}
	}

	public void paintTextinformation(Graphics g,
			GeminiImageRecordI geminiImageRecord) {
		LayoutInfo layoutInfo = sonarsPanel.sonarImageLayout.getLayoutInfo(this.panelIndex);
		paintTextinformation(g, layoutInfo.getTextPoint(), geminiImageRecord);
		
	}
	public void paintTextinformation(Graphics g, LayoutInfo layoutInfo,
			GeminiImageRecordI geminiImageRecord) {
		paintTextinformation(g, layoutInfo.getTextPoint(), geminiImageRecord);
	}
	
	public void paintTextinformation(Graphics g, Point txtPoint,
			GeminiImageRecordI geminiImageRecord) {
		
		textTips.clear();
		
		if (geminiImageRecord == null) {
			return;
		}
		/*
		 * and draw text into the corner of the image. Will eventually mess with font
		 * sizes, but for now, we're in get it going mode.
		 */
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
		int xt = txtPoint.x;
		int yt = txtPoint.y;
		FontMetrics fm = g2d.getFontMetrics();
		int lineHeight = fm.getHeight();
		int maxCharWidth = fm.getMaxAdvance();
		// clear a rectangle (deals with the text being on top of the axis)
		Color currCol = g.getColor();
		g.setColor(this.getBackground());
		//		g.fillRect(xt, yt, maxCharWidth * 2, lineHeight * 4);
		g.setColor(currCol);
		yt += lineHeight;
		xt += fm.charWidth(' ');
		String str;
		String filePath = geminiImageRecord.getFilePath();
		if (filePath != null) {
			File f = new File(filePath);
			str = f.getName();
			paintTextLine(g2d, str, xt, yt, "Gemini log file");
			yt += lineHeight;
		}
		str = PamCalendar.formatDBDateTime(geminiImageRecord.getRecordTime(), true);
		paintTextLine(g2d, str, xt, yt, "Record time (UTC)");
		yt += lineHeight;
		str = String.format("Sonar %d, record %d", geminiImageRecord.getDeviceId(),
				geminiImageRecord.getRecordNumber());
		paintTextLine(g2d, str, xt, yt, "Sonar ID and record index");
		yt += lineHeight;
		str = String.format("nRange %d, nAngle %d", geminiImageRecord.getnRange(), geminiImageRecord.getnBeam());
		paintTextLine(g2d, str, xt, yt, "Number of range and bearing bins");
		yt += lineHeight;
		long paintTime = System.nanoTime()-paintStart;
		str = String.format("L=%3.1fms; T=%3.1fms; P=%3.1fms", geminiImageRecord.getLoadTime() / 1000000.,
				imageTime / 1000000., paintTime / 1000000.);
		paintTextLine(g2d, str, xt, yt, "L=load, T=transform, P=paint time");
		yt += lineHeight;
		str = String.format("SoS %3.2fm/s", geminiImageRecord.getSoS());
		paintTextLine(g2d, str, xt, yt, "Speed of sound");
		yt += lineHeight;
		str = String.format("Gain %d%%", geminiImageRecord.getGain());
		paintTextLine(g2d, str, xt, yt, "Recording gain");
		
		str = getDragText();
		if (str != null) {
			yt += lineHeight;
			paintTextLine(g2d, str, xt, yt);
		}
	}
	/**
	 * Paint a line of text, which may overlap the image, so 
	 * repaint it's background in semi-transparent. 
	 * @param g2d
	 * @param str
	 * @param xt
	 * @param yt
	 */
	private void paintTextLine(Graphics2D g2d, String str, int xt, int yt) {
		paintTextLine(g2d, str, xt, yt, null);
	}
	/**
	 * Paint a line of text, which may overlap the image, so 
	 * repaint it's background in semi-transparent. 
	 * @param g2d
	 * @param str
	 * @param xt
	 * @param yt
	 * @param tip tooltip to display if mouse hoveres
	 */
	private void paintTextLine(Graphics2D g2d, String str, int xt, int yt, String tip) {
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(str, g2d);
		Color cc = g2d.getColor();
		Color bg = sonarsPanel.getBackground();
		//		Rectangle r = new Rectangle(xt, yt, xt + (int) bounds.getWidth(), yt + (int) bounds.getHeight());
		if (bg != null) {
			bg = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 192);
			g2d.setColor(bg);
		}
		Rectangle rect = new Rectangle(xt, yt-(int) bounds.getHeight()+1, (int) bounds.getWidth(), (int) bounds.getHeight()+1);
		g2d.fillRect(xt, yt-(int) bounds.getHeight()+1, (int) bounds.getWidth(), (int) bounds.getHeight()+1);
		int dip = fm.getDescent();
		if (cc != null) {
			g2d.setColor(cc);
		}
		g2d.drawString(str, xt, yt-1);
		if (tip != null) {
			textTips.add(new TextTip(rect, tip));
		}
	}

	private boolean needNewOverlay() {
		if (overlayImage == null) {
			return true;
		}
		if (overlayImage.getWidth() != getWidth() || overlayImage.getHeight() != getHeight()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get the overlay image.Create if needed. 
	 * @return
	 */
	private BufferedImage getOverlayImage() {
		if (needNewOverlay()) {
			createNewOverlay();
		}
		return overlayImage;
	}
	/**
	 * Create a new overlay image. 
	 */
	private void createNewOverlay() {
		synchronized (overlaySynch) {
			overlayImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			paintDetectorData(overlayImage.getGraphics(), sonarsPanel.getCurrentScrollTime());
		}
	}
	
	public void clearOverlayImage() {
		synchronized (overlaySynch) {
			overlayImage = null;
		}
	}
	
	/**
	 * @return the sonarId
	 */
	public int getSonarId() {
		return sonarId;
	}

	/**
	 * @param sonarId the sonarId to set
	 */
	public void setSonarId(int sonarId) {
		this.sonarId = sonarId;
	}

	/**
	 * @return the fanImageData
	 */
	public FanImageData getFanImageData() {
		return fanImageData;
	}

	/**
	 * @param fanImageData the fanImageData to set
	 */
	public void setFanImageData(FanImageData fanImageData) {
		this.fanImageData = fanImageData;
	}

	/**
	 * Set the gemini record. In viewer mode we create the image
	 * before repainting so that paint runs as fast as and we also 
	 * block the scrolling thread. In normal mode we don't make 
	 * the image and paint will get one when it can. 
	 * @param imageRec
	 */
	public void setImageRecord(GeminiImageRecordI imageRec) {
		this.imageRecord = imageRec;
		if (imageRecord == null) {
			fanImage = null;
			return;
		}
		setSonarId(imageRecord.getDeviceId());
//		if (isViewer) {
			makeFinalImage();
			repaint();
//		}
	}
	
	/**
	 * Convert a gemini image record into fan image data, then into
	 * a buffered image for display
	 */
	private void makeFinalImage() {
		if (imageRecord == null) {
			fanImageData = null;
			fanImage = null;
		}
		else {
			long t1 = System.nanoTime();
			int nBearing = imageRecord.getnBeam();
			int nXPix = getWidth();
			int usePix = sonarsPanel.getImagePixels(nBearing, nXPix);
			fanImageData = imageFanMaker.createFanData(imageRecord, usePix);
			fanImage = new FanDataImage(fanImageData, sonarsPanel.getColourMap(), true, sonarsPanel.getSonarsPanelParams().displayGain);
			fanImage.getBufferedImage(); // created and kept..
			imageTime = System.nanoTime()-t1;
		}
	}
	
	/**
	 * Called in viewer mode when the outer scroller changes. 
	 * @param minimumMillis
	 * @param maximumMillis
	 */
	public void newScrollRange(long minimumMillis, long maximumMillis) {
		clearOverlayImage();
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		
		String tip = findTextTip(event.getPoint());
		if (tip != null) {
			return tip;
		}
		
		// find where the mouse is in an image and show range-bearing data.
		SonarCoordinate sonarCoord = findSonarCoordinate(event.getX(), event.getY());
		if (sonarCoord == null) {
			return null;
		}

		// first see if we're on an overlay.
		String overlayText = null;
		// Coordinate3d c3d = new Coordinate3d(event.getX(), event.getY());
//		if (showingOverlays && overlayProjectors != null) {
//			for (int i = 0; i < overlayProjectors.length; i++) {
//				Rectangle imageRect = imageRectangles[i].getImageRectangle();
//				Point transPoint = new Point(event.getX() - imageRect.x, event.getY()-imageRect.y);
//				overlayText = overlayProjectors[i].getHoverText(transPoint);
//				if (overlayText != null) {
//					break;
//				}
//			}
//		}
//		else if (xyProjectors != null) {
				overlayText = xyProjector.getHoverText(event.getPoint());

//		}

		String str;

		if (overlayText != null) {
			str = overlayText;
		} else {
			str = "<html>";
		}

		str += String.format("<b>Mouse ...</b><br>Range %3.1fm, Angle %3.1f%s<br>xy (%3.1f, %3.1f)m",
				sonarCoord.getRange(), sonarCoord.getAngleDegrees(), LatLong.deg, sonarCoord.getX(), sonarCoord.getY());
//		FanImageData fanData = imageFanData[sonarCoord.getSonarIndex()];
		// get the amplitude for the nearest xy pixel.
		int xPix = 0, yPix = 0;
		if (fanImageData != null) {
		try {
			xPix = (int) Math.round(sonarCoord.getX() / fanImageData.getMetresPerPixX());
			yPix = (int) Math.round(sonarCoord.getY() / fanImageData.getMetresPerPixY());
			short[][] imValues = fanImageData.getImageValues();
			if (sonarsPanel.getSonarsPanelParams().flipLeftRight) {
				xPix = -xPix;
			}
			xPix += imValues.length / 2;
			// yPix = imValues[0].length-yPix;
			int val = imValues[xPix][yPix];
			str += String.format("<br>Level %d", val);
		} catch (Exception e) {
			str += String.format("<p>Invalid pixel (%d,%d)", xPix, yPix);
		}
		}
		str += "</html>";

		return str;
	}
	
	/**
	 * find a tool tip associated with some of the displayed text. 
	 * @param point
	 * @return
	 */
	private String findTextTip(Point point) {
		if (textTips == null) {
			return null;
		}
		for (TextTip aTip : textTips) {
			if (aTip.rectangle.contains(point)) {
				return aTip.tip;
			}
		}
		return null;
	}

	/**
	 * convert a panel x,y to a coordinate, only returning if 
	 * in the actual image. 
	 * @param x
	 * @param y
	 * @return
	 */
	public SonarCoordinate findSonarCoordinate(double x, double y) {
		if (sonarZoomTransform == null) {
			return null;
		}
		Coordinate3d imagePos = sonarZoomTransform.screenToImageMetres(x, y);
		if (imagePos == null) {
			return null;
		}
		double r = Math.sqrt(Math.pow(imagePos.x, 2) + Math.pow(imagePos.y, 2));
		double a = Math.atan2(imagePos.x, imagePos.y);
		if (r < 0) {
			return null;
		}
		if (imageRecord != null) {
			if (r > imageRecord.getMaxRange()) {
				return null;
			}
			double[] bt = imageRecord.getBearingTable();
			if (Math.abs(a) > Math.abs(bt[0])) {
				return null;
			}
		}
		return new SonarCoordinate(sonarId, sonarId, imagePos.x, imagePos.y);
	}
	
	/**
	 * Get text to show when the mouse is being dragged over an image.
	 * 
	 * @param imageIndex
	 * @return drag text or null if not dragging or mouse out of image.
	 */
	private String getDragText() {
		if (mousePressPoint == null || mouseDragPoint == null) {
			return null;
		}
		SonarCoordinate downCoord = findSonarCoordinate(mousePressPoint.getX(), mousePressPoint.getY());
		SonarCoordinate dragCoord = findSonarCoordinate(mouseDragPoint.getX(), mouseDragPoint.getY());
		if (downCoord == null || dragCoord == null) {
			return null;
		}

		double angle = Math
				.toDegrees(Math.atan2(dragCoord.getY() - downCoord.getY(), dragCoord.getX() - downCoord.getX()));
		double bearing = 90. - angle;
		bearing = PamUtils.constrainedAngle(bearing);
		double range = Math.sqrt(
				Math.pow(dragCoord.getY() - downCoord.getY(), 2) + Math.pow(dragCoord.getX() - downCoord.getX(), 2));
		String txt = String.format("Bearing %3.1f%s, Distance %3.1fm", bearing, LatLong.deg, range);
		return txt;
	}
	
	private class SonarPanelMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (externalMouseHandler.mousePressed(e)) {
				return;
			}
			mousePressPoint = e;
			if (e.isPopupTrigger()) {
				mousePopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (externalMouseHandler.mouseReleased(e)) {
				return;
			}
			mousePressPoint = null;
			mouseDragPoint = null;
			if (e.isPopupTrigger()) {
				mousePopup(e);
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (externalMouseHandler.mouseDragged(e)) {
				return;
			}
			mouseDragPoint = e;
			repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (externalMouseHandler.mouseWheelMoved(e)) {
				return;
			}
			this.zoomDisplay(e);
		}

		private void zoomDisplay(MouseWheelEvent e) {
			Coordinate3d zoomCentre = sonarZoomTransform.screenToImageMetres(e.getX(), e.getY());
			if (zoomCentre == null) {
				return;
			}
			double zoom = Math.pow(1.05, -e.getPreciseWheelRotation());
			SonarCoordinate sonarCoordinate = new SonarCoordinate(sonarId, sonarId, zoomCentre.x, zoomCentre.y);
			sonarsPanel.zoomDisplay(sonarCoordinate, zoom);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (externalMouseHandler.mouseClicked(e)) {
				return;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (externalMouseHandler.mouseMoved(e)) {
				return;
			}
		}

	}private void mousePopup(MouseEvent e) {
		// see if we're on a detection ...
		PamDataUnit dataUnit = xyProjector.getHoveredDataUnit();
//		PamDataUnit dataUnit = findOverlayDataUnit(e.getX(), e.getY());
		if (sonarsPanel.showAnottationMenu(e, dataUnit)) {
			return;
		}
		else {
			showStandardMenu(e);
		}
	}
//	private boolean showAnottationMenu(MouseEvent e, PamDataUnit dataUnit) {
//		if (dataUnit == null) {
//			return false;
//		}
//		/**
//		 * Need to see if the data unit we're hovered over or any super 
//		 * detections of that data unit have annotation managers. 
//		 */
//		JPopupMenu annotMenu = new JPopupMenu();
//		PamDataUnit du = dataUnit;
//		int nItems = 0;
//		while (du != null) {
//			PamDataBlock db = du.getParentDataBlock();
//			if (db != null) {
//				AnnotationHandler ah = db.getAnnotationHandler();
//				if (ah != null) {
//					List<JMenuItem> menuItems = ah.getAnnotationMenuItems(this, e.getPoint(), du);
//					if (menuItems != null) {
//						nItems++;
//						for (JMenuItem mi : menuItems) {
//							annotMenu.add(mi);
//						}
//					}
//				}
//			}
//			du = du.getSuperDetection(0);
//		}
//		if (nItems == 0) {
//			return false;
//		}
//		annotMenu.show(e.getComponent(), e.getX(), e.getY());
//		return true;
//	}

	private void showStandardMenu(MouseEvent e) {	
		
		JPopupMenu popMenu = getImageMenu(e);
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public JPopupMenu getImageMenu(MouseEvent e) {
		JPopupMenu standardItems = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Overlay options");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sonarsPanel.overlayOptions(e);
			}
		});
		standardItems.add(menuItem);
		standardItems.addSeparator();
		// add the tail options as menu items since they are so useful. 
		int[] tailOpts = SonarsPanelParams.getOverlayOptValues();
		for (int i = 0; i < tailOpts.length; i++) {
			JCheckBoxMenuItem cbi = new JCheckBoxMenuItem(SonarsPanelParams.getOverlayOptName(tailOpts[i]));
			cbi.setSelected(sonarsPanel.getSonarsPanelParams().tailOption == tailOpts[i]);
			int opt = tailOpts[i];
			cbi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sonarsPanel.setTailOption(opt);
				}
			});
			standardItems.add(cbi);
		}
		
		if (sonarsPanel.getZoomFactor() > 1) {
			menuItem = new JMenuItem("Reset zoom");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sonarsPanel.resetZoom();
				}
			});
			standardItems.addSeparator();
			standardItems.add(menuItem);
		}

		standardItems.addSeparator();
		int nOverlay = sonarsPanel.sonarOverlayManager.addSelectionMenuItems(standardItems, null, true, false, true);
//		if (nOverlay == 0) {
//			return;
		//		}
		try {
			if (imageRecord != null) {
				String filePath = imageRecord.getFilePath();
				File imFile = new File(filePath);
				if (imFile.exists()) {
					menuItem = new JMenuItem("Open file in Tritech software");
					menuItem.setToolTipText("Requires Tritech Genesis to be installed");
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (Desktop.isDesktopSupported()) {
								try {
									Desktop.getDesktop().open(imFile);
								} catch (IOException e1) {
								}
							}
						}
					});
					standardItems.addSeparator();
					standardItems.add(menuItem);
				}
			}
		}
		catch (Exception exc) {
			
		}
		PamDataUnit hoveredData = xyProjector.getHoveredDataUnit();
		if (hoveredData != null && hoveredData.getSuperDetection(0) != null) {
			hoveredData = hoveredData.getSuperDetection(0);
		}
		if (hoveredData != null) {
			String str = "Move cursor to detection";
			menuItem = new JMenuItem(str);
			long t = hoveredData.getTimeMilliseconds();
			menuItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					sonarsPanel.scrollTo(t);
				}
			});
			standardItems.add(menuItem);
		}
		
		
		
		
		return standardItems;
	}

	private class OverlayMarkObserver implements PamView.paneloverlay.overlaymark.OverlayMarkObserver {

		@Override
		public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker,
				OverlayMark overlayMark) {
			repaint();
			return true;
		}

		@Override
		public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ParameterType[] getRequiredParameterTypes() {
			return SonarXYProjector.requiredParams;
		}

		@Override
		public String getObserverName() {
			return getMarkName();
		}

		@Override
		public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMarkName() {
			return sonarsPanel.getDataSelectorName() + " Panel " + panelIndex;
		}
		
	}
	/**
	 * @return the xyProjector
	 */
	public SonarXYProjector getXyProjector() {
		return xyProjector;
	}
}
