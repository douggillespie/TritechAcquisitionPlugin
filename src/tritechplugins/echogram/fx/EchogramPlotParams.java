package tritechplugins.echogram.fx;

import dataPlotsFX.scrollingPlot2D.PlotParams2D;

public class EchogramPlotParams extends PlotParams2D {

	private static final long serialVersionUID = 1L;

	public EchogramPlotParams() {
		maxAmplitudeLimits[0] = 0;
		maxAmplitudeLimits[1] = 255;
		amplitudeLimitsSerial[0] = 0;
		amplitudeLimitsSerial[1] = 255;
		createAmplitudeProperty();
	}

}
