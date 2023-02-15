package tritechplugins.detect.threshold.background;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import PamguardMVC.background.BackgroundBinaryWriter;
import PamguardMVC.background.BackgroundDataUnit;
import PamguardMVC.background.BackgroundManager;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryTypes;
import tritechgemini.fileio.GLFFileCatalog;
import tritechgemini.fileio.LittleEndianDataOutputStream;
import tritechgemini.imagedata.GLFImageRecord;

/**
 * Class to write frames of background data from the threshold detector. 
 * These will follow pretty much the standard format for a Gemini data frame 
 * or may have a somewhat reduced metadata sample ? 
 * @author dg50
 *
 */
public class ThresholdBackgroundWriter extends BackgroundBinaryWriter {
	
	private GLFFileCatalog glfFileCatalog;

	public ThresholdBackgroundWriter(BackgroundManager backgroundManager) {
		super(backgroundManager);
		glfFileCatalog = new GLFFileCatalog(null);
	}

	@Override
	public BinaryObjectData packBackgroundData(BackgroundDataUnit backgroundUnit) {
		ThresholdBackgroundDataUnit tbdu = (ThresholdBackgroundDataUnit) backgroundUnit;
		GLFImageRecord record = tbdu.getBackgroundImage();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(bos);
		try {
			record.genericHeader.write(dos);
			glfFileCatalog.writeGLFReecord(record, dos);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		BinaryObjectData bod = new BinaryObjectData(BinaryTypes.BACKGROUND_DATA, bos.toByteArray());
		return bod;
	}

	@Override
	public BackgroundDataUnit unpackBackgroundData(BinaryObjectData binaryObjectData, BinaryHeader bh,
			int moduleVersion) {
		// TODO Auto-generated method stub
		return null;
	}

}
