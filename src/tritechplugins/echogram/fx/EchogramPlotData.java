package tritechplugins.echogram.fx;

import dataPlotsFX.spectrogramPlotFX.Spectrogram2DPlotData;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import tritechplugins.echogram.EchogramDataBlock;

public class EchogramPlotData extends Spectrogram2DPlotData {

	private EchogramPlotInfo echogramPlotInfo;

	public EchogramPlotData(EchogramPlotInfo echogramPlotInfo, int iChannel) {
		super(echogramPlotInfo, iChannel);
		this.echogramPlotInfo = echogramPlotInfo;
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

	@Override
	public double valueToBin(double value) {
		/*
		 * 
		double val = (double) dataWidth * (value-dataBlock2D.getMinDataValue()) / (dataBlock2D.getMaxDataValue()-dataBlock2D.getMinDataValue());
		return dataWidth-val;
		 */
//		EchogramDataBlock datablock = echogramPlotInfo.getEchogramDataBlock();
//		dataWidth = datablock.getDataWidth(getFftHop())
		return super.valueToBin(value);
	}

	@Override
	public double binToValue(double bin) {
		return super.binToValue(bin);
	}

}
