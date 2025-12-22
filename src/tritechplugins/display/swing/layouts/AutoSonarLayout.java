package tritechplugins.display.swing.layouts;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import PamController.PamController;
import tritechplugins.acquire.SonarPosition;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.display.swing.LayoutInfo;
import tritechplugins.display.swing.SonarsPanel;
import tritechplugins.display.swing.SonarsPanelParams;

public class AutoSonarLayout extends SonarLayout {

	private SonarsPanel sonarsPanel;
	private TritechAcquisition tritechAcquisition;

	public AutoSonarLayout(TritechAcquisition tritechAcquisition, SonarsPanel sonarsPanel) {
		this.tritechAcquisition = tritechAcquisition;
		this.sonarsPanel = sonarsPanel;
	}

	/**
	 * Get current sonar id's. 
	 * @return
	 */
	public int[] getSonarIds() {
		return sonarsPanel.getSonarIds();
	}
	
	@Override
	public LayoutInfo[] getRectangles(Rectangle bounds, int nSonar, double maxAngle) {
		// calculate the ratio of width to height for each rectangle. 
		double rotation = Math.PI/2;
		double imageAspect = getImageAspect(maxAngle); 
		double windowAspect = getWindowAspect(maxAngle, rotation);
		int[] sonarIds = getSonarIds();
		imageIndexes.clear();
		for (int i = 0; i < sonarIds.length; i++) {
			imageIndexes.put(sonarIds[i], i);
		}
//		int nSonar = sonars.length;
		
		sonarIds = Arrays.copyOf(sonarIds, nSonar);
		ImageAspect[] aspects = new ImageAspect[nSonar];
		double maxAspect = 0;
		for (int i = 0; i < nSonar; i++) {
			aspects[i] = getRotatedAspect(sonarIds[i], maxAngle);
			maxAspect = Math.max(maxAspect, aspects[i].getAspectRatio());
		}
		
		
		if (nSonar == 1) {
//			Rectangle rect = checkImageAspect(bounds, maxAngle);
			Rectangle rect = bounds;
			Point pt = new Point(rect.x+rect.width*4/5, rect.y);
			LayoutInfo[] layoutInfo = {new LayoutInfo(sonarIds[0], aspects[0], rect, pt)};
			return layoutInfo;
		}
		int w = bounds.width;
		int h = bounds.height;
		if (w/nSonar/maxAspect > h) {
			// side by side layout
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int wr = w/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(sonarIds[i], aspects[i], checkImageAspect(new Rectangle(bounds.x+i*wr, bounds.y, wr, h), aspects[0]));
			}
			return layout;
		}
		if (h*nSonar*imageAspect > w*2) {
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int hr = h/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(sonarIds[i], aspects[i], checkImageAspect(new Rectangle(bounds.x, bounds.y+i*hr, w, hr), aspects[i]));
			}
			return layout;
		}
		if (nSonar == 2){
			LayoutInfo[] layouts = diagonal2(bounds, aspects);
//			if (layouts.length > 1) {
//				layouts[1].getImageRectangle().x -= 500;
//			}
			return layouts;
		}
		if (nSonar > 2){
			return manyGrid(bounds, nSonar, aspects);
		}
		
		return null;
	}


	/**
	 * Work out aspect data for a sonar image that's been rotated. 
	 * @param i
	 * @param maxAngle 
	 * @return
	 */
	private ImageAspect getRotatedAspect(int sonarId, double maxAngle) {
		SonarsPanelParams params = sonarsPanel.getSonarsPanelParams();
		boolean rotate = params.isUseSonarRotation();
		SonarPosition sonarPos = tritechAcquisition.getDaqParams().getSonarPosition(sonarId);
		if (rotate == false || sonarPos.getHead() == 0) {
			return new ImageAspect(0, getImageAspect(maxAngle), 1);
		}
		else {
			double xMax = 0, xMin = 0;
			double yMax = 0, yMin = 0;
			maxAngle = Math.abs(maxAngle);
			double a = -maxAngle;
			while (a <= maxAngle) {
				double ra = Math.toRadians(a+sonarPos.getHead());
				xMax = Math.max(xMax, Math.abs(Math.sin(ra)));
				xMin = Math.min(xMax, Math.abs(Math.sin(ra)));
				yMax = Math.max(yMax, Math.abs(Math.cos(ra)));
				yMin = Math.max(yMin, Math.abs(Math.cos(ra)));
				a += 1;
			}
			return new ImageAspect(sonarPos.getHead(), xMax-xMin, yMax-yMin);
		}
		
	}

	protected LayoutInfo[] diagonal2(Rectangle bounds, ImageAspect[] aspects) {
		/*
		 * 
			drawSonarImage(g2d, 0, fanData[0], bufferedImage[0], 0, getHeight()*1/3, getWidth()*5/9, getHeight());
			drawSonarImage(g2d, 1, fanData[1], bufferedImage[1], getWidth()*4/9, 0, getWidth(), getHeight()*2/3);
		 */
		Rectangle[] rects = new Rectangle[2];
		int w1 = 9;
		int w2 = 5;
		int h1 = 11;
		int h2 = 8;
		int w = bounds.width*w2/w1;
		int h = bounds.height*h2/h1;
		int[] sonarIds = getSonarIds();
		sonarIds = Arrays.copyOf(sonarIds, 2);
//		for (int i = 0; i < 2; i++) {
			rects[0] = checkImageAspect(new Rectangle(bounds.x, bounds.y+bounds.height*(h1-h2)/h1, w, h), aspects[0]);
//		}
		Point pt1;
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			pt1 = new Point(rects[0].x, rects[0].y);
		}
		else {
			pt1 = new Point(rects[0].x+rects[0].width*3/4, rects[0].y+rects[0].height-rects[0].height/4);
		}
		rects[1] = checkImageAspect(new Rectangle(bounds.x+bounds.width*(w1-w2)/w1, bounds.y, w, h), aspects[1]);
		
		LayoutInfo[] layouts = new LayoutInfo[2];
		layouts[0] = new LayoutInfo(sonarIds[0], aspects[0], rects[0], pt1);
		layouts[1] = new LayoutInfo(sonarIds[1], aspects[1], rects[1]);
		return layouts;
	}

	protected LayoutInfo[] manyGrid(Rectangle bounds, double maxAngle, ImageAspect[] aspects) {
		int nSonar = aspects.length;
		LayoutInfo layouts[] = new LayoutInfo[nSonar];
		int nx = 2;
		int ny = (nSonar+1)/2;
		int w = bounds.width/nx;
		int h = bounds.height/ny;
		int x = 0, y = 0;
		int[] sonarIds = getSonarIds();
		sonarIds = Arrays.copyOf(sonarIds, nSonar);
		for (int i = 0; i < nSonar; i++) {
			layouts[i] = new LayoutInfo(sonarIds[i], aspects[i], checkImageAspect(new Rectangle(x, y, w, h), aspects[i]));
			if (i/nx < nx-1) {
				x += w;
			}
			else {
				x = 0;
				y += h;
			}
		}
		return layouts;
	}

	@Override
	public String getName() {
		return "Automatic";
	}
}
