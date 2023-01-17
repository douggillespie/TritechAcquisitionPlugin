package tritechplugins.display.swing;

import java.util.LinkedList;

import PamView.ColourArray;
import tritechgemini.imagedata.FanImageData;

/**
 * Persistent fan image is a 
 * @author dg50
 *
 */
public class PersistentFanImageMaker {

	private int persistentFrames;
	
	private LinkedList<FanImageData> fanImageHistory;

	public PersistentFanImageMaker() {
		fanImageHistory = new LinkedList<>();
	}
	
	public FanImageData makePersistentImage(FanImageData fanImageData, int persistentFrames, boolean rescale) {
		if (fanImageData == null || persistentFrames < 2) {
			return fanImageData;
		}
		FanImageData persImage = fanImageData.clone();
		short[][] data = persImage.getImageValues();
		int nBear = data.length;
		int nRange = data[0].length;
		while (fanImageHistory.size() >= persistentFrames) {
			fanImageHistory.remove(0);
		}
		int n = 1;
		for (FanImageData oldImage : fanImageHistory) {
			short[][] oldData = oldImage.getImageValues();
			int nB = Math.min(nBear, oldData.length);
			int nR = Math.min(nRange, oldData[0].length);
			for (int iB = 0; iB < nB; iB++) {
				for (int iR = 0; iR < nR; iR++) {
					data[iB][iR] += oldData[iB][iR];
				}
			}
			n++;
		}
		// consider scaling down ? 
		if (rescale) {
			// scale down by number of frames
			for (int iB = 0; iB < nBear; iB++) {
				for (int iR = 0; iR < nRange; iR++) {
					data[iB][iR] /= n;
				}
			}
		}
		else {
			// otherwise just check nothing is saturated. 
			for (int iB = 0; iB < nBear; iB++) {
				for (int iR = 0; iR < nRange; iR++) {
					data[iB][iR] = (short) Math.min(data[iB][iR], 255);
				}
			}
		}
		
		
		fanImageHistory.add(fanImageData);
		return persImage;
		
	}

	
}
