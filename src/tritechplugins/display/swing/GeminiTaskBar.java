package tritechplugins.display.swing;

import javax.swing.JComponent;

public interface GeminiTaskBar {
	
	/**
	 * Component, probably with a flow layout,to include in a display
	 * @return
	 */
	public JComponent getComponent();
	
	/**
	 * Called when we switch to a different task bar so it can unsubsribe or whatever is needed. 
	 */
	public void closeTaskBar();

}
