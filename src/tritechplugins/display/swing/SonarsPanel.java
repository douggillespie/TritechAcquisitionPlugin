package tritechplugins.display.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import tritechgemini.imagedata.FanImageData;
import tritechgemini.imagedata.FanPicksFromData;
import tritechgemini.imagedata.GeminiImageRecordI;
import tritechgemini.imagedata.ImageFanMaker;
import tritechplugins.acquire.ImageDataBlock;
import tritechplugins.acquire.TritechAcquisition;

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
	
	private int numSonars = 1;
	
	private GeminiImageRecordI[] currentImageRecords;
	
	private FanImageData[] imageFanData;
	
	private BufferedImage[] images;
	
	private int numImages; // may not be the same as numSonars for some display options
	
	private Rectangle[] imageRectangles;

	private ImageFanMaker fanMaker = new FanPicksFromData(4);
	
	private SonarLayout sonarLayout = new AutoSonarLayout();

	public SonarsPanel(TritechAcquisition tritechAcquisition) {
		super();
		this.tritechAcquisition = tritechAcquisition;
		setLayout(new CornerLayout(new CornerLayoutContraint()));
		this.imageDataBlock = tritechAcquisition.getImageDataBlock();
		setNumSonars(numSonars);
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getsonarsPanel() {
		return this;
	}
	
	public void setNumSonars(int numSonars) {
		this.numSonars = numSonars;
		currentImageRecords = new GeminiImageRecordI[numSonars];
		imageFanData = new FanImageData[numSonars];
		images = new BufferedImage[numSonars];
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
		imageRectangles = new Rectangle[numImages];
		if (numImages == 0) {
			return;
		}
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		imageRectangles = sonarLayout.getRectangles(getBounds(), numImages, Math.toRadians(60));
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
		if (sonarIndex < numSonars) {
			prepareSonarImage(sonarIndex, imageRecord);
			
		}
	}

	private void prepareSonarImage(int sonarIndex, GeminiImageRecordI imageRecord) {
		currentImageRecords[sonarIndex] = imageRecord;
		if (imageRecord == null) {
			imageFanData[sonarIndex] = null;
			images[sonarIndex] = null;
			return;
		}
		imageFanData[sonarIndex] = fanMaker.createFanData(imageRecord);
		FanDataImage fanImage = new FanDataImage(imageFanData[sonarIndex], null, false);
		images[sonarIndex] = fanImage.getBufferedImage();
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
			paintSonarImage(g, imageRectangles[i], currentImageRecords[i], images[i]);
		}
	}

	private void paintSonarImage(Graphics g, Rectangle rectangle, GeminiImageRecordI geminiImageRecord,
			BufferedImage bufferedImage) {
		if (rectangle == null || bufferedImage == null) {
			return;
		}
		Rectangle trueAsp = sonarLayout.checkAspect(rectangle, Math.toRadians(60));
		g.drawImage(bufferedImage, trueAsp.x, trueAsp.height+trueAsp.y, trueAsp.x+trueAsp.width, trueAsp.y, 
				0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
		/*
		 *  and draw text into the corner of the image. Will eventually mess with font sizes, but for
		 *  now, we're in get it going mode. 
		 */
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
		int xt = trueAsp.x;
		int yt = trueAsp.y;
		FontMetrics fm = g2d.getFontMetrics();
		int lineHeight = fm.getHeight();
		yt += lineHeight;
		xt += fm.charWidth(' ');
		String str;
		str = PamCalendar.formatDBDateTime(geminiImageRecord.getRecordTime(), true);
		g2d.drawString(str, xt, yt);	
		yt += lineHeight;
		str = String.format("Sonar %d, record %d", geminiImageRecord.getDeviceId(), geminiImageRecord.getRecordNumber());
		g2d.drawString(str, xt, yt);	
	}

}
