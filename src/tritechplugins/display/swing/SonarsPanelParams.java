package tritechplugins.display.swing;

import java.io.Serializable;

import PamView.ColourArray.ColourArrayType;

/**
 * Parameters controlling layout of the sonars panel. 
 * @author dg50
 *
 */
public class SonarsPanelParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public SonarsLayout sonarsLayout = SonarsLayout.SEPARATE;
	
	public int amplitudeMin = 0;
	
	public int amplitudeMAx = 255;
	
	public ColourArrayType colourMap = ColourArrayType.HOT;

	public int displayGain = 1;
	
	public boolean showGrid;

	public boolean flipLeftRight;
	
	public boolean subtractBackground = false;
	
	public int backgroundScaleFactor = 50;
	
}
