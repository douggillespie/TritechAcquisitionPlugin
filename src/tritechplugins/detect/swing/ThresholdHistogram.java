package tritechplugins.detect.swing;

import java.util.Arrays;

/**
 * Faster integer histo class than PAMHistogram since everything is 
 * integer and no need to scale anything. 
 * @author dg50
 *
 */
public class ThresholdHistogram {
	
	public static final int MAX_VAL = 255;
	
	private int[] data;

	public ThresholdHistogram() {
		data = new int[MAX_VAL+1];
	}
	
	/**
	 * Add a data array to the histogram. 
	 * @param dataArray byte data array from sonar. 
	 */
	public void addData(byte[] dataArray) {
		for (int i = 0; i < dataArray.length; i++) {
			data[Byte.toUnsignedInt(dataArray[i])]++;
		}
	}
	
	/**
	 * @return the data
	 */
	public int[] getData() {
		return data;
	}

	/**
	 * Clear the data / set to zero. 
	 */
	public void clearData() {
		Arrays.fill(data, 0);
	}
	
	/**
	 * Downlscale the data by the given factor. 
	 * @param factor
	 */
	public void downScaleData(int factor) {
		for (int i = 0; i < data.length; i++) {
			data[i] /= factor;
		}
	}
	
	/**
	 * Get the current maximum bin value. 
	 * @return max bin value
	 */
	public int getMaxValue() {
		int max = 0;
		for (int i = 0; i < data.length; i++) {
			max = Math.max(max, data[i]);
		}
		return max;
	}
	
	/**
	 * Get the current minimum bin value. 
	 * @return min bin value
	 */
	public int getMinValue() {
		int min = 0;
		for (int i = 0; i < data.length; i++) {
			min = Math.max(min, data[i]);
		}
		return min;
	}

}
