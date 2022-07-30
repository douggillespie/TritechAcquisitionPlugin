package tritechplugins.display.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class SonarImageLayout implements LayoutManager {

	private SonarLayout sonarLayout = new AutoSonarLayout();
	
	public SonarImageLayout() {
		// TODO Auto-generated constructor stub
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
		LayoutInfo[] rectangles = sonarLayout.getRectangles(parent.getBounds(), components.length, Math.toRadians(60));
		for (int i = 0; i < components.length; i++) {
			components[i].setBounds(rectangles[i].getImageRectangle());
		}
	}

}
