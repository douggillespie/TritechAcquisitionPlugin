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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import Layout.PamAxis;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamUtils.time.CalendarControl;
import PamView.ClipboardCopier;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.HoverData;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
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
import detectiongrouplocaliser.DetectionGroupSummary;
import pamMaths.PamVector;
import pamScrollSystem.PamScrollSlider;
import tritechgemini.detect.DetectedRegion;
import tritechgemini.fileio.MultiFileCatalog;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GLFImageRecord;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.acquire.offline.TritechOffline;
import tritechplugins.detect.threshold.RegionDataUnit;
import tritechplugins.detect.track.TrackLinkDataUnit;
import tritechplugins.detect.veto.SpatialVetoDataBlock;
import tritechplugins.display.swing.overlays.SonarOverlayData;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Put everything to do with each sonar image into subclass of JPanel.
 * This means that layouts, mouse managers and mark overlays become
 * specific to the panel and a lot easier to manage.  
 * @author dg50
 *
 */
public class SonarImagePanel extends JPanel {

//	private int sonarId;

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

	private PamAxis sideAxis;

	private File toolTipImageFile;

	private BufferedImage toolTipImage;


	/**
	 * Overlay image which get's used when all data are displayed 
	 */
	private BufferedImage overlayImage;

	/**
	 * Share the synch between displays. 
	 */
	private static Object overlaySynch = new Object();

	public long lastEscape;

	private PersistentFanImageMaker persistentFanMaker;

	private TrackLinkDataUnit lastHighlightTrack;

	private boolean neverImage = true;

	private AffineTransform rotationTransform;

	private Object inverseRotationTransform;

