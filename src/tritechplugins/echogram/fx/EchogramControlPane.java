package tritechplugins.echogram.fx;

import dataPlotsFX.FXIconLoder;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.scrollingPlot2D.Plot2DControPane;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

public class EchogramControlPane extends Plot2DControPane implements TDSettingsPane {

	private EchogramPlotInfo echogramPlotInto;
	private Canvas icon;

	/**
	 * @param plotInfo2D
	 * @param tdGraph
	 */
	public EchogramControlPane(EchogramPlotInfo echogramPlotInto, TDGraphFX tdGraph) {
		super(echogramPlotInto, tdGraph);
		this.echogramPlotInto = echogramPlotInto;
		icon = FXIconLoder.createIcon("Resources/BeamformIcon20.png", 20, 20);
	}

	@Override
	public Node getHidingIcon() {
		// TODO Auto-generated method stub
		return icon;
	}

	@Override
	public String getShowingName() {
		return "Echogram";
	}

	@Override
	public Node getShowingIcon() {
		// TODO Auto-generated method stub
		return icon;
	}

	@Override
	public Pane getPane() {
		return this;
	}
	

}
