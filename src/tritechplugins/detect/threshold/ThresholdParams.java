package tritechplugins.detect.threshold;

import java.io.Serializable;

public class ThresholdParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public String imageDataSource;
	
	public int highThreshold = 20;
	
	public int lowThreshold = 5;
	
	public int backgroundTimeConst = 20;
	
	public double backgroundScale = 1.5; // a reasonabl default, that used to be hard wired. 
	
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
