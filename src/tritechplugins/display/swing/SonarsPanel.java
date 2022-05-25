package tritechplugins.display.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import Layout.PamAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import tritechgemini.detect.BackgroundSub;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.detect.BackgroundRemoval;

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
	
	private HashMap<Integer, BackgroundRemoval> backgroundSubtractors = new HashMap<>();
	
	private HashMap<Integer, Integer> imageIndexes = new HashMap<>();
	
	/*
	 * time taken to create images. 
	 */
	private long[] imageTime;

	private ImageFanMaker fanMaker = new FanPicksFromData(4);
	
	private SonarLayout sonarLayout = new AutoSonarLayout();
	
	private static final int NCOLOURS = 256;
	
	private ColourArray colourArray = ColourArray.createHotArray(NCOLOURS);

	private SettingsNameProvider nameProvider;
	
	private PamAxis pamAxis = new PamAxis(0, 1, 0, 1, 0, 2, false, "m", "%3.1f");

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
		sortRectangles();
	}
	
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
				backgroundSub.setTimeConstant(sonarsPanelParams.backgroundScaleFactor);
				imageRecord = backgroundSub.removeBackground(imageRecord, true);
			}
			prepareSonarImage(sonarIndex, imageRecord);
			if (imageRecord != null) {	
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
		imageFanData[sonarIndex] = fanMaker.createFanData(imageRecord);
		FanDataImage fanImage = new FanDataImage(imageFanData[sonarIndex], colourArray, false, sonarsPanelParams.displayGain);
		images[sonarIndex] = fanImage.getBufferedImage();
		long t2 = System.nanoTime();
		imageTime[sonarIndex] = t2-t1;
		repaint(10);
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

		String str = String.format("<html>Range %3.1fm, Angle %3.1f%s<br>xy (%3.1f, %3.1f)m</html>", 
				sonarCoord.getRange(), sonarCoord.getAngleDegrees(), LatLong.deg, sonarCoord.getX(), sonarCoord.getY());
		
		return str;
	}
	
	/**
	 * find the coordinates within a sonar image for the given display coordinates. 
	 * @param x x on display panel
	 * @param y y on display panel
	 * @return Sonar coordinate or null if x,y are not within an image. 
	 */
	public SonarCoordinate findSonarCoordinate(double x, double y) {// find where the mouse is in an image and show range-bearing data. 
		if (imageRectangles == null) {
			return null;
		}
		for (int i = 0; i < imageRectangles.length; i++) {
			if (imageRectangles[i] == null || currentImageRecords[i] == null) {
				continue;
			}
			Rectangle aspRect =  imageRectangles[i].getImageRectangle();// sonarLayout.checkAspect(imageRectangles[i], Math.toRadians(60));
			double maxR = currentImageRecords[i].getMaxRange();
			int pix = aspRect.height;
			int x0 = aspRect.x + aspRect.width/2;
			int y0 = aspRect.y + aspRect.height;
			double rPix = Math.sqrt(Math.pow(x-x0, 2) + Math.pow(y-y0, 2));
			if (rPix > pix) {
				continue;
			}
			double maxAng = Math.toRadians(60);
			if (currentImageRecords[i].getBearingTable() != null) {
				maxAng = Math.abs(currentImageRecords[i].getBearingTable()[0]);
				
			}
			double ang = Math.atan2(x-x0, y0-y);
			if (ang > maxAng || ang < -maxAng) {
				continue;
			}
			double xr = (x-x0)*maxR/aspRect.height;
			double yr = (y0-y)*maxR/aspRect.height;
			return new SonarCoordinate(currentImageRecords[i].getDeviceId(), xr, yr);
		}
		
		return null;		
	}

	private void paintSonarImage(Graphics g, int imageIndex, LayoutInfo layoutInfo, GeminiImageRecordI geminiImageRecord,
			BufferedImage bufferedImage) {
		if (layoutInfo == null || bufferedImage == null) {
			return;
		}
		Rectangle trueAsp = layoutInfo.getImageRectangle();//.checkAspect(layoutInfo, Math.toRadians(60));
		if (sonarsPanelParams.flipLeftRight) {
			g.drawImage(bufferedImage, trueAsp.x+trueAsp.width, trueAsp.height+trueAsp.y, trueAsp.x, trueAsp.y, 
					0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
		}
		else {
			g.drawImage(bufferedImage, trueAsp.x, trueAsp.height+trueAsp.y, trueAsp.x+trueAsp.width, trueAsp.y, 
					0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
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
		pamAxis.drawAxis(g, trueAsp.x+trueAsp.width/2, trueAsp.y+trueAsp.height, trueAsp.x+trueAsp.width, (int) y2);
		/*
		 * And grid, taking range coordinates from the axis. 
		 */
		Color col = colourArray.getContrastingColour();
		col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 90);
		g.setColor(col);
		if (sonarsPanelParams.showGrid) {
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
