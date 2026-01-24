package tritechplugins.echogram;

import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import tritechgemini.echogram.EchogramLine;

public class EchogramDataUnit extends DataUnit2D<PamDataUnit, SuperDetection> {

	private EchogramLine echogramLine;

	public EchogramDataUnit(EchogramLine echogramLine) {
		super(echogramLine.getGeminiRecord().getRecordTime());
		this.echogramLine = echogramLine;
	}

	/**
	 * @return the echogramLine
	 */
	public EchogramLine getEchogramLine() {
		return echogramLine;
	}

	@Override
	public double[] getMagnitudeData() {
		short[] dat = echogramLine.getData();
		double[] mag = new double[dat.length];
		for (int i = 0; i < dat.length; i++) {
			mag[i] = (double) dat[i] / 256.;
		}
		return mag;
	}


}
