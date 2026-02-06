package tritechplugins.echogram.fx;


import dataPlotsFX.FXIconLoder;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.scrollingPlot2D.Plot2DControPane;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.sliders.PamRangeSlider;

/**
 * Based on the control panel for spectrograms, which also has two sliders. 
 * The amplitude one is OK but we'll be using the frequency slider for range. 
 */
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
		getFreqLabel().setText("Range (m)");
		setRangeRange(50);
//		getFrequencySlider()
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

	public void setRangeRange(double maxRange) {
		PamRangeSlider freqSlider = this.getFrequencySlider();
//		freqSlider.lowValueProperty().set(0);
//		freqSlider.highValueProperty().set(maxRange);
		freqSlider.setMin(0);
		freqSlider.setMax(maxRange);
		freqSlider.setLowValue(0);
		freqSlider.setHighValue(maxRange);
		
	}

	public void setMinRange(double doubleValue) {
		PamRangeSlider freqSlider = this.getFrequencySlider();
		freqSlider.setMin(doubleValue);
	}
	public void setMaxRange(double doubleValue) {
		PamRangeSlider freqSlider = this.getFrequencySlider();
		freqSlider.setMax(doubleValue);
	}

}
