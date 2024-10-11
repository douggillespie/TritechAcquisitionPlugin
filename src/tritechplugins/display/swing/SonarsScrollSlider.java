package tritechplugins.display.swing;

import java.awt.event.MouseWheelEvent;

import pamScrollSystem.PamScrollSlider;

public class SonarsScrollSlider extends PamScrollSlider {

	private SonarsOuterPanel sonarsOuterPanel;

	public SonarsScrollSlider(SonarsOuterPanel sonarsOuterPanel, String name, int orientation, int stepSizeMillis, long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		this.sonarsOuterPanel = sonarsOuterPanel;
	}
	
	@Override
	public void doMouseWheelAction(MouseWheelEvent mouseWheelEvent) {
		int n = mouseWheelEvent.getWheelRotation();
		sonarsOuterPanel.scrollByFrames(n);
		scrollMoved();
	}

}
