package tritechplugins.echogram;

import java.util.Arrays;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamConstants;
import dataPlotsFX.data.DataTypeInfo;

public class EchogramDataBlock extends DataBlock2D<EchogramDataUnit>{

	private DataTypeInfo dataTypeInfo;
	
	private int[] dataWidths = new int[PamConstants.MAX_CHANNELS];

	private double maxDataRange = 50;

	private EchogramProcess echogramProcess;
	
	public EchogramDataBlock(EchogramProcess echogramProcess) {
		super(EchogramDataUnit.class, "Echogram Data", echogramProcess, 0);
		this.echogramProcess = echogramProcess;
		dataTypeInfo = new DataTypeInfo(ParameterType.RANGE, ParameterUnits.METERS);
	}

	@Override
	public int getHopSamples() {
		return 1;
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		if (sequenceNumber >= dataWidths.length || dataWidths[sequenceNumber] == 0) {
			return 1000;
		}
		else {
			return dataWidths[sequenceNumber];
		}
	}
	
	public void setDataWidth(int sequenceNumber, int dataWidth) {
		if (sequenceNumber >= dataWidths.length) {
			dataWidths = Arrays.copyOf(dataWidths, sequenceNumber+1);
		}
		dataWidths[sequenceNumber] = dataWidth;
	}

	@Override
	public double getMinDataValue() {
		return 0;
	}
	
	public void setMaxDataValue(double maxValue) {
		this.maxDataRange = maxValue;
	}

	@Override
	public double getMaxDataValue() {
		return maxDataRange ;
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return dataTypeInfo;
	}

	@Override
	public float getSampleRate() {
//		float fs = super.getSampleRate();
//		return fs*2;
		return echogramProcess.getFSforLoad();
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
//		System.out.println("Echogram sample rate set to " + sampleRate);
		super.setSampleRate(sampleRate, notify);
	}


}
