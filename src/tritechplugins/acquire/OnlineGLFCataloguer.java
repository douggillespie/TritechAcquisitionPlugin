package tritechplugins.acquire;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import PamUtils.PamCalendar;
import tritechgemini.fileio.CatalogException;
import tritechgemini.fileio.GLFFileCatalog;
import warnings.PamWarning;
import warnings.WarningSystem;

public class OnlineGLFCataloguer extends SwingWorker<Integer, String> {

	private TritechAcquisition tritechAcquisition;
	
	private File glfFile;
	
	private static int currentJobs = 0;
	
	private PamWarning catWarning;
	
	public OnlineGLFCataloguer(TritechAcquisition tritechAcquisition, File glfFile) {
		super();
		this.tritechAcquisition = tritechAcquisition;
		this.glfFile = glfFile;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		currentJobs ++;
		try {
			buildFileCatalogue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		currentJobs --;
		return 0;
	}

	private void buildFileCatalogue() {
		if (glfFile.exists() == false) {
			publish("Cannot catalogue a file that doesn't exit " + glfFile);
			return;
		}
/*
 *  wait until the file is a few seconds old.
 *  This is needed since the file will almost immediatley exit, but 
 *  the Genesis side will still be compressing and writing to the file
 *  so we need to wait until the file hasn't modified for several seconds
 *  but only wait a maximum of about 30 seconds. 
 */
		long now = System.currentTimeMillis();
		while (System.currentTimeMillis() - now < 30000) {
			// 10 s max
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			if (System.currentTimeMillis()-glfFile.lastModified() > 2000) {
				break;
			}
		}
		long finalCheck = System.currentTimeMillis()-glfFile.lastModified();
		if (finalCheck < 2000) {
			String msg = String.format("File %s is still being written after %ds give up", glfFile.getName(), finalCheck/1000);
			publish (msg);
			return;
		}
//		long created = glfFile.
		String msg = String.format("Catalogue file %s after %ds wait", glfFile.getName(), (System.currentTimeMillis()-now)/1000);
		publish(msg);
		try {
			GLFFileCatalog.getFileCatalog(glfFile.getAbsolutePath(), true);
		} catch (CatalogException e) {
			e.printStackTrace();
			publish("Catalogue file error in " + glfFile.getAbsolutePath());
		}
	}

	@Override
	protected void process(List<String> chunks) {
		for (String str : chunks) {
			System.out.println(str);
			if (catWarning == null) {
				catWarning = new PamWarning("GLF Catlogue", str, 1);
			}
			else {
				catWarning.setWarningMessage(str);
			}
			catWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 40000);
			WarningSystem.getWarningSystem().addWarning(catWarning);
		}
	}

	@Override
	protected void done() {
		if (catWarning != null) {
//			WarningSystem.getWarningSystem().removeWarning(catWarning);
		}
	}

	/**
	 * @return the currentJobs
	 */
	public static int getCurrentJobs() {
		return currentJobs;
	}

}
