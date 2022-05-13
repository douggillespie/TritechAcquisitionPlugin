package tritechplugins.display.swing;

/**
 * Some options for different layouts of the sonar images on the display panel. 
 * @author dg50
 *
 */
public enum SonarsLayout {

	SEPARATE, SUMMED_ONECOL, MAXVAL, SUMMED_CHANNELCOLS;

	@Override
	public String toString() {
		switch (this) {
		case SEPARATE:
			return "Separate image for each sonar";
		case MAXVAL:
			return "Maximum value of all sonar images";
		case SUMMED_CHANNELCOLS:
			return "Sum (or mean) of all sonar images";
		case SUMMED_ONECOL:
			return "Combined image with RGB colours for each sonar";
		default:
			break;
		
		}
		return null;
	}
	
	
//	public static final int LAYOUT_SEPARATE = 0; // separate images
//	public static final int LAYOUT_SUMMED_ONECOL = 1; // one image which is mean of all images. 
//	public static final int LAYOUT_SUMMED_CHANNELCOLS = 2; // one image with a RGB per image (OK up to three !)

}