	public SonarImagePanel(SonarsPanel sonarsPanel, int panelIndex) {
		this.panelIndex = panelIndex;
		this.sonarsPanel = sonarsPanel;
		//		this.setSonarId(sonarId);
		setBackground(new Color(0.f,0.f,0.f,0.f)); // make it transparent. 
		setOpaque(false);
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		imageFanMaker = new FanPicksFromData(4);
		persistentFanMaker = new PersistentFanImageMaker();
		//		xyProjector = new SonarXYProjector(sonarsPanel, sonarId, sonarId);
		xyProjector = new SonarRThiProjector(this);

		externalMouseHandler = new ExtMapMouseHandler(PamController.getMainFrame(), false);
		sonarPanelMarker = new SonarsPanelMarker(sonarsPanel, xyProjector, panelIndex);
		OverlayMarkProviders.singleInstance().addProvider(sonarPanelMarker);
		externalMouseHandler.addMouseHandler(sonarPanelMarker);
		sonarPanelMarker.addObserver(new SonarMarkObserver());
		TritechAcquisition acquisition = sonarsPanel.getTritechAcquisition();
		if (acquisition != null) {
			sonarPanelMarker.addObserver(acquisition.getSonarMarker());
		}
		markOverlayDraw = new MarkOverlayDraw(sonarPanelMarker);
		sideAxis = new PamAxis(0, 1, 0,  1, 0,  1, PamAxis.BELOW_RIGHT, null, PamAxis.LABEL_NEAR_MIN, "  %3.1fm");

		sonarsPanelMouse = new SonarPanelMouse();
		this.addMouseListener(sonarsPanelMouse);
		this.addMouseMotionListener(sonarsPanelMouse);
		this.addMouseWheelListener(sonarsPanelMouse);
		setToolTipText("Sonar display panel. No data");

		this.setFocusable(true);
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				boolean ctrl = e.isControlDown();
//				char keyChar = e.getKeyChar();
//				System.out.println("Keychar is " + keyChar);
//				System.out.println("Keycode is " + e.getKeyCode());
				if (e.getKeyChar() == 't') {
					cycleTipTypes();
				}
//				else if (e.getKeyChar() == 'c') {
//					copyToolTipImage();
//				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
				char keyChar = e.getKeyChar();
//				System.out.println("Keypressed is " + keyChar);
//				System.out.println("Keypressedcode is " + e.getKeyCode());
				boolean ctrl = e.isControlDown();
				if (e.getKeyCode() == 67) {
					copyToolTipImage();
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					arrowKeyPress(-1);
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					arrowKeyPress(1);
				}

			}

		});

	}

	/**
	 * Scroll forwards or backwards by frames
	 * @param frames frames to scroll (-1 or 1).
	 */
	protected void arrowKeyPress(int frames) {
		if (isViewer == false) {
			return;
		}
		//		System.out.println("Sroll frames " + frames);
		sonarsPanel.getSonarsOuterPanel().scrollByFrames(frames);
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintEverything(g);
	}

	public void paintEverything(Graphics g) {

		paintStart = System.nanoTime();

		long currentTime = sonarsPanel.getCurrentScrollTime();

		Rectangle drawRect = checkDrawingRect();

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		SonarsPanelParams sonarsPanelParams = sonarsPanel.getSonarsPanelParams();
		/*
		 * sort out zoom transforms and projectors
		 */
		double maxRange = 50;
		Rectangle panelRectangle = drawRect; // new Rectangle(0,0,getWidth(),getHeight());
		Rectangle imageBounds = panelRectangle;
		// copy reference, just incase it get's knocked out in a different thread while we're drawing. 
		FanDataImage theFanimage = fanImage;
		if (!isViewer && theFanimage == null) {
			fanImage = theFanimage = makeFinalImage();
		}
		if (theFanimage != null) {
			neverImage = false;
			maxRange = theFanimage.getFanData().getGeminiRecord().getMaxRange();
			BufferedImage bufIm = theFanimage.getBufferedImage();
			if (bufIm != null) {
				imageBounds = new Rectangle(0,0,bufIm.getWidth(),bufIm.getHeight());
			}
			currentTime = theFanimage.getFanData().getGeminiRecord().getRecordTime();
		}
		else if (neverImage){ // only ever do this once ?
			// try to get it from the tritech acquisition
			maxRange = sonarsPanel.getDefaultMaxRange(maxRange, layoutInfo.getSonarId());
		}
		sonarZoomTransform = new SonarZoomTransform(maxRange, drawRect, imageBounds, 
				sonarsPanel.getZoomFactor(), sonarsPanel.getZoomCentre(),
				sonarsPanelParams.flipLeftRight);
		xyProjector.setLayout(sonarZoomTransform);
		/*
		 * Sort out a rotated transform if the image is rotated in any way. 
		 */
		Graphics2D rotatedG = (Graphics2D) g2d.create();
//		layoutInfo.setRotationDegrees(0*panelIndex);
		double r = layoutInfo.getRotationDegrees();
		if (r != 0) {
			/*
			 * If r is 0, then don't need to do anything, otherwise rotate bloody everything
			 */
			r = r * Math.PI/180.;
			AffineTransform at = rotatedG.getTransform();
			if (at == null) {
				at = new AffineTransform();
			}
			/**
			 * This works !
			 */
//			Point vertex = layoutInfo.getVertex();
//			double tx = layoutInfo.getImageRectangle().width/2;
//			double ty = layoutInfo.getImageRectangle().height;
//			AffineTransform rot = AffineTransform.getRotateInstance(r, tx, ty);
//			AffineTransform mv = AffineTransform.getTranslateInstance(vertex.x-tx, vertex.y-ty);
//			mv.concatenate(rot);
//			at.concatenate(mv);
			// this might not - just rotate about image centre
			double tx = layoutInfo.getImageRectangle().width/2;
			double ty = layoutInfo.getImageRectangle().height/2;
			AffineTransform rot = AffineTransform.getRotateInstance(r, tx, ty);
			at.concatenate(rot);
			
			
			rotatedG.setTransform(at);
			rotationTransform = rot;
			sonarZoomTransform.setrTransform(rot);
			try {
				inverseRotationTransform = rot.createInverse();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
		}
		else {
			sonarZoomTransform.setrTransform(null);
			rotationTransform = new AffineTransform();
			inverseRotationTransform = new AffineTransform();
		}
		//		xyProjector.setFlipImage(sonarsPanelParams.flipLeftRight);

		paintSonarImage(rotatedG, drawRect, theFanimage);

		if (sonarsPanelParams.showGrid) {
			paintGrid(g2d, drawRect);
		}

		paintVetos(rotatedG, drawRect);

		if (isViewer && sonarsPanelParams.tailOption == SonarsPanelParams.OVERLAY_TAIL_ALL) {
			BufferedImage image = getOverlayImage();
			if (image != null) {
				g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			}
		}
		else {
			paintDetectorData(g, drawRect, currentTime);
		}

		paintMouseDragLine(g);

		markOverlayDraw.drawDataUnit(g2d, null, xyProjector);

		Point textPoint = new Point(g.getFontMetrics().charWidth(' '),0);
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL && panelIndex == 0) {
			textPoint.x = getWidth()*3/4;
			textPoint.y = getHeight()*3/4;
		}
		paintTextinformation(g, textPoint, imageRecord);
	}

	private void paintVetos(Graphics g, Rectangle destRect) {
		Graphics2D g2d = (Graphics2D) g;
		TritechAcquisition acquisition = sonarsPanel.getTritechAcquisition();
	}


	private void paintGrid(Graphics g, Rectangle destRect) {
//		if (fanImage == null ||imageRecord == null) {
//			return;
//		}

		Color col = sonarsPanel.getColourMap().getContrastingColour();
		col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 192);

		g.setColor(col);
		
		double rotate = layoutInfo.getRotationDegrees();
		double rotateRadians = rotate*Math.PI/180.;
