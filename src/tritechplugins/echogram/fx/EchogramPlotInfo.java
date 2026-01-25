package tritechplugins.echogram.fx;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import pamViewFX.fxNodes.sliders.PamRangeSlider;
import tritechplugins.echogram.EchogramProcess;

public class EchogramPlotInfo extends Scrolling2DPlotInfo {

	private EchogramProcess echogramProcess;
	private TDScaleInfo echogramScaleInfo; // the range scale. 
	private EchogramControlPane echogramControlPane;
	private volatile boolean stopLoop;

	public EchogramPlotInfo(EchogramProcess echogramProcess,  EchogramPlotProviderFX tdDataProvider, 
			TDGraphFX tdGraph, DataBlock2D pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.echogramProcess = echogramProcess;
	}

	@Override
	public TDSettingsPane getGraphSettingsPane() {
		if (echogramControlPane == null) {
			echogramControlPane = new EchogramControlPane(this, getTDGraph());
		}
		return echogramControlPane;
	}

	@Override
	public PlotParams2D createPlotParams() {
		return new EchogramPlotParams();
	}

	@Override
	public TDScaleInfo createTDScaleInfo(Scrolling2DPlotInfo scrolingPlotinfo, double minVal, double maxVal) {
		echogramScaleInfo = new TDScaleInfo(0, 50, ParameterType.RANGE, ParameterUnits.METERS);
		echogramScaleInfo.setReverseAxis(false);
		return echogramScaleInfo;
	}
	@Override
	public Scrolling2DPlotDataFX makeScrolling2DPlotData(int iChannel) {
		return new EchogramPlotData(this, iChannel);
	}

	@Override
	public void bindPlotParams() {
		if (echogramScaleInfo == null) {
			return;
		}

		echogramScaleInfo.minValProperty().addListener((obsVal, oldVal, newVal)->{
			if (echogramControlPane != null && stopLoop == false) {
				stopLoop=true; 
				echogramControlPane.setMinRange(newVal.doubleValue());
				stopLoop=false; 
			}
		});
		echogramScaleInfo.maxValProperty().addListener((obsVal, oldVal, newVal)->{
			if (echogramControlPane != null && stopLoop == false) {
				stopLoop=true; 
				echogramControlPane.setMaxRange(newVal.doubleValue());
				stopLoop=false; 
			}
		});
		if (echogramControlPane != null) {
			
			echogramControlPane.setBindsAndListeners();
			
			PamRangeSlider freqSlider = echogramControlPane.getFrequencySlider();
			freqSlider.highValueProperty().addListener((obsVal, oldVal, newVal)->{
				if (stopLoop == false) {
					stopLoop=true; 
					echogramScaleInfo.setMaxVal(newVal.doubleValue());
					stopLoop=false; 
				}
			});
			freqSlider.lowValueProperty().addListener((obsVal, oldVal, newVal)->{
				if (stopLoop == false) {
					stopLoop=true; 
					echogramScaleInfo.setMinVal(newVal.doubleValue());
					stopLoop=false; 
				}
			});
			
//			ampSlider = echogramControlPane.get
		}
	}

	@Override
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		super.drawData(plotNumber, g, scrollStart, tdProjector);
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		return super.getDataValue(pamDataUnit);
	}

}
