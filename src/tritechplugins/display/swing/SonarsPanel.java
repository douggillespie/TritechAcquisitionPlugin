package tritechplugins.display.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PanelOverlayDraw;
import PamView.dialog.GenericSwingDialog;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
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
public class SonarsPanel extends PamPanel {

	private TritechAcquisition tritechAcquisition;

	private SonarsPanelParams sonarsPanelParams = new SonarsPanelParams();

	private ImageDataBlock imageDataBlock;

	private int numSonars = 0;

	private GeminiImageRecordI[] currentImageRecords;

	private FanImageData[] imageFanData;

	private BufferedImage[] images;

	private int numImages; // may not be the same as numSonars for some display options

	private LayoutInfo[] imageRectangles;

	private SonarXYProjector[] xyProjectors;

	private HashMap<Integer, BackgroundRemoval> backgroundSubtractors = new HashMap<>();

	private HashMap<Integer, Integer> imageIndexes = new HashMap<>();

	protected SonarOverlayManager sonarOverlayManager;

	/*
	 * time taken to create images. 
	 */
	private long[] imageTime;

	private ImageFanMaker[] fanMakers;

	private SonarLayout sonarLayout = new AutoSonarLayout();

	private static final int NCOLOURS = 256;

	private ColourArray colourArray = ColourArray.createHotArray(NCOLOURS);

	private SettingsNameProvider nameProvider;

	private PamAxis pamAxis = new PamAxis(0, 1, 0, 1, 0, 2, false, "m", "%3.1f");

	private MouseEvent mousePressPoint;

	private MouseEvent mouseDragPoint;

	private SonarPanelMouse sonarsPanelMouse; 
	
	private double zoomFactor = 1.0;
	
	private Coordinate3d zoomCentre = new Coordinate3d(0,0);

	// units of this are metres in the original sonar image. 
	private BufferedImage[] zoomedImages;
	
	private SonarZoomTransform[] sonarZoomTransforms;

