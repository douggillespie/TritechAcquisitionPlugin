package tritechplugins.echogram.fx;

import dataPlotsFX.spectrogramPlotFX.Spectrogram2DPlotData;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class EchogramPlotData extends Spectrogram2DPlotData {

	public EchogramPlotData(EchogramPlotInfo echogramPlotInfo, int iChannel) {
		super(echogramPlotInfo, iChannel);
	}

	@Override
	public synchronized void drawSpectrogramScroll(GraphicsContext g2d, double timePixels, double freqPixels,
			PamAxisFX timeAxis, double scrollStart, double scrollEndTime, double imageFP1, double imageFP2,
			double[] freqBinRange, double freqWidth) {
		super.drawSpectrogramScroll(g2d, timePixels, freqPixels, timeAxis, scrollStart, scrollEndTime, imageFP1, imageFP2,
				freqBinRange, freqWidth);
	}

	@Override
	public void drawSpectrogram(GraphicsContext g2d, Rectangle windowRect, Orientation orientation, PamAxisFX timeAxis,
			double scrollStart, boolean wrap) {
		super.drawSpectrogram(g2d, windowRect, orientation, timeAxis, scrollStart, wrap);
	}

}
