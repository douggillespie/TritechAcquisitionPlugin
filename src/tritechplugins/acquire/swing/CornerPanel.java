package tritechplugins.acquire.swing;

import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

public class CornerPanel extends PamPanel {

	private Color backCol=PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA);
	
	public CornerPanel() {
		setCols();
	}


	public CornerPanel(LayoutManager layout) {
		super(layout);
		setCols();
	}

	private void setCols() {
		this.setOpaque(false);
		this.setBackground(backCol);		
	}

}
