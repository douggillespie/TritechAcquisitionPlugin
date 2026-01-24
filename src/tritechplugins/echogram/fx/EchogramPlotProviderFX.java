package tritechplugins.echogram.fx;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import tritechplugins.echogram.EchogramDataBlock;
import tritechplugins.echogram.EchogramProcess;

public class EchogramPlotProviderFX extends TDDataProviderFX {
	
	private EchogramProcess echogramProcess;
	private EchogramDataBlock echogramDataBlock;

	public EchogramPlotProviderFX(EchogramProcess echogramProcess, EchogramDataBlock echogramDataBlock) {
		super(echogramDataBlock);
		this.echogramProcess = echogramProcess;
		this.echogramDataBlock = echogramDataBlock;
	}

	@Override
	public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
		return new EchogramPlotInfo(echogramProcess, this, tdGraph, echogramDataBlock);
	}


}
