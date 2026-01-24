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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import tritechplugins.echogram.EchogramProcess;

public class EchogramPlotInfo extends Scrolling2DPlotInfo {

	private EchogramProcess echogramProcess;
	private TDScaleInfo echogramScaleInfo;
	private EchogramControlPane echogramControlPane;

	public EchogramPlotInfo(EchogramProcess echogramProcess,  EchogramPlotProviderFX tdDataProvider, 
			TDGraphFX tdGraph, DataBlock2D pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.echogramProcess = echogramProcess;
		echogramControlPane = new EchogramControlPane(this, tdGraph);
	}

	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return echogramControlPane;
	}

	@Override
	public PlotParams2D createPlotParams() {
		return new EchogramPlotParams();
	}

	@Override
	public TDScaleInfo createTDScaleInfo(Scrolling2DPlotInfo scrolingPlotinfo, double minVal, double maxVal) {
		echogramScaleInfo = new TDScaleInfo(0, 50, ParameterType.RANGE, ParameterUnits.METERS);
		echogramScaleInfo.setReverseAxis(true);
		return echogramScaleInfo;
	}
	@Override
	public Scrolling2DPlotDataFX makeScrolling2DPlotData(int iChannel) {
		return new EchogramPlotData(this, iChannel);
	}

	@Override
	public void bindPlotParams() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		// TODO Auto-generated method stub
		super.drawData(plotNumber, g, scrollStart, tdProjector);
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		// TODO Auto-generated method stub
		return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return super.getDataValue(pamDataUnit);
	}

}