	public SonarsPanel(TritechAcquisition tritechAcquisition, SettingsNameProvider nameProvider) {
		super();
		this.tritechAcquisition = tritechAcquisition;
		this.nameProvider = nameProvider;
		setLayout(new CornerLayout(new CornerLayoutContraint()));
		PamSettingManager.getInstance().registerSettings(new SettingsIO());
		this.imageDataBlock = tritechAcquisition.getImageDataBlock();
		setNumSonars(numSonars);
		updateColourMap(sonarsPanelParams.colourMap);
		setToolTipText("Sonar display panel");
		sonarOverlayManager = new SonarOverlayManager(this);
		sonarsPanelMouse = new SonarPanelMouse();
		this.addMouseListener(sonarsPanelMouse);
		this.addMouseMotionListener(sonarsPanelMouse);
		this.addMouseWheelListener(sonarsPanelMouse);
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getsonarsPanel() {
		return this;
	}

	public SonarsPanelParams getSonarsPanelParams() {
		return sonarsPanelParams;
	}


	public void setSonarsPanelParams(SonarsPanelParams sonarsPanelParams) {
		this.sonarsPanelParams = sonarsPanelParams;
	}

	public void setNumSonars(int numSonars) {
		this.numSonars = numSonars;
		currentImageRecords = new GeminiImageRecordI[numSonars];
		imageFanData = new FanImageData[numSonars];
		images = new BufferedImage[numSonars];
		imageTime = new long[numSonars];
		fanMakers = new ImageFanMaker[numSonars];
		zoomedImages = new BufferedImage[numSonars];
		sonarZoomTransforms = new SonarZoomTransform[numSonars];
		for (int i = 0; i < numSonars; i++) {
			fanMakers[i] = new FanPicksFromData(4);
		}
		sortRectangles();
	}

//	private void sortRectangles() {
//		numImages = numSonars;
//		switch (sonarsPanelParams.sonarsLayout) {
//		case MAXVAL:
//			numImages = 1;
//			break;
//		case SEPARATE:
//			break;
//		case SUMMED_CHANNELCOLS:
//			numImages = 1;
//			break;
//		case SUMMED_ONECOL:
//			numImages = 1;
//			break;
//		default:
//			break;
//		}
//		//		imageRectangles = new LayoutInfo[numImages];
//		if (numImages == 0) {
//			return;
//		}
//		if (xyProjectors == null || xyProjectors.length != numImages) {
//			xyProjectors = new SonarXYProjector[numImages];
//			for (int i = 0; i < numImages; i++) {
//				xyProjectors[i] = new SonarXYProjector(this, i, i);
//			}
//		}
//		Rectangle theseBounds = getBounds();
//		int border = theseBounds.width/50;
//		Graphics g = this.getGraphics();
//		if (g != null) {
//			int charWidth = g.getFontMetrics().getMaxAdvance();
//			int charHeight = g.getFontMetrics().getHeight();
//			theseBounds.x = charWidth; 
//			theseBounds.y = charHeight/2;
//			theseBounds.width -= 2*charWidth;
//			theseBounds.height -= 2*charHeight;
//		}
//		imageRectangles = sonarLayout.getRectangles(theseBounds, numImages, Math.toRadians(60));
//		//		if (numImages == 1) {
//		//			imageRectangles[0] = new Rectangle(panelWidth, panelHeight);
//		//		}
//		//		if (numImages == 2) {
//		//			imageRectangles[0] = new Rectangle(panelWidth/2, panelHeight);
//		//			imageRectangles[1] = new Rectangle(panelWidth/2, 0, panelWidth/2, panelHeight);
//		//		}
//	}

	private void sortRectangles() {
		numImages = numSonars;
		switch (sonarsPanelParams.sonarsLayout) {
		case MAXVAL:
			numImages = 1;
			break;
		case SEPARATE:
			break;
		case SUMMED_CHANNELCOLS:
			numImages = 1;
			break;
		case SUMMED_ONECOL:
			numImages = 1;
			break;
		default:
			break;
		}
		//		imageRectangles = new LayoutInfo[numImages];
		if (numImages == 0) {
			return;
		}
		if (xyProjectors == null || xyProjectors.length != numImages) {
			xyProjectors = new SonarXYProjector[numImages];
			for (int i = 0; i < numImages; i++) {
				xyProjectors[i] = new SonarXYProjector(this, i, i);
			}
		}
		Rectangle theseBounds = getBounds();
		int border = theseBounds.width/50;
		Graphics g = this.getGraphics();
		if (g != null) {
			int charWidth = g.getFontMetrics().getMaxAdvance();
			int charHeight = g.getFontMetrics().getHeight();
			theseBounds.x = charWidth; 
			theseBounds.y = charHeight/2;
			theseBounds.width -= 2*charWidth;
			theseBounds.height -= 2*charHeight;
		}
		imageRectangles = sonarLayout.getRectangles(theseBounds, numImages, Math.toRadians(60));
		//		if (numImages == 1) {
		//			imageRectangles[0] = new Rectangle(panelWidth, panelHeight);
		//		}
		//		if (numImages == 2) {
		//			imageRectangles[0] = new Rectangle(panelWidth/2, panelHeight);
		//			imageRectangles[1] = new Rectangle(panelWidth/2, 0, panelWidth/2, panelHeight);
		//		}
	}


	public void setImageRecord(int sonarIndex, GeminiImageRecordI imageRecord) {
		//		System.out.printf("New image record for id %d %s\n", sonarIndex, imageRecord);
		if (imageRecord != null) {
			sonarIndex = checkSonarIndex(imageRecord.getDeviceId());
		}
		if (sonarIndex < numSonars) {
			if (imageRecord != null && sonarsPanelParams.subtractBackground) {
				BackgroundRemoval backgroundSub = findBackgroundSub(imageRecord.getDeviceId());
				backgroundSub.setTimeConstant(sonarsPanelParams.backgroundTimeFactor);
				backgroundSub.setRemovalScale(sonarsPanelParams.backgroundScale);
				imageRecord = backgroundSub.removeBackground(imageRecord, true);
			}
			prepareSonarImage(sonarIndex, imageRecord);
			if (imageRecord != null && PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {	
				/*
				 * Only free data in viewer since in normal mode data can always be recovered. Probably 
				 *
				 */
				imageRecord.freeImageData();
			}
		}
	}

	/**
	 * Gets an index for each sonar, allowing for new ones coming online
	 * after start. Will update number of plots. 
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
	 * remake the images from fan data, e.g. after a colour map change. 
	 */
	public void remakeImages() {
		for (int i = 0; i < imageFanData.length; i++) {
			if (imageFanData != null) {
				FanDataImage fanImage = new FanDataImage(imageFanData[i], colourArray, false, sonarsPanelParams.displayGain);
				images[i] = fanImage.getBufferedImage();
			}
		}
		repaint();
	}

	private void prepareSonarImage(int sonarIndex, GeminiImageRecordI imageRecord) {
		currentImageRecords[sonarIndex] = imageRecord;
		if (imageRecord == null) {
			imageFanData[sonarIndex] = null;
			images[sonarIndex] = null;
			return;
		}
		long t1 = System.nanoTime();
		int nBearing = imageRecord.getnBeam();
		int nXPix = imageRectangles[sonarIndex].getImageRectangle().width;
		int usePix = getImagePixels(nBearing, nXPix);
		imageFanData[sonarIndex] = fanMakers[sonarIndex].createFanData(imageRecord, usePix);
		FanDataImage fanImage = new FanDataImage(imageFanData[sonarIndex], colourArray, false, sonarsPanelParams.displayGain);
		images[sonarIndex] = fanImage.getBufferedImage();
		long t2 = System.nanoTime();
		imageTime[sonarIndex] = t2-t1;
		repaint(10);
	}

	/**
	 * Get the number of image pixels based on the number of bearings in the image and the size on screen. 
	 * @param nBearing
	 * @param nXPix
	 * @return pixels width of image
	 */
	private int getImagePixels(int nBearing, int nXPix) {
		switch (sonarsPanelParams.resolution) {
		case SonarsPanelParams.RESOLUTION_DEFAULT:
			return nBearing;
		case SonarsPanelParams.RESOLUTION_HIGH:
			return Math.max(nBearing, nXPix/2);
		case SonarsPanelParams.RESOLUTION_BEST:
			return Math.max(nBearing, nXPix);
		}
		return nBearing;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (currentImageRecords == null || images == null || imageRectangles == null) {
			return;
		}
		sortRectangles();

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON); 
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		synchronized (this) {

		}
		for (int i = 0; i < numImages; i++) {
			paintSonarImage(g, i, imageRectangles[i], currentImageRecords[i], images[i]);
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		// find where the mouse is in an image and show range-bearing data. 
		SonarCoordinate sonarCoord = findSonarCoordinate(event.getX(), event.getY());
		if (sonarCoord == null) {
			return null;
		}

		// first see if we're on an overlay. 
		String overlayText = null;
		//		Coordinate3d c3d = new Coordinate3d(event.getX(), event.getY());
		if (xyProjectors != null) {
			for (int i = 0; i < xyProjectors.length; i++) {
				overlayText = xyProjectors[i].getHoverText(event.getPoint());
				if (overlayText != null) {
					break;
				}
			}
		}

		String str;

		if (overlayText != null) {
			str = overlayText;
		}
		else {
			str = "<html>";
		}

		str += String.format("Mouse ...<br>Range %3.1fm, Angle %3.1f%s<br>xy (%3.1f, %3.1f)m", 
				sonarCoord.getRange(), sonarCoord.getAngleDegrees(), LatLong.deg, sonarCoord.getX(), sonarCoord.getY());
		FanImageData fanData = imageFanData[sonarCoord.getSonarIndex()];
		// get the amplitude for the nearest xy pixel.
		int xPix = 0, yPix = 0;
		try {
			xPix = (int) Math.round(sonarCoord.getX() / fanData.getMetresPerPixX());
			yPix = (int) Math.round(sonarCoord.getY() / fanData.getMetresPerPixY());
			short[][] imValues = fanData.getImageValues();
			if (sonarsPanelParams.flipLeftRight) {
				xPix = -xPix;
			}
			xPix += imValues.length/2;
			//			yPix = imValues[0].length-yPix;
			int val = imValues[xPix][yPix];
			str += String.format("<br>Level %d", val);
		}
		catch (Exception e) {
			str += String.format("<p>Invalid pixel (%d,%d)", xPix, yPix);
		}
		str += "</html>";

		return str;
	}

	/**
	 * find the coordinates within a sonar image for the given display coordinates. 
	 * @param x x on display panel
	 * @param y y on display panel
	 * @return Sonar coordinate or null if x,y are not within an image. 
	 */
	public SonarCoordinate findSonarCoordinate(double x, double y) {// find where the mouse is in an image and show range-bearing data. 
//		if (imageRectangles == null) {
//			return null;
//		}
//		for (int i = 0; i < imageRectangles.length; i++) {
//			if (imageRectangles[i] == null || currentImageRecords[i] == null) {
//				continue;
//			}
//			Rectangle aspRect =  imageRectangles[i].getImageRectangle();// sonarLayout.checkAspect(imageRectangles[i], Math.toRadians(60));
//			double maxR = currentImageRecords[i].getMaxRange();
//			int pix = aspRect.height;
//			int x0 = aspRect.x + aspRect.width/2;
//			int y0 = aspRect.y + aspRect.height;
//			double rPix = Math.sqrt(Math.pow(x-x0, 2) + Math.pow(y-y0, 2));
//			if (rPix > pix) {
//				continue;
//			}
//			double maxAng = Math.toRadians(60);
//			if (currentImageRecords[i].getBearingTable() != null) {
//				maxAng = Math.abs(currentImageRecords[i].getBearingTable()[0]);
//
//			}
//			double ang = Math.atan2(x-x0, y0-y);
//			if (ang > maxAng || ang < -maxAng) {
//				continue;
//			}
//			double xr = (x-x0)*maxR/aspRect.height;
//			double yr = (y0-y)*maxR/aspRect.height;
//			return new SonarCoordinate(i, currentImageRecords[i].getDeviceId(), xr, yr);
//		}

		// work it out from the Zoom Transforms
		if (sonarZoomTransforms == null) {
			return null;
		}
		for (int i = 0; i < sonarZoomTransforms.length; i++) {
			if (sonarZoomTransforms[i] == null) {
				continue;
			}
			Coordinate3d metres = sonarZoomTransforms[i].screenToImageMetres(x, y);
			if (metres == null) {
				continue;
			}
			double maxAng = Math.abs(sonarZoomTransforms[i].getImageRecord().getBearingTable()[0]);
			double ang = Math.atan2(metres.x, metres.y);
			if (ang > maxAng || ang < -maxAng) {
				continue;
			}
			double r = Math.sqrt(Math.pow(metres.x, 2) + Math.pow(metres.y, 2));
			if (r > sonarZoomTransforms[i].getImageRecord().getMaxRange()) {
				continue;
			}
			return new SonarCoordinate(i, sonarZoomTransforms[i].getImageRecord().getDeviceId(), metres.x, metres.y);
		}
		return null;		
	}

	private void paintSonarImage(Graphics g, int imageIndex, LayoutInfo layoutInfo, GeminiImageRecordI geminiImageRecord,
			BufferedImage sonarImage) {
		if (layoutInfo == null || sonarImage == null) {
			return;
		}
		Rectangle trueAsp = layoutInfo.getImageRectangle();//.checkAspect(layoutInfo, Math.toRadians(60));

		sonarZoomTransforms[imageIndex] = new SonarZoomTransform(geminiImageRecord, trueAsp, sonarImage, 
				zoomFactor, zoomCentre, sonarsPanelParams.flipLeftRight);

		xyProjectors[imageIndex].clearHoverList();
		xyProjectors[imageIndex].setLayout(sonarZoomTransforms[imageIndex]);
		xyProjectors[imageIndex].setFlipImage(sonarsPanelParams.flipLeftRight);
		
		BufferedImage bufferedImage = sonarImage;
//		if (zoomFactor <= 1) {
//			bufferedImage = sonarImage;
//		}
//		else {
//			double imageScale = sonarImage.getHeight() / geminiImageRecord.getMaxRange();
//			bufferedImage = getZoomedImage(imageIndex, trueAsp, geminiImageRecord, sonarImage, imageScale);
//		}
		Rectangle imageClip = sonarZoomTransforms[imageIndex].getImageClipRect();
		if (sonarsPanelParams.flipLeftRight) {
			g.drawImage(bufferedImage, trueAsp.x+trueAsp.width, trueAsp.height+trueAsp.y, trueAsp.x, trueAsp.y, 
					imageClip.x, imageClip.y, imageClip.x+imageClip.width, imageClip.y+imageClip.height, null);
		}
		else {
			g.drawImage(bufferedImage, trueAsp.x, trueAsp.height+trueAsp.y, trueAsp.x+trueAsp.width, trueAsp.y, 
					imageClip.x, imageClip.y, imageClip.x+imageClip.width, imageClip.y+imageClip.height, null);
		}
		/*
		 * And the range ...
		 */
		double range = geminiImageRecord.getMaxRange();
		if (geminiImageRecord.getBearingTable() == null) {
			return;
		}
		double maxAng = Math.abs(geminiImageRecord.getBearingTable()[0]);
		pamAxis.setMaxVal(range);
		double y2 = trueAsp.y+trueAsp.height-trueAsp.height*Math.cos(maxAng);
		if (zoomFactor == 1) {
			pamAxis.drawAxis(g, trueAsp.x+trueAsp.width/2, trueAsp.y+trueAsp.height, trueAsp.x+trueAsp.width, (int) y2);
		}
		/*
		 * And grid, taking range coordinates from the axis. 
		 */
		Color col = colourArray.getContrastingColour();
		col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 90);
		g.setColor(col);
		if (sonarsPanelParams.showGrid && zoomFactor == 1) {
			ArrayList<Double> rangeVals = pamAxis.getAxisValues();
			int a1 = (int) Math.round(Math.toDegrees(Math.PI/2-maxAng));
			int a2 = (int) Math.round(Math.toDegrees(Math.PI/2+maxAng));
			int x0 = trueAsp.x + trueAsp.width/2;
			int y0 = trueAsp.y+trueAsp.height;
			for (int i = 0; i < rangeVals.size(); i++) {
				double r = trueAsp.getHeight() * rangeVals.get(i) / range;
				//				r = trueAsp.getHeight();
				double sr = r;//Math.sin(maxAng)*r;
				double cr = r;//Math.cos(maxAng)*r;
				int x1a = (int) Math.round(x0-sr);
				int x2a = (int) Math.round(x0+sr);
				int y1a = (int) Math.round(y0-cr);
				int y2a = (int) Math.round(y0+cr);
				g.drawArc(x1a, y1a, x2a-x1a, y2a-y1a, a1, a2-a1);
			}
			// and the angle lines grid.
			for (int i = 0; i < 5; i++) {
				double ang = maxAng-(i*maxAng/2);
				int x = (int) (x0 + Math.round(trueAsp.height*Math.sin(ang)));
				int y = (int) (y0 - Math.round(trueAsp.height*Math.cos(ang)));
				g.drawLine(x0,  y0, x, y);
			}
		}

		paintDetectorData(g, xyProjectors[imageIndex], geminiImageRecord);

		paintTextinformation(g, imageIndex, layoutInfo, geminiImageRecord);

		paintMouseDragLine(g, imageIndex);

	}

	/**
	 * Create a zoomed image which is a subset of the original sonarImage based
	 * on the zoom factor , zoom centre and desired image size. 
	 * @param imageIndex image index (can be used to recycle returned images
	 * @param geminiImageRecord 
	 * @param imagesize size of output image
	 * @param sonarImage original sonar image
	 * @param imageScale scale on original image in pixels per metre.  
	 * @return new buffered image to display in place of original. 
	 */
	private BufferedImage getZoomedImage(int imageIndex, Rectangle imageSize, 
			GeminiImageRecordI geminiImageRecord, BufferedImage sonarImage, double imageScale) {
//		if (zoomedImages[imageIndex] == null || zoomedImages[imageIndex] = )
		BufferedImage zoomedImage = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_4BYTE_ABGR);
		double secondScale = (double) zoomedImage.getWidth() / (double) sonarImage.getWidth();
		int ws = sonarImage.getWidth();
		int wd = zoomedImage.getWidth();
		int hs = sonarImage.getHeight();
		int hd = zoomedImage.getHeight();
		zoomCentre.x = 1;
		zoomCentre.y = 10;
		
		/**
		 * initial zooming only uses hs and ws, the sonar image sizes. 
		 * this can then be projected onto the larger image. 
		 */
		double x1 = ws/2*(1.-1./zoomFactor) + zoomCentre.x*imageScale;
		double x2 = ws/zoomFactor+x1;
		double y2 = hs-zoomCentre.y*imageScale;
		double y1 = y2-hs/zoomFactor;
		
//		x1 *= secondScale;
//		x2 *= secondScale;
//		y1 *= secondScale;
//		y2 *= secondScale;
		
		System.out.printf("Z %3.1f, ws %d, hs %d, x1 %3.1f, x2 %3.1f, y1 %3.1f, y2 %3.1f\n", 
				zoomFactor, ws, hs, x1,x2,y1,y2);
		if (x1 < 0) {
			x2 -= x1;
			x1 = 0;
			zoomCentre.x = x1;
		}
		if (x2 > ws) {
			double wid = x2-x1;
			x2 = ws-1;
			x1 = x2-wid;		
			zoomCentre.x = x1;	
		}
		if (y1 < 0) {
			y2 -= y1;
			y1 = 0;
		}
		if (y2 > hs) {
			double h = y2-y1;
			y2 = hs-1;
			y1 = y2-h;
		}
		Graphics g = zoomedImage.getGraphics();
		g.drawImage(sonarImage, 0, 0, wd, hd, (int) x1, (int) (hs-y2), (int) x2, (int) (hs-y1), null);
		// need to draw the axis onto this image or we'll never work out where they go. 
		if (sonarsPanelParams.showGrid && zoomFactor == 1) {
			Color col = colourArray.getContrastingColour();
			col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 200);
			g.setColor(col);
			
			int x0 = (int) (wd/2 -zoomCentre.x * imageScale * secondScale);
			int y0 = (int) (hd + (hs-y2) * secondScale);
			double maxAng = Math.abs(geminiImageRecord.getBearingTable()[0]);
			double len = hd*zoomFactor;
			
			
//				ArrayList<Double> rangeVals = pamAxis.getAxisValues();
//				int a1 = (int) Math.round(Math.toDegrees(Math.PI/2-maxAng));
//				int a2 = (int) Math.round(Math.toDegrees(Math.PI/2+maxAng));
//				int x0 = trueAsp.x + trueAsp.width/2;
//				int y0 = trueAsp.y+trueAsp.height;
//				for (int i = 0; i < rangeVals.size(); i++) {
//					double r = trueAsp.getHeight() * rangeVals.get(i) / range;
//					//				r = trueAsp.getHeight();
//					double sr = r;//Math.sin(maxAng)*r;
//					double cr = r;//Math.cos(maxAng)*r;
//					int x1a = (int) Math.round(x0-sr);
//					int x2a = (int) Math.round(x0+sr);
//					int y1a = (int) Math.round(y0-cr);
//					int y2a = (int) Math.round(y0+cr);
//					g.drawArc(x1a, y1a, x2a-x1a, y2a-y1a, a1, a2-a1);
//				}
				// and the angle lines grid.
				for (int i = 0; i < 5; i++) {
					double ang = maxAng-(i*maxAng/2);
					int x = (int) (x0 + Math.round(len*Math.sin(ang)));
					int y = (int) (y0 - Math.round(len*Math.cos(ang)));
					g.drawLine(x0,  hd-y0, x, hd-y);
					System.out.printf("Line %d x0 %d, y0 %d, x %d, y %d\n", i, x0,  y0, x, y);
				}
		}
		
		
//		g.setColor(Color.green);
//		g.drawLine(-1, -1, 1000, 1000);
		
