package tritechplugins.detect.threshold;

import java.io.Serializable;

public class ThresholdParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public String imageDataSource;
	
	public int highThreshold = 20;
	
	public int lowThreshold = 5;
	
	public int backgroundTimeConst = 20;
	
	public double backgroundScale = 1.5; // a reasonabl default, that used to be hard wired. 
	
	/**
	 * STD of data at each point is typically 1/3 to 1/2 of background level
	 * so subtracting off 1.5 mean is roughly mean + 1 or 2 STD. Backgrounds also 
	 * typically around 10 - 15 counts and STD's around 5, so mean*1.5+20 is more like
	 * mean+ 4or5 STD A sensible strategy might be to have a background of mean + n*STD + m
	 * where n and m are around 3 and 5.  
	 */
	public double backgroundSTDs = 0; // number of STD's of background at each point to also subtract off. 
	
	public int connectionType = 8; // 4 or 8 for connect 4 or connect 8. 
	
	public boolean filterRange;
	
	/**
	 * Minimum object (diagonal) size in metres
	 */
	public double minSize = 0.5;

	/**
	 * Maximum object (diagonal) size in metres
	 */
	public double maxSize = 4.0;
	
	/**
	 * time between writes of background data to binary files. 
	 */
	public int backgroundIntervalSecs = 60;

	public ThresholdParams() {
	}

}
