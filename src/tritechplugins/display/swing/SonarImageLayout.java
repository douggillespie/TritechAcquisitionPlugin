package tritechplugins.display.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.HashMap;

import tritechplugins.display.swing.layouts.AutoSonarLayout;
import tritechplugins.display.swing.layouts.SonarLayout;

/**
 * This is a Swing layout manager that arranges multiple transparent 
 * SonarImagePanel components within the ImagesPanel. The layout manager
 * handles the relative positions of the multiple panels which are
 * set in a Swing call to layoutContainer. At the same
 * time, the SonarLayout will set which sonar
 * is assigned to each panel and any other rotations and anchor points
 * within each SonarImagePanel. 
 * @author dg50
 *
 */
public class SonarImageLayout implements LayoutManager {
	
	private LayoutInfo[] layoutInformation;

	private SonarsPanel sonarsPanel;
	
	/**
	 * Angle used to calculate aspect ratio of layout. 
	 */
	private double maxAngleDegrees = 60;
	
	
	/**
	 * @return the maxAngleDegrees
	 */
	public double getMaxAngleDegrees() {
		return maxAngleDegrees;
	}

	/**
	 * Set the max angle and return true if it's changes
	 * in which case we'll need to layout. 
	 * @param maxAngleDegrees the maxAngleDegrees to set
	 */
	public boolean setMaxAngleDegrees(double maxAngleDegrees) {
		maxAngleDegrees = 60;
		boolean isNew = this.maxAngleDegrees != maxAngleDegrees;
		this.maxAngleDegrees = maxAngleDegrees;
		return isNew;
	}

	public SonarImageLayout(SonarsPanel sonarsPanel) {
		this.sonarsPanel = sonarsPanel;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub

	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void layoutContainer(Container parent) {
		Component[] components = parent.getComponents();
		if (components == null | components.length == 0) {
			return;
		}
		SonarLayout sonarLayout = sonarsPanel.getSonarLayout();
		layoutInformation = sonarLayout.getRectangles(parent.getBounds(), components.length, Math.toRadians(maxAngleDegrees));
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof SonarImagePanel) {
				SonarImagePanel imagePanel = (SonarImagePanel) components[i];
				if (i == 1) {
					layoutInformation[i].setRotationDegrees(0);
					
				}
				imagePanel.setSonarLayout(layoutInformation[i]);
			}
			components[i].setBounds(layoutInformation[i].getImageRectangle());
		}
	}

	/**
	 * Get the layout information which was last used to layout 
	 * a particular panel. null if that panel doesn't exist. 
	 * @param imageIndex
	 * @return
	 */
	public LayoutInfo getLayoutInfo(int imageIndex) {
		if (layoutInformation == null || layoutInformation.length <= imageIndex) {
			return null;
		}
		return layoutInformation[imageIndex];
	}

}