//		rotate = 0;

		Coordinate3d zero = xyProjector.getCoord3d(0, 0, false);
		if (zero == null) {
			return;
		}
		Double range = xyProjector.getMaxRange();
		if (range == null) {
			return;
		}
		double maxAng = Math.toRadians(sonarsPanel.sonarImageLayout.getMaxAngleDegrees());
		if (imageRecord != null) {
			double[] bearings = imageRecord.getBearingTable();
			maxAng = Math.abs(bearings[0]);
		}
		double[] toPlot = {-1., -0.5, 0, .5, 1};
		//		double x = 0, y = 0;
		Coordinate3d end = null;
		Coordinate3d maxXend = new Coordinate3d(0,0,0);
		for (int i = 0; i < toPlot.length; i++) {
			//			x = range*Math.sin(toPlot[i]*maxAng);
			//			y = range*Math.cos(toPlot[i]*maxAng);
			//			 end = xyProjector.getCoord3d(x, y, false);
			end = xyProjector.getCoord3d(range, toPlot[i]*maxAng, false);
			if (end.x > maxXend.x) {
				maxXend = end;
			}
			g.drawLine((int)zero.x,  (int)zero.y,  (int)end.x,  (int)end.y);
		}
		if (end == null) {
			return;
		}
		sideAxis.setRange(0, range);
		sideAxis.setDrawLine(false);
		sideAxis.setLabelPos(PamAxis.LABEL_NEAR_MAX);
		if (Math.abs(rotate) > 90) {
			sideAxis.setTickPosition(PamAxis.ABOVE_LEFT);
		}
		else {
			sideAxis.setTickPosition(PamAxis.BELOW_RIGHT);
		}
		sideAxis.drawAxis(g, (int)zero.x,  (int)zero.y,  (int)maxXend.x,  (int)maxXend.y);

		// now the curves...
		ArrayList<Double> ranges = sideAxis.getAxisValues();
		for (Double aRange : ranges) {
			if (aRange == null || aRange == 0) {
				continue;
			}
			//			aRange = range*.5;
			g.setColor(col);
			//			x = aRange*Math.sin(maxAng);
			//			y = aRange*Math.cos(maxAng);
			end = xyProjector.getCoord3d(aRange*2, maxAng, false);
			//			double width = Math.abs(end.x-zero.x);
			//			y = end.y;
			Coordinate3d end2 = xyProjector.getCoord3d(aRange*2, -maxAng-rotateRadians, false);
			int maxDegs = (int) Math.toDegrees(maxAng);
			int ang1 = (int) (90-maxDegs-1-rotate);
			int ang2 = 2*maxDegs+2;
			int h = (int) Math.abs(end2.y-zero.y);
			g.drawArc((int) (zero.x-h), (int) (zero.y-h), (int) (2*+h), (int) (2*h), ang1, ang2);
			//			g.drawRect((int) (zero.x-width), (int) (zero.y-h), (int) (2*+width), (int) (2*h));
			//			g.drawLine((int) (zero.x-width), (int) (zero.y-h), (int) (zero.x+width), (int) (zero.y+h));
			//			break;
		}
	}

	private void paintGridWrong(Graphics g, Graphics2D rotatedG) {
//		if (fanImage == null ||imageRecord == null) {
//			return;
//		}

		Color col = sonarsPanel.getColourMap().getContrastingColour();
		col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 192);
//		col = Color.red;
		g.setColor(col);
		rotatedG.setColor(col);

		Point vertex = layoutInfo.getVertex();