		return zoomedImage;
	}

	private void paintMouseDragLine(Graphics g, int imageIndex) {
		// txt will be null if line is outside the image. 
		String txt = getDragText(imageIndex);
		if (txt == null) {
			return;
		}
		g.setColor(colourArray.getContrastingColour());
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		g.drawLine(mousePressPoint.getX(), mousePressPoint.getY(),  
				mouseDragPoint.getX(), mouseDragPoint.getY());
		if (txt != null) {
			g.drawString(txt, mouseDragPoint.getX(), mouseDragPoint.getY());
		}
	}
	
	/**
	 * Get text to show when the mouse is being dragged over an image. 
	 * @param imageIndex 
	 * @return drag text or null if not dragging or mouse out of image. 
	 */
	private String getDragText(int imageIndex) {
		if (mousePressPoint == null || mouseDragPoint == null) {
			return null;
		}
		SonarCoordinate downCoord = findSonarCoordinate(mousePressPoint.getX(), mousePressPoint.getY());
		SonarCoordinate dragCoord = findSonarCoordinate(mouseDragPoint.getX(), mouseDragPoint.getY());
		if (downCoord == null || dragCoord == null) {
			return null;
		}
		// the && means it will still show lne and text when crossing between images, with the value
		// being the difference between the position on each image - so can measure between what may
		// be the same target on different sonars. 
		if (downCoord.getSonarIndex() != imageIndex && dragCoord.getSonarIndex() != imageIndex) {
			return null;
		}
		double angle = Math.toDegrees(Math.atan2(dragCoord.getY()-downCoord.getY(), dragCoord.getX()-downCoord.getX()));
		double bearing = 90.-angle;
		bearing = PamUtils.constrainedAngle(bearing);
		double range = Math.sqrt(Math.pow(dragCoord.getY()-downCoord.getY(), 2) + Math.pow(dragCoord.getX()-downCoord.getX(),  2));
		String txt = String.format("Bearing %3.1f%s, Distance %3.1fm", bearing, LatLong.deg, range);
		return txt;
	}

	private void paintTextinformation(Graphics g, int imageIndex, LayoutInfo layoutInfo, GeminiImageRecordI geminiImageRecord) {
		/*
		 *  and draw text into the corner of the image. Will eventually mess with font sizes, but for
		 *  now, we're in get it going mode. 
		 */
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
		Point txtPoint = layoutInfo.getTextPoint();
		int xt = txtPoint.x;
		int yt = txtPoint.y;
		FontMetrics fm = g2d.getFontMetrics();
		int lineHeight = fm.getHeight();
		int maxCharWidth = fm.getMaxAdvance();
		// clear a rectangle (deals with the text being on top of the axis)
		Color currCol = g.getColor();
		g.setColor(this.getBackground());
		g.fillRect(xt, yt, maxCharWidth*2, lineHeight*4);
		g.setColor(currCol);
		yt += lineHeight;
		xt += fm.charWidth(' ');
		String str;
		str = PamCalendar.formatDBDateTime(geminiImageRecord.getRecordTime(), true);
		g2d.drawString(str, xt, yt);	
		yt += lineHeight;
		str = String.format("Sonar %d, record %d", geminiImageRecord.getDeviceId(), geminiImageRecord.getRecordNumber());
		g2d.drawString(str, xt, yt);	
		yt += lineHeight;
		str = String.format("nRange %d, nAngle %d", geminiImageRecord.getnRange(), geminiImageRecord.getnBeam());
		g2d.drawString(str, xt, yt);	
		yt += lineHeight;
		str = String.format("Load %3.2fms; Image %3.2fms", geminiImageRecord.getLoadTime()/1000000., imageTime[imageIndex]/1000000.);
		g2d.drawString(str, xt, yt);	
		yt += lineHeight;
		str = String.format("SoS %3.2fm/s", geminiImageRecord.getSoS());
		g2d.drawString(str, xt, yt);	
		str = getDragText(imageIndex);
		if (str != null) {
			yt += lineHeight;
			g2d.drawString(str, xt, yt);	
		}
	}

	private void paintDetectorData(Graphics g, SonarXYProjector sonarXYProjector,
			GeminiImageRecordI geminiImageRecord) {
		Collection<SonarOverlayData> selBlocks = sonarOverlayManager.getSelectedDataBlocks();
		for (SonarOverlayData selBlock : selBlocks) {
			paintDetectorData(g, selBlock, sonarXYProjector, geminiImageRecord);
		}
	}

	private void paintDetectorData(Graphics g, SonarOverlayData selBlock, SonarXYProjector sonarXYProjector,
			GeminiImageRecordI geminiImageRecord) {
		PamDataBlock dataBlock =  PamController.getInstance().getDataBlockByLongName(selBlock.dataName);
		if (dataBlock == null) {
			return;
		}
		PanelOverlayDraw overlayDraw = dataBlock.getOverlayDraw();
		if (overlayDraw == null) {
			return;
		}
		ArrayList<PamDataUnit> dataCopy = null;
		synchronized (dataBlock.getSynchLock()) {
			dataCopy = dataBlock.getDataCopy();
		}
		long tailEnd = geminiImageRecord.getRecordTime();
		long tailStart = 0;
		switch (sonarsPanelParams.tailOption) {
		case SonarsPanelParams.OVERLAY_TAIL_ALL:
			tailStart = 0;
			tailEnd = Long.MAX_VALUE;
			break;
		case SonarsPanelParams.OVERLAY_TAIL_NONE:
			tailStart = tailEnd;
			break;
		case SonarsPanelParams.OVERLAY_TAIL_TIME:
			tailStart = (long) (tailEnd - sonarsPanelParams.tailTime * 1000.);
			break;
		}
		for (PamDataUnit aUnit : dataCopy) {
			if (aUnit.getTimeMilliseconds() < tailStart || aUnit.getTimeMilliseconds() > tailEnd) {
				continue;
			}
			overlayDraw.drawDataUnit(g, aUnit, sonarXYProjector);
		}

	}
	
	/**
	 * work out a vaguely sensible range step. 
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

	private class SonarPanelMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			mousePressPoint = e;
			if (e.isPopupTrigger()) {
				mousePopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mousePressPoint = null;
			mouseDragPoint = null;
			if (e.isPopupTrigger()) {
				mousePopup(e);
			}
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseDragPoint = e;
			repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			zoomDisplay(e);
		}

	}

	private void mousePopup(MouseEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		int nOverlay = sonarOverlayManager.addSelectionMenuItems(popMenu, null, true, false, true);
		if (nOverlay == 0) {
			return;
		}
		JMenuItem menuItem = new JMenuItem("Overlay options");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				overlayOptions(e);
			}
		});
		popMenu.add(menuItem, 0);
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void overlayOptions(ActionEvent e) {
		boolean ans = GenericSwingDialog.showDialog(getFrame(1), "Overlay tail options", new OverlayTailDialogPanel(this));
		if (ans) {
			repaint();
		}
	}

	public void zoomDisplay(MouseWheelEvent e) {
		// get the zoom factor and current zoom coordinate, only zoom if on an image. 
		double zoom = Math.pow(1.05, -e.getPreciseWheelRotation());
		SonarCoordinate sonarPos = findSonarCoordinate(e.getX(), e.getY());
		if (sonarPos == null) {
			return;
		}
			
		
		zoomDisplay(sonarPos, zoom);
				
	}

	private void zoomDisplay(SonarCoordinate sonarPos, double zoom) {
		if (sonarPos == null) {
			return;
		}
		if (zoomCentre == null) {
			zoomCentre = new Coordinate3d(0, 0);
		}
		double flip = sonarsPanelParams.flipLeftRight ? -1 : 1;
		double mouseX = sonarPos.getX()*flip;
		double mouseY = sonarPos.getY();
		double dx = mouseX - zoomCentre.x;
		double dy = mouseY - zoomCentre.y;
		dx /= zoom;
		dy /= zoom;
		double newX = mouseX-dx;
		double newY = mouseY-dy;
		
		zoomCentre = new Coordinate3d(newX, newY);
		
		zoomFactor *= zoom;
		zoomFactor = Math.max(1.0,  zoomFactor);
		if (zoomFactor == 1.) {
			zoomCentre = new Coordinate3d(0, 0);
		}
//		System.out.printf("Zoom centre move from %3.1f,%3.1f to %3.1f,%3.1f\n",
//				sonarPos.getX(), sonarPos.getY(), zoomCentre.x, zoomCentre.y);
//		

//		System.out.println("Zoom by " + zoom);
		// get the mouse point. only zoom if it's on an image. 
		repaint();
	}

	private class SettingsIO implements PamSettings {

		@Override
		public String getUnitName() {
			if (nameProvider != null) {
				return nameProvider.getUnitName();
			}
			else {
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

}
