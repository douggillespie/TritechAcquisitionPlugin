package tritechplugins.display.swing;

import java.awt.Rectangle;

public class AutoSonarLayout extends SonarLayout {

	public AutoSonarLayout() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle[] getRectangles(Rectangle bounds, int nSonar, double maxAngle) {
		// calculate the ratio of width to height for each rectangle. 
		double aspect = getAspect(maxAngle); 
		if (nSonar == 1) {
			Rectangle rects[] = {bounds};
			return rects;
		}
		int w = bounds.width;
		int h = bounds.height;
		if (w/nSonar/aspect > h) {
			// side by side layout
			Rectangle[] rects = new Rectangle[nSonar];
			int wr = w/nSonar;
			for (int i = 0; i < nSonar; i++) {
				rects[i] = new Rectangle(bounds.x+i*wr, bounds.y, wr, h);
			}
			return rects;
		}
		if (h*nSonar*aspect > w*2) {
			Rectangle[] rects = new Rectangle[nSonar];
			int hr = h/nSonar;
			for (int i = 0; i < nSonar; i++) {
				rects[i] = new Rectangle(bounds.x, bounds.y+i*hr, w, hr);
			}
			return rects;
		}
		if (nSonar == 2){
			return diagonal2(bounds, aspect);
		}
		if (nSonar > 2){
			return manyGrid(bounds, nSonar, aspect);
		}
		
		return null;
	}


	private Rectangle[] diagonal2(Rectangle bounds, double aspect) {
		/*
		 * 
			drawSonarImage(g2d, 0, fanData[0], bufferedImage[0], 0, getHeight()*1/3, getWidth()*5/9, getHeight());
			drawSonarImage(g2d, 1, fanData[1], bufferedImage[1], getWidth()*4/9, 0, getWidth(), getHeight()*2/3);
		 */
		Rectangle[] rects = new Rectangle[2];
		int w = bounds.width*5/9;
		int h = bounds.height*2/3;
		rects[0] = new Rectangle(bounds.x, bounds.y+bounds.height*1/3, w, h);
		rects[1] = new Rectangle(bounds.x+bounds.width*4/9, bounds.y, w, h);
		
		return rects;
	}

	private Rectangle[] manyGrid(Rectangle bounds, int nSonar, double aspect) {
		Rectangle rects[] = new Rectangle[nSonar];
		int nx = 2;
		int ny = (nSonar+1)/2;
		int w = bounds.width/nx;
		int h = bounds.height/ny;
		int x = 0, y = 0;
		for (int i = 0; i < nSonar; i++) {
			rects[i] = new Rectangle(x, y, w, h);
			if (i/nx < nx-1) {
				x += w;
			}
			else {
				x = 0;
				y += h;
			}
		}
		return rects;
	}
}