//		Coordinate3d zero = xyProjector.getCoord3d(0, 0, false);
		Coordinate3d zero = new Coordinate3d(vertex.getX(), vertex.getY());
		if (zero == null) {
			return;
		}
		double r = layoutInfo.getRotationDegrees();
		double rRadians = r * Math.PI/180.;
		Double range = xyProjector.getMaxRange();
		if (range == null) {
			return;
		}
		double maxAng = Math.toRadians(sonarsPanel.sonarImageLayout.getMaxAngleDegrees());
		if (imageRecord != null) {
			double[] bearings = imageRecord.getBearingTable();
			maxAng = Math.abs(bearings[0]);
		}
		double[] toPlot = {-1., -0.5, 0, .5, 1};
		//		double x = 0, y = 0;
		Coordinate3d end = null;
		Coordinate3d maxXend = new Coordinate3d(0,0,0);
		Coordinate3d botPoint = xyProjector.getCoord3d(0, 0, false);
		Coordinate3d topPoint = xyProjector.getCoord3d(xyProjector.getMaxRange(), 0, false);
		double lineLen = topPoint.distance(botPoint);
		
		for (int i = 0; i < toPlot.length; i++) {
			//			x = range*Math.sin(toPlot[i]*maxAng);
			//			y = range*Math.cos(toPlot[i]*maxAng);
			//			 end = xyProjector.getCoord3d(x, y, false);
			int x = (int) (lineLen*Math.sin(maxAng*toPlot[i]+rRadians));
			int y = (int) (lineLen*Math.cos(maxAng*toPlot[i]+rRadians));
			end = xyProjector.getCoord3d(range, toPlot[i]*maxAng, false);
//			if (end.x > maxXend.x) {
//				maxXend = end;
//			}
			g.drawLine(vertex.x, vertex.y, vertex.x+x, vertex.y-y);
//			g.drawLine((int)zero.x,  (int)zero.y,  (int)end.x,  (int)end.y);
		}
		if (end == null) {
			return;
		}
		sideAxis.setRange(0, range);
		sideAxis.setDrawLine(false);
		sideAxis.setLabelPos(PamAxis.LABEL_NEAR_MAX);
		sideAxis.drawAxis(g, (int)zero.x,  (int)zero.y,  (int)maxXend.x,  (int)maxXend.y);

		// now the curves...
		ArrayList<Double> ranges = sideAxis.getAxisValues();
		for (Double aRange : ranges) {
			if (aRange == null || aRange == 0) {
				continue;
			}
			//			aRange = range*.5;
			g.setColor(col);
			//			x = aRange*Math.sin(maxAng);
			//			y = aRange*Math.cos(maxAng);
			end = xyProjector.getCoord3d(aRange*2, maxAng, false);
			//			double width = Math.abs(end.x-zero.x);
			//			y = end.y;
			Coordinate3d end2 = xyProjector.getCoord3d(aRange*2, -maxAng, false);
			int maxDegs = (int) Math.toDegrees(maxAng);
			int ang1 = (int) (90-maxDegs-1+r);
			int ang2 = (int) (2*maxDegs+2);
			int h = (int) Math.abs(end2.y-zero.y);
			g.drawArc((int) (zero.x-h), (int) (zero.y-h), (int) (2*+h), (int) (2*h), ang1, ang2);
			//			g.drawRect((int) (zero.x-width), (int) (zero.y-h), (int) (2*+width), (int) (2*h));
			//			g.drawLine((int) (zero.x-width), (int) (zero.y-h), (int) (zero.x+width), (int) (zero.y+h));
			//			break;
		}
	}


	/**
	 * paint the main sonar image. 
	 * @param g
	 * @param theFanimage 
	 */
	private void paintSonarImage(Graphics2D g2d, Rectangle destRect, FanDataImage theFanimage) {
		if (theFanimage == null) {
			return;
		}
		BufferedImage image = theFanimage.getBufferedImage();
		if (image == null) {
			return;
		}
		boolean flip = sonarsPanel.getSonarsPanelParams().flipLeftRight;
		Rectangle imageClip = sonarZoomTransform.getImageClipRect();
		/*
		 *  should really get this from the projector, which will handle all the rotations
		 *  as needed I think. Perhpas not though, since the image is already zoomed ?
		 *  give it a go ...  
		 *  Don't bother - need to rotate, or it's not going to be able to do anything apart from
		 *  90 degree intervals. 
//		 */
//		if (imageRecord != null) {
//		double rMax = imageRecord.getMaxRange();
//		double aMax = Math.abs(imageRecord.getBearingTable()[0]);
//		double xMax = rMax * Math.sin(aMax);
//		Coordinate3d bl = sonarZoomTransform.imageMetresToScreen(0, -xMax);
//		}
		
//		g.fillRect(0, 0, getWidth(), getHeight());0
		int minX = (int) destRect.getMinX();
		int maxX = (int) destRect.getMaxX();
		int minY = (int) destRect.getMinY();
		int maxY = (int) destRect.getMaxY();
		if (flip) {
			g2d.drawImage(image, maxX+1, maxY, minX+1, minY, 
					imageClip.x, imageClip.y, 
					imageClip.x + imageClip.width, imageClip.y + imageClip.height, null);
		}
		else {
			g2d.drawImage(image, minX, maxY, maxX, minY,   
					imageClip.x, imageClip.y, 
					imageClip.x + imageClip.width, imageClip.y + imageClip.height, null);
//			g2d.drawImage(image, 0, getHeight(), getWidth(), 0,
//					imageClip.x, imageClip.y, imageClip.x + imageClip.width, imageClip.y + imageClip.height, null);	
		}
	}

	/**
	 * Check the drawing clip. This is often the exact size of the window, but
	 * particularly with the support for sonars with different swaths, it may
	 * now be less. 
	 * @return
	 */
	private Rectangle checkDrawingRect() {
		Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
		if (imageRecord == null) {
			return r;
		}
		double[] bt = imageRecord.getBearingTable();
		if (bt == null || bt.length == 0) {
			return r;
		}
		double maxAng = Math.abs(bt[0]);
		double aspect = 2*Math.sin(maxAng);
		double panelAspect = (double) getWidth() / (double) getHeight();
		if (aspect == panelAspect) {
			return r;
		}
		if (aspect > panelAspect) {
			// image is too wide, so shrink in height
			int newH = (int) (getWidth() / aspect);
			r.y = (r.height-newH)/2;
			r.height = newH;
		}
		else {
			// image is too high, so shrink in width
			int newW = (int) (getHeight() * aspect);
			r.x = (r.width-newW)/2;
			r.width = newW;
		}
		return r;
	}

	/**
	 * Paint all overlay data into the current graphics handle. This 
	 * may be the main panel (this) but might also be into a buffered
	 * image to speed drawing of all data.
	 * @param g
	 * @param currentTime
	 */
	private void paintDetectorData(Graphics g, Rectangle destRect, long currentTime) {
		Collection<SonarOverlayData> selBlocks = sonarsPanel.sonarOverlayManager.getSelectedDataBlocks();
		xyProjector.clearHoverList();
		for (SonarOverlayData selBlock : selBlocks) {
			paintDetectorData(g, destRect, selBlock, currentTime);
		}
	}

	private void paintDetectorData(Graphics g, Rectangle destRect, SonarOverlayData selBlock, long currentTime) {
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
		long tailEnd = currentTime+1;
		long tailStart = 0;
		switch (sonarsPanel.getSonarsPanelParams().tailOption) {
		case SonarsPanelParams.OVERLAY_TAIL_ALL:
			// get the scroll times, since some dat amay be loaded elsewhere. 
			if (isViewer) {
				PamScrollSlider scroller = sonarsPanel.getSonarsOuterPanel().getViewerSlider();
				if (scroller != null) {
					tailStart = scroller.getMinimumMillis();
					tailEnd = scroller.getMaximumMillis();
				}
			}
			//			tailStart = 0;
//			tailEnd = Long.MAX_VALUE;
			break;
		case SonarsPanelParams.OVERLAY_TAIL_NONE:
			if (imageRecord != null) {
				tailStart = tailEnd = imageRecord.getRecordTime();
				tailStart-=10;
				tailEnd+=10;
			}
			break;
		case SonarsPanelParams.OVERLAY_TAIL_TIME:
			tailStart = (long) (tailEnd - sonarsPanel.getSonarsPanelParams().tailTime * 1000.);
			break;
		}
		if (dataBlock instanceof SpatialVetoDataBlock) {
			tailStart = 0;
			tailEnd = Long.MAX_VALUE;
		}
		synchronized (dataBlock.getSynchLock()) {
			dataCopy = dataBlock.getDataCopy(tailStart, tailEnd, false, dataSelector);
		}
		overlayDraw.preDrawAnything(g, dataBlock, xyProjector);
		//		System.out.printf("Paint tail from %s to %s\n", PamCalendar.formatDBDateTime(tailStart), PamCalendar.formatDBDateTime(tailEnd));
		boolean drawSpecial = overlayDraw.canDraw(xyProjector) == false;
		ArrayList<PamDataUnit> laterList = new ArrayList();
//		System.out.printf("Draw detector data panel %d sonar %d\n", panelIndex, layoutInfo.getSonarId());
		for (PamDataUnit aUnit : dataCopy) {
			if (aUnit.getTimeMilliseconds() < tailStart || aUnit.getTimeMilliseconds() > tailEnd) {
				continue;
			}
			if (aUnit instanceof RegionDataUnit) {
				if (((RegionDataUnit) aUnit).getSonarId() != layoutInfo.getSonarId()) {
					continue;
				}
				if (getClickedOnTrack() != null && aUnit.getSuperDetection(TrackLinkDataUnit.class) == getClickedOnTrack()) {
					laterList.add(aUnit);
					continue;
				}
			}
			if (drawSpecial) {
				drawHere(g, overlayDraw, aUnit, xyProjector);
			}
			else {
				overlayDraw.drawDataUnit(g, aUnit, xyProjector);
			}
		}
		for (PamDataUnit aUnit : laterList) {
			if (drawSpecial) {
				drawHere(g, overlayDraw, aUnit, xyProjector);
			}
			else {
				overlayDraw.drawDataUnit(g, aUnit, xyProjector);
			}
		}

	}
	private void drawHere(Graphics g, PanelOverlayDraw overlayDraw, PamDataUnit aUnit, SonarXYProjector xyProjector) {
		// assume that all we have is a bearing. 
		PamSymbol symbol = overlayDraw.getPamSymbol(aUnit, xyProjector);
		AbstractLocalisation loc = aUnit.getLocalisation();
		if (loc == null) {
			return;
		}
		PamVector[] vecs = loc.getWorldVectors();
		double[] angles = loc.getAngles();
		if (vecs == null) {
			return;
		}
		if (symbol != null) {
			g.setColor(symbol.getLineColor());
		}
		try {
			for (int i = 0; i < vecs.length; i++) {
				double angle = vecs[i].getHeading();
				//			angle = 0;
				Coordinate3d zero = xyProjector.getCoord3d(new Coordinate3d(0,0,0), false);
				if (zero == null) return;
				Point p1 = zero.getXYPoint();
				Coordinate3d end = xyProjector.getCoord3d(new Coordinate3d(55, angle, 0), false);
				if (end == null) return;
				Point p2 = end.getXYPoint();
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
				xyProjector.addHoverData(end, aUnit);
			}
		}
		catch (Exception e) {

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
		// check it's GLF
		if (geminiImageRecord instanceof GLFImageRecord) {
			GLFImageRecord glfRecord = (GLFImageRecord) geminiImageRecord;
			str = String.format("%s, %s Mode", glfRecord.getDeviceType(), glfRecord.isHF() ? "HF" : "LF");
			paintTextLine(g2d, str, xt, yt, "Device Info");
			yt += lineHeight;
		}
		str = PamCalendar.formatDBStyleTime(geminiImageRecord.getRecordTime(), true, true);
		//		str += " " + PamCalendar.getShortDisplayTimeZoneString(true);
		str += CalendarControl.getInstance().getTZCode(true);
		paintTextLine(g2d, str, xt, yt, "Record time (UTC)");
		yt += lineHeight;
		//		if (isViewer) {
		//			str = String.format("Sonar %d, record %d", geminiImageRecord.getDeviceId(),
		//					geminiImageRecord.getRecordNumber());
		//		}
		//		else {
		str = String.format("Sonar %d, rec %d, chirp %s", geminiImageRecord.getDeviceId(),
				geminiImageRecord.getRecordNumber(), geminiImageRecord.getChirp() > 0 ? "on" : "off");
		//		}
		paintTextLine(g2d, str, xt, yt, "Sonar ID, record index and chirp mode");
		yt += lineHeight;
		if (geminiImageRecord instanceof GLFImageRecord) {
			GLFImageRecord glfRecord = (GLFImageRecord) geminiImageRecord;
			str = String.format("nRange %d, nAngle %d, comp %d", geminiImageRecord.getnRange(), 
					geminiImageRecord.getnBeam(), glfRecord.rangeCompression);
		}
		else {
			str = String.format("nRange %d, nAngle %d", geminiImageRecord.getnRange(), 
					geminiImageRecord.getnBeam());
		}
		paintTextLine(g2d, str, xt, yt, "Number of range and bearing bins");
		yt += lineHeight;
		long paintTime = System.nanoTime()-paintStart;
		String tip;
		if (isViewer) {
			str = String.format("L=%3.1fms; T=%3.1fms; P=%3.1fms", geminiImageRecord.getLoadTime() / 1000000.,
					imageTime / 1000000., paintTime / 1000000.);
			tip = "L=load time, T=transform time, P=paint time";
		}
		else {
			str = String.format("T=%3.1fms; P=%3.1fms", 
					imageTime / 1000000., paintTime / 1000000.);
			tip = "T=transform time, P=paint time";
		}
		paintTextLine(g2d, str, xt, yt, tip);
		yt += lineHeight;
		str = String.format("SoS %3.2fm/s", geminiImageRecord.getSoS());
		paintTextLine(g2d, str, xt, yt, "Speed of sound");
		yt += lineHeight;
		str = String.format("Gain %d%%, Range %3.1fm", geminiImageRecord.getGain(), 
				geminiImageRecord.getMaxRange());
		paintTextLine(g2d, str, xt, yt, "Recording gain and range");


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
		if (lastHighlightTrack != getClickedOnTrack()) {
			lastHighlightTrack = getClickedOnTrack();
			return true;
		}
		return false;
	}

	/**
	 * Get the overlay image.Create if needed. Only applies in viewer
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
		BufferedImage synchedImage = null;
		synchronized (overlaySynch) {
			overlayImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			synchedImage = overlayImage;
		}
		Rectangle destRect = checkDrawingRect();
		paintDetectorData(synchedImage.getGraphics(), destRect, sonarsPanel.getCurrentScrollTime());
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
		if (layoutInfo == null) {
			return -1;
		}
		return layoutInfo.getSonarId();
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
	 * the image and paint will get one when it can. In normal mode it's
	 * possible that not every frame will get painted. 
	 * @param imageRec
	 */
	public void setImageRecord(GeminiImageRecordI imageRec) {
		this.imageRecord = imageRec;
		if (imageRecord == null) {
			fanImage = null;
			return;
		}
		if (isViewer) {
			fanImage = makeFinalImage();
			repaint();
		}
		else {
			fanImageData = null;
			fanImage = null;
			repaint(10);
		}
	}

	/**
	 * Get the current image record. 
	 * @return current image record.
	 */
	public GeminiImageRecordI getImageRecord() {
		return imageRecord;
	}

	/**
	 * Convert a gemini image record into fan image data, then into
	 * a buffered image for display
	 * @return 
	 */
	private FanDataImage makeFinalImage() {
		FanDataImage aFanImage = null;
		if (imageRecord == null) {
			fanImageData = null;
			fanImage = null;
		}
		else {
			long t1 = System.nanoTime();
			int nBearing = imageRecord.getnBeam();
			int nRange = imageRecord.getnRange();
			int nXPix = getWidth();
			int usePix = sonarsPanel.getImagePixels(nBearing, nRange, nXPix);
			SonarZoomTransform zoomTrans = xyProjector.getSonarZoomTransform();
			if (zoomTrans != null) {
				double zoom = xyProjector.getSonarZoomTransform().getZoomFactor();
				if (zoom > 1) {
					// don't do this, it slows it down FAR too much. 
					//					usePix = (int) (usePix*Math.min(zoom, 2));
				}
			}
			SonarsPanelParams panelParams = sonarsPanel.getSonarsPanelParams();
			fanImageData = imageFanMaker.createFanData(imageRecord, usePix);
			/**
			 * Multithreading, so small chance fanImageData may have been reset between
			 * last line and next line, so copy the refernece in order that this can't 
			 * happen and deal with possibility of null of the local variable
			 */
			FanImageData totallyFinalData = fanImageData;
			if (totallyFinalData == null) {
				return null;
			}
			if (panelParams.usePersistence) {
				totallyFinalData = persistentFanMaker.makePersistentImage(totallyFinalData, 
						panelParams.persistentFrames, panelParams.rescalePersistence);
			}

			aFanImage = new FanDataImage(totallyFinalData, sonarsPanel.getColourMap(), true, panelParams.displayGain);
			//			t1 = System.nanoTime();
			aFanImage.getBufferedImage(); // created and kept..
			imageTime = System.nanoTime()-t1;
		}
		return aFanImage;
	}

	/**
	 * Called in viewer mode when the outer scroller changes. 
	 * @param minimumMillis
	 * @param maximumMillis
	 */
	public void newScrollRange(long minimumMillis, long maximumMillis) {
		clearOverlayImage();
	}

	private PamWarning tipWarning = new PamWarning("Tritech tool tips", "", 1);

	/**
	 * Information on the panel bounds and also where the
	 * image vertex lies and any rotation to apply. 
	 */
	private LayoutInfo layoutInfo;

	private PamCoordinate dataPos;
	
	private void cycleTipTypes() {
		sonarsPanel.getSonarsPanelParams().cycleTipType();
		String currType = sonarsPanel.getSonarsPanelParams().getTipDescription();
		if (currType != null) {
			tipWarning.setWarningMessage("Tooltip type set to " + currType);
			WarningSystem.getWarningSystem().addWarning(tipWarning);
			Timer stopTimer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					WarningSystem.getWarningSystem().removeWarning(tipWarning);
				}
			});
			stopTimer.setRepeats(false);
			stopTimer.start();
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {

		//		if (System.currentTimeMillis() - lastEscape < 10000) {
		//			return null;
		//		}


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
		overlayText = xyProjector.getHoverText(event.getPoint());


		String str;

		if (overlayText != null) {
			str = overlayText;
		} else {
			str = "<html>";
		}
		SonarsPanelParams panelParams = sonarsPanel.getSonarsPanelParams();
		if ((panelParams.getToolTipType() & SonarsPanelParams.TOOLTIP_IMAGE) != 0) {
			BufferedImage image = null;
			FanDataImage tipImage = null;
			if (isViewer) {
				tipImage = getToolTipImage();
				if (tipImage != null) {
					image = tipImage.getBufferedImage();
				}
			}
			if (image != null)  {
				try {
					if (toolTipImageFile == null) {
						toolTipImageFile = File.createTempFile("littleimage", ".jpg");
						toolTipImageFile.deleteOnExit();
					}
					int imWid = 200;
					int imHei = 200;
					if (image.getHeight() > image.getWidth()) {
						imWid = imHei * image.getWidth() / image.getHeight();
					}
					else {
						imHei = imWid * image.getHeight() / image.getWidth();
					}

					//				if (toolTipImage == null) {
					toolTipImage = new BufferedImage(imWid, imHei, BufferedImage.TYPE_INT_RGB);
					//				}
					if ( panelParams.flipLeftRight) {
						toolTipImage.createGraphics().drawImage(image, imWid, imHei, 0, 0, 
								0, 0, image.getWidth(), image.getHeight(), sonarsPanel);
					}
					else {
						toolTipImage.createGraphics().drawImage(image, 0, imHei, imWid, 0, 
								0, 0, image.getWidth(), image.getHeight(), sonarsPanel);
					}
					ImageIO.write(toolTipImage, "jpg", toolTipImageFile);
					// https://developer.mozilla.org/en-US/docs/Web/CSS/image-rendering
					double imW = tipImage.getFanData().getMetresPerPixX()*image.getWidth();
					double imH = tipImage.getFanData().getMetresPerPixX()*image.getHeight();
					String newLine = String.format("<img src=\"%s\">&nbsp; (%3.1fx%3.1fm)",  
							toolTipImageFile.toURI(), imW, imH);
					// put after the first line. 
					int firstBr = str.indexOf("<br>");
					if (firstBr > 0) {
						if (panelParams.getToolTipType() == SonarsPanelParams.TOOLTIP_BOTH) {
							str = str.replaceFirst("<br>", "<br>" + newLine + "<br>");
						}
						else {
							PamDataUnit hoverData = xyProjector.getHoveredDataUnit();
							str = "<html>"+ PamCalendar.formatDBDateTime(hoverData.getTimeMilliseconds(), true) +
									"<p>" + newLine  +  "<br></html>";
							return str;
						}
					}
					else {
						str += newLine;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
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
				// this not needed, since coordinate ref back to original unflipped image. 
				//			if (sonarsPanel.getSonarsPanelParams().flipLeftRight) {
				//				xPix = -xPix;
				//			}
				xPix += imValues.length / 2;
				int val = imValues[xPix][yPix];
				str += String.format("<br>Level %d", val);
			} catch (Exception e) {
				str += String.format("<p>Invalid pixel (%d,%d)", xPix, yPix);
			}
		}
		str += "</html>";

		return str;
	}

	@Override
	public Point getToolTipLocation(MouseEvent event) {
		/**
		 * Offset the tooltip by a tiny amount to the right. 
		 * Swing will still position it vertically if it's going
		 * off the since of the screen. The offset allows the mouse to 
		 * move over the underlying window, rather than the tooltip,
		 * when you move it to the right, so that image updates correctly
		 * occur as you move the mouse around.  
		 */
		Point pt = new Point(event.getPoint());
		pt.x += 5;
		pt.y += 2;
		return pt;
	}
	

	/**
	 * If there is a tooltip image, copy it to the clip board. 
	 */
	protected void copyToolTipImage() {
		FanDataImage tipImage = getToolTipImage();
		if (tipImage == null) {
			return;
		}
		BufferedImage image = tipImage.getBufferedImage();
		image = ClipBoardImage.flipImageV(image);
		if (image == null) {
			return;
		}
		ClipBoardImage clipImage = new ClipBoardImage(image);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(clipImage, new ClipBoardThing());
		
		PamWarning w = new PamWarning("Sonar Panel", "Thumbnail copied to clipboard", 0);
		w.setEndOfLife(System.currentTimeMillis() + 2000);
		WarningSystem.getWarningSystem().addWarning(w);
	}
	
	private class ClipBoardThing implements ClipboardOwner {

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private FanDataImage getToolTipImage() {
		try {
			PamDataUnit hoverData = xyProjector.getHoveredDataUnit();
			if (hoverData == null) {
				return null;
			}
			if (hoverData instanceof RegionDataUnit == false) {
				return null;
			}
			RegionDataUnit regionDataUnit = (RegionDataUnit) hoverData;
			// get the frame and make it's full image. 
			TritechOffline tritechOffline = sonarsPanel.getTritechAcquisition().getTritechOffline();
			if (tritechOffline == null) {
				return null;
			}
			MultiFileCatalog geminiCatalog = tritechOffline.getMultiFileCatalog();

			//		System.out.printf("Find image records for time %s\n", PamCalendar.formatDateTime(valueMillis));
			GeminiImageRecordI imageRec = geminiCatalog.findRecordForTime(regionDataUnit.getSonarId(), regionDataUnit.getTimeMilliseconds());
			if (imageRec == null) {
				return null;
			}
			int usePix = sonarsPanel.getImagePixels(imageRec.getnBeam(), imageRec.getnRange(), getWidth());
			FanImageData fanData = imageFanMaker.createFanData(imageRec, usePix);
			SonarsPanelParams panelParams = sonarsPanel.getSonarsPanelParams();
			// now need to clip a part of that out around our bit. 
			DetectedRegion region = regionDataUnit.getRegion();
			double flip = panelParams.flipLeftRight ? -1 : -1;
			double maxS = Math.max(Math.sin(region.getMinBearing()), Math.sin(region.getMaxBearing()));
			double minS = Math.min(Math.sin(region.getMinBearing()), Math.sin(region.getMaxBearing()));
			double maxC = Math.max(Math.cos(region.getMinBearing()), Math.cos(region.getMaxBearing()));
			double minC = Math.min(Math.cos(region.getMinBearing()), Math.cos(region.getMaxBearing()));
			double xMin = minS * region.getMaxRange()*flip;
			double xMax = maxS * region.getMaxRange()*flip;
			double yMin = minC * region.getMinRange();
			double yMax = maxC * region.getMaxRange();
			double w = Math.max(Math.abs(xMax-xMin)/2,.5);
			double h = Math.max(Math.abs(yMax-yMin)/2,.5);
			FanDataImage tipImage = new FanDataImage(fanData, sonarsPanel.getColourMap(), true, panelParams.displayGain,
					Math.min(xMin,  xMax)-w, Math.max(xMin, xMax)+w, Math.min(yMin, yMax)-h, Math.max(yMin,  yMax)+h);
			return tipImage;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		return new SonarCoordinate(layoutInfo.getSonarId(), layoutInfo.getSonarId(), imagePos.x, imagePos.y);
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

		if (sonarsPanel.getSonarsPanelParams().flipLeftRight) {
			bearing = -bearing;
		}
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
			SonarCoordinate sonarCoordinate = new SonarCoordinate(layoutInfo.getSonarId(), layoutInfo.getSonarId(), zoomCentre.x, zoomCentre.y);
			sonarsPanel.zoomDisplay(sonarCoordinate, zoom);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (externalMouseHandler.mouseClicked(e)) {
				return;
			}
			TrackLinkDataUnit oldClickedOn = getClickedOnTrack();
			setClickedOnTrack(null);
			try {
				int dataUnitInd = xyProjector.findClosestDataUnitIndex(new Coordinate3d(e.getX(), e.getY(), 0));
				if (dataUnitInd >= 0) {
					HoverData hoverData = (HoverData) xyProjector.getHoverDataList().get(dataUnitInd);
					PamDataUnit dataUnit = hoverData.getDataUnit();
					setClickedOnTrack((TrackLinkDataUnit) dataUnit.getSuperDetection(TrackLinkDataUnit.class));
				}
			}
			catch (Exception exp) {

			}
			if (oldClickedOn != getClickedOnTrack()) {
				sonarsPanel.repaint();
				repaint();
			}
			//			sonarsPanel.get

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
		
		JMenuItem copyItem = new JMenuItem("Copy image");
		copyItem.setToolTipText("Copy image to clipboard");
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyImage();
			}
		});
		standardItems.add(copyItem);
		
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

	/**
	 * Copy the image, overlays and all, to the clipboard. 
	 */
	protected void copyImage() {
		ClipboardCopier cbc = new ClipboardCopier(sonarsPanel);
		cbc.copyToClipBoard();
	}

	private class SonarMarkObserver implements PamView.paneloverlay.overlaymark.OverlayMarkObserver {

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


	/**
	 * @return the clickedOnTrack
	 */
	public TrackLinkDataUnit getClickedOnTrack() {
		return sonarsPanel.getClickedOnTrack();
	}

	public void setClickedOnTrack(TrackLinkDataUnit tlDataUnit) {
		sonarsPanel.setClickedOnTrack(tlDataUnit);
	}

	/**
	 * Set the sonar layout. This includes setting the bounds rectangle for
	 * the panel within the larger panel, but is also used later to 
	 * set the vertex position and rotation of the sonar image. 
	 * @param layoutInfo
	 */
	public void setSonarLayout(LayoutInfo layoutInfo) {
		this.layoutInfo = layoutInfo;
		setBounds(layoutInfo.getImageRectangle());
	}
}
