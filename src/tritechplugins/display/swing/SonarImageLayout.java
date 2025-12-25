package tritechplugins.display.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
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
	
	public static final int TOPBORDER = 0x1;
	public static final int BOTTOMBORDER = 0x2;
	public static final int LEFTBORDER = 0x4;
	public static final int RIGHTBORDER = 0x8;
	
	/**
	 * Angle used to calculate aspect ratio of layout. 
	 */
	private double maxAngleDegrees = 60;
	
	private SonarLayoutParams sonarLayoutParams = new SonarLayoutParams();
	
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
		PamSettingManager.getInstance().registerSettings(new LayoutSettings());
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
		
		// this should be the same as the number of panels!
		int[] sonars = sonarsPanel.getSonarIds();
		if (sonars == null) {
			sonars = new int[0];
		}
		SonarLayout sonarLayout = sonarsPanel.getSonarLayout();
		layoutInformation = sonarLayout.getRectangles(parent.getBounds(), components.length, Math.toRadians(maxAngleDegrees));

		/*
		 * Now see if manual layout has been set, and if it has, override the above layout
		 */
		for (int i = 0; i < layoutInformation.length; i++) {
			Rectangle newRect = sonarLayoutParams.getPanelRectangle(layoutInformation[i].getSonarId()
					, sonarsPanel);
			if (newRect != null) {
				layoutInformation[i].setImageRectangle(newRect);
			}
		}
		
		// then apply the layout
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
	
	public LayoutInfo findLayoutForSonar(int sonarId) {
		if (layoutInformation == null) {
			return null;
		}
		for (int i = 0; i < layoutInformation.length; i++) {
			if (layoutInformation[i].getSonarId() == sonarId) {
				return layoutInformation[i];
			}
		}
		return null;
	}

	public void dragSonarBorder(int panelIndex, Point startPt, Point endPt, int borderMouse) {
//		if (layoutInformation == null || layoutInformation.length <= panelIndex) {
//			return;
//		}
//		LayoutInfo layoutInf = layoutInformation[panelIndex];
		LayoutInfo layoutInf = findLayoutForSonar(panelIndex);
		if (layoutInf == null) {
			return;
		}
		Rectangle imageRect = layoutInf.getImageRectangle();
		Rectangle newRect = new Rectangle(imageRect);
		if ((borderMouse & TOPBORDER) != 0) {
			newRect.y = imageRect.y + (endPt.y - startPt.y);
			newRect.height = imageRect.height - (endPt.y - startPt.y);
		}
		if ((borderMouse & BOTTOMBORDER) != 0) {
			newRect.height = imageRect.height + (endPt.y - startPt.y);
		}
		if ((borderMouse & LEFTBORDER) != 0) {
			newRect.x = imageRect.x +  (endPt.x - startPt.x);
			newRect.width = imageRect.width -  (endPt.x - startPt.x);
		}
		if ((borderMouse & RIGHTBORDER) != 0) {
			newRect.width = imageRect.width +  (endPt.x - startPt.x);
		}
		newRect.y = Math.max(newRect.y, 0);
		newRect.y = Math.min(sonarsPanel.getHeight()-20, newRect.y);
		newRect.x = Math.max(newRect.x, 0);
		newRect.x = Math.min(sonarsPanel.getWidth()-20, newRect.x);
		newRect.height = Math.max(10, newRect.height);
		newRect.height = Math.min(sonarsPanel.getHeight()-newRect.y, newRect.height);
		newRect.width = Math.max(10, newRect.width);
		newRect.width = Math.min(sonarsPanel.getWidth()-newRect.x, newRect.width);
		
		sonarLayoutParams.setPanelRectangle(panelIndex, newRect, sonarsPanel);
//		invalidateParent();
	}
	
	public void showPopupMenu(MouseEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Restore auto layout");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				restoreAutomaticLayout();
			}
		});
		popMenu.add(menuItem);
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	public void restoreAutomaticLayout() {
		sonarLayoutParams.clear();
		invalidateParent();
	}
	
	private void invalidateParent() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
//				sonarsPanel.getImagesPanel().invalidate();
//				sonarsPanel.repaint();
//				sonarsPanel.getImagesPanel().repaint();
				sonarsPanel.invalidateLayout();
			}
		});
	}

	private class LayoutSettings implements PamSettings {

		@Override
		public String getUnitName() {
			return sonarsPanel.getNameProvider().getUnitName();
		}

		@Override
		public String getUnitType() {
			return "Sonar panel layout";
		}

		@Override
		public Serializable getSettingsReference() {
			return sonarLayoutParams;
		}

		@Override
		public long getSettingsVersion() {
			return SonarLayoutParams.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			sonarLayoutParams = (SonarLayoutParams) pamControlledUnitSettings.getSettings();
			sonarLayoutParams = sonarLayoutParams.clone();
			return true;
		}
		
	}
}
