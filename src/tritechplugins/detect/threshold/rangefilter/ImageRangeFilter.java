package tritechplugins.detect.threshold.rangefilter;

import Filters.Filter;
import Filters.NullFilter;

public class ImageRangeFilter {
	
	private int nThread = 3;

	public ImageRangeFilter() {
		
	}
	
	public byte[] filterImage(byte[] inputData, int nBeam, int nRange) {
		byte[] outputData = new byte[nRange*nBeam];
		Thread[] threads = new Thread[nThread];
		for (int t = 0; t < nThread; t++) {
			int start = t;
			threads[t] = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int b = start; b < nBeam; b+= nThread) {
						filterBeam(inputData, outputData, nBeam, nRange, b);
					}
				}
			});
			threads[t].start();
		}
		for (int t = 0; t < nThread; t++) {
			try {
				threads[t].join();
			} catch (InterruptedException e) {
			}
		}
		
		
		
		return outputData;
	}
	
	protected void filterBeam(byte[] inputData, byte[] outputData, int nBeam, int nRange, int b) {
		// setup loop with p the 'pointer'
		// into the input and outputdata. 
		int nData = nRange*nBeam;
		Filter filter = filterMaker();
		double in;
		int out;
		for (int p = b; p < nData; p+=nBeam) {
			in = Byte.toUnsignedInt(inputData[p]);
			out = (int) filter.runFilter(in);
			outputData[p] = (byte) (out & 0xFF);
		}
	}

	/**
	 * Generate a filter. Several will be needed since several beams
	 * will be processed simultaneously in different threads. 
	 * @return a valid filter function (or null for no filter).
	 */
	private Filter filterMaker() {
		return new KernelFilter();
	}

}
