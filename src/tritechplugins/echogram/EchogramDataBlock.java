package tritechplugins.echogram;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import dataPlotsFX.data.DataTypeInfo;

public class EchogramDataBlock extends DataBlock2D<EchogramDataUnit>{

	private DataTypeInfo dataTypeInfo;
	public EchogramDataBlock(EchogramProcess echogramProcess) {
		super(EchogramDataUnit.class, "Echogram Data", echogramProcess, 0);
		dataTypeInfo = new DataTypeInfo(ParameterType.RANGE, ParameterUnits.METERS);
	}

	@Override
	public int getHopSamples() {
		return 1;
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		return 1000;
	}

	@Override
	public double getMinDataValue() {
		return 0;
	}

	@Override
	public double getMaxDataValue() {
		return 30;
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return dataTypeInfo;
	}


}
