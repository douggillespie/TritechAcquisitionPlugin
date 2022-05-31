package tritechplugins.display.swing;

import javax.swing.JComponent;
import javax.swing.JMenu;

/**
 * Sonar add ons which can be associated with a TritechDaqSystem
 * @author dg50
 *
 */
public abstract class SonarDisplayDecorations {

	/**
	 * @return Bar of controls to go in the top of the sonars panel.
	 */
	public SonarDisplayDecoration getTopBar() {
		return null;
	}

	/**
	 * @return Rectangle of controls to go in the NW corner
	 */
	public SonarDisplayDecoration getNorthWestInset() {
		return null;
	}

	/**
	 * @return Rectangle of controls to go in the NE corner
	 */
	public SonarDisplayDecoration getNorthEastInset() {
		return null;
	}

	/**
	 * @return Rectangle of controls to go in the SW corner
	 */
	public  SonarDisplayDecoration getSouthWestInset() {
		return null;
	}

	/**
	 * @return Rectangle of controls to go in the SE corner
	 */
	public SonarDisplayDecoration getSouthEastInset() {
		return null;
	}
	
	/**
	 * Add menu items to an existing menu. 
	 * @param menu existing menu
	 * @return number of items added. 
	 */
	public int addMenuItems(JMenu menu) {
		return 0;
	}

}
