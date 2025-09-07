package tritechplugins.display.swing.layouts;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import PamController.PamController;
import tritechplugins.acquire.TritechAcquisition;
import tritechplugins.display.swing.LayoutInfo;
import tritechplugins.display.swing.SonarsPanel;

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
//		tritechAcquisition.getTritechDaqProcess().getImageDataBlock().get
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
		
		sonarIds = Arrays.copyOf(sonarIds, nSonar);
		if (nSonar == 1) {
			Rectangle rect = checkImageAspect(bounds, maxAngle);
			Point pt = new Point(rect.x+rect.width*4/5, rect.y);
			LayoutInfo[] layoutInfo = {new LayoutInfo(sonarIds[0], rect, pt)};
			return layoutInfo;
		}
		int w = bounds.width;
		int h = bounds.height;
		if (w/nSonar/imageAspect > h) {
			// side by side layout
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int wr = w/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(sonarIds[i], checkImageAspect(new Rectangle(bounds.x+i*wr, bounds.y, wr, h), maxAngle));
			}
			return layout;
		}
		if (h*nSonar*imageAspect > w*2) {
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int hr = h/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(sonarIds[i], checkImageAspect(new Rectangle(bounds.x, bounds.y+i*hr, w, hr),maxAngle));
			}
			return layout;
		}
		if (nSonar == 2){
			LayoutInfo[] layouts = diagonal2(bounds, maxAngle);
//			if (layouts.length > 1) {
//				layouts[1].getImageRectangle().x -= 500;
//			}
			return layouts;
		}
		if (nSonar > 2){
			return manyGrid(bounds, nSonar, maxAngle);
		}
		
		return null;
	}


	protected LayoutInfo[] diagonal2(Rectangle bounds, double maxAngle) {
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
		rects[0] = checkImageAspect(new Rectangle(bounds.x, bounds.y+bounds.height*(h1-h2)/h1, w, h), maxAngle);
		Point pt1;
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			pt1 = new Point(rects[0].x, rects[0].y);
		}
		else {
			pt1 = new Point(rects[0].x+rects[0].width*3/4, rects[0].y+rects[0].height-rects[0].height/4);
		}
		rects[1] = checkImageAspect(new Rectangle(bounds.x+bounds.width*(w1-w2)/w1, bounds.y, w, h), maxAngle);
		
		LayoutInfo[] layouts = new LayoutInfo[2];
		layouts[0] = new LayoutInfo(sonarIds[0], rects[0], pt1);
		layouts[1] = new LayoutInfo(sonarIds[1], rects[1]);
		return layouts;
	}

	protected LayoutInfo[] manyGrid(Rectangle bounds, int nSonar, double maxAngle) {
		LayoutInfo layouts[] = new LayoutInfo[nSonar];
		int nx = 2;
		int ny = (nSonar+1)/2;
		int w = bounds.width/nx;
		int h = bounds.height/ny;
		int x = 0, y = 0;
		int[] sonarIds = getSonarIds();
		sonarIds = Arrays.copyOf(sonarIds, nSonar);
		for (int i = 0; i < nSonar; i++) {
			layouts[i] = new LayoutInfo(sonarIds[i], checkImageAspect(new Rectangle(x, y, w, h), maxAngle));
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
