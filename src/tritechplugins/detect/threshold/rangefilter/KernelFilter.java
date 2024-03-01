package tritechplugins.detect.threshold.rangefilter;

import Filters.Filter;

public class KernelFilter implements Filter {

	double[] history;
	double[] taps;
	int order;
	
	/**
	 * Currently the only available filter. Fits reasonably well
	 * with the size over range measurements of Meygen data where seals 
	 * mostly appear to only be a couple of pixels across in the range dimension. 
	 */
	static double[] defaultTaps3 = {.25, .5, .25};
	/**
	 * Slightly broader filter. Not currently used. 
	 */
	static double[] defaultTaps5 = {.1, .2, .4, .2, .1};
	
	public KernelFilter() {
		this(defaultTaps3);
	}
	
	public KernelFilter(double[] taps) {
		this.taps = taps;
		order = taps.length;
		history = new double[order+1];
	}

	@Override
	public void prepareFilter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void runFilter(double[] inputData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runFilter(double[] inputData, double[] outputData) {
		// TODO Auto-generated method stub

	}

	@Override
	public double runFilter(double aData) {
		double out = 0;
		int i = 0;
		history[order] = aData;
		for (; i < order; i++) {
			out += history[i]*taps[i];
			history[i] = history[i+1];
		}
		return out;
	}

	@Override
	public int getFilterDelay() {
		return (taps.length)/2;
	}

}
