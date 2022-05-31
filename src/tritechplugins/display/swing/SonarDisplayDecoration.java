package tritechplugins.display.swing;

import javax.swing.JComponent;

public interface SonarDisplayDecoration {

	/**
	 * Get the swing component to add to the sonars outer display
	 * @return Swing component
	 */
	public JComponent getComponent();
	
	/**
	 * Destroy the decoration. This generally means unsubscribing 
	 * from any message updates. 
	 */
	public void destroyComponent();

}
