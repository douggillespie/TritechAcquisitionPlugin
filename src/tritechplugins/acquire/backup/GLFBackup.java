package tritechplugins.acquire.backup;

import PamController.PamController;
import PamController.SettingsNameProvider;
import backupmanager.FileLocation;
import backupmanager.stream.FileBackupStream;
import tritechplugins.acquire.TritechAcquisition;

public class GLFBackup extends FileBackupStream {

	private TritechAcquisition tritechDaq;

	public GLFBackup(TritechAcquisition tritechDaq) {
		super(tritechDaq, "Tritech GLF Files");
		this.tritechDaq = tritechDaq;
	}

	@Override
	public FileLocation getSourceLocation() {
		String fileLoc = tritechDaq.getDaqParams().getOfflineFileFolder();
		if (fileLoc == null) {
			return null;
		}
		FileLocation sl = new FileLocation();
		sl.path = fileLoc;
		sl.mask = null;
		sl.canEditMask = false;
		sl.canEditPath = false;
		return sl;
	}

	@Override
	public void setSourceLocation(FileLocation fileLocation) {
	}
	
	public long getMinBackupDelay() {
		if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
			return 0;
		}
		else {
			/*
			 * In principle this can be very short since the files are being 
			 * continuously written, so the modified time will always be 
			 * very recent. give it 10 mins though to be safe which is longer 
			 * than typical files. 
			 */
			return 600000;
//			return 3600000;
		}
	}
}
