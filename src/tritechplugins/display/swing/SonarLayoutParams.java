package tritechplugins.display.swing;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;

public class SonarLayoutParams implements Cloneable, Serializable{

	public static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Rectangle2D.Double> rectangles = new HashMap<Integer, Rectangle2D.Double>();

	public SonarLayoutParams() {
	}
	
	/**
	 * Get a panel index, scaled relative to the main outer window, i.e. all dimensions should be 
	 * between 0 and 1. 
	 * @param panelIndex
	 * @return
	 */
	private Rectangle2D.Double getPanelRectangle(int panelIndex) {
		if (rectangles == null) {
			return null;
		}		
		return rectangles.get(panelIndex);
	}
	
	@Override
	protected SonarLayoutParams clone() {
		try {
			return (SonarLayoutParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Rectangle getPanelRectangle(int panelIndex, JComponent parent) {
		Rectangle r = new Rectangle();
		Rectangle2D.Double scale = getPanelRectangle(panelIndex);
		if (scale == null) {
			return null;
		}
		r.x = (int) (parent.getWidth()*scale.x);
		r.y = (int) (parent.getHeight()*scale.y);
		r.width = (int) (parent.getWidth()*scale.width);
		r.height = (int) (parent.getHeight()*scale.height);
		return r;
	}
	
	public void setPanelRectangle(int panelIndex, Rectangle rect, JComponent parent) {
		if (rect == null) {
			rectangles.remove(panelIndex);
			return;
		}
		Rectangle2D.Double r2d = new Rectangle2D.Double();

		r2d.x = (double) rect.x / (double) parent.getWidth() ;
		r2d.y =  (double) rect.y / (double) parent.getHeight();
		r2d.width = (double) rect.width / (double) parent.getWidth();
		r2d.height = (double) rect.height / (double) parent.getHeight();
		rectangles.put(panelIndex, r2d);
	}
	
	public void clear() {
		rectangles.clear();
	}

}
