package tritechplugins.display.swing;

import java.awt.Point;
import java.awt.Rectangle;

import PamController.PamController;

public class AutoSonarLayout extends SonarLayout {

	public AutoSonarLayout() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public LayoutInfo[] getRectangles(Rectangle bounds, int nSonar, double maxAngle) {
		// calculate the ratio of width to height for each rectangle. 
		double aspect = getAspect(maxAngle); 
		if (nSonar == 1) {
			Rectangle rect = checkAspect(bounds, maxAngle);
			Point pt = new Point(rect.x+rect.width*4/5, rect.y);
			LayoutInfo[] layoutInfo = {new LayoutInfo(rect, pt)};
			return layoutInfo;
		}
		int w = bounds.width;
		int h = bounds.height;
		if (w/nSonar/aspect > h) {
			// side by side layout
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int wr = w/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(checkAspect(new Rectangle(bounds.x+i*wr, bounds.y, wr, h), maxAngle));
			}
			return layout;
		}
		if (h*nSonar*aspect > w*2) {
			LayoutInfo[] layout = new LayoutInfo[nSonar];
			int hr = h/nSonar;
			for (int i = 0; i < nSonar; i++) {
				layout[i] = new LayoutInfo(checkAspect(new Rectangle(bounds.x, bounds.y+i*hr, w, hr),maxAngle));
			}
			return layout;
		}
		if (nSonar == 2){
			return diagonal2(bounds, maxAngle);
		}
		if (nSonar > 2){
			return manyGrid(bounds, nSonar, maxAngle);
		}
		
		return null;
	}


	private LayoutInfo[] diagonal2(Rectangle bounds, double maxAngle) {
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
		rects[0] = checkAspect(new Rectangle(bounds.x, bounds.y+bounds.height*(h1-h2)/h1, w, h), maxAngle);
		Point pt1;
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			pt1 = new Point(rects[0].x, rects[0].y);
		}
		else {
			pt1 = new Point(rects[0].x+rects[0].width*3/4, rects[0].y+rects[0].height-rects[0].height/4);
		}
		rects[1] = checkAspect(new Rectangle(bounds.x+bounds.width*(w1-w2)/w1, bounds.y, w, h), maxAngle);
		
		LayoutInfo[] layouts = new LayoutInfo[2];
		layouts[0] = new LayoutInfo(rects[0], pt1);
		layouts[1] = new LayoutInfo(rects[1]);
		return layouts;
	}

	private LayoutInfo[] manyGrid(Rectangle bounds, int nSonar, double maxAngle) {
		LayoutInfo layouts[] = new LayoutInfo[nSonar];
		int nx = 2;
		int ny = (nSonar+1)/2;
		int w = bounds.width/nx;
		int h = bounds.height/ny;
		int x = 0, y = 0;
		for (int i = 0; i < nSonar; i++) {
			layouts[i] = new LayoutInfo(checkAspect(new Rectangle(x, y, w, h), maxAngle));
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
}
