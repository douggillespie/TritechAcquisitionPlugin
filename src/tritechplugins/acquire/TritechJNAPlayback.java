package tritechplugins.acquire;

import fileOfflineData.OfflineFileList;
import geminisdk.Svs5Exception;
import geminisdk.structures.GeminiStructure;
import tritechplugins.acquire.offline.TritechFileFilter;
import tritechplugins.acquire.swing.DaqControlPanel;
import tritechplugins.acquire.swing.SonarsStatusPanel;
import tritechplugins.display.swing.SonarDisplayDecoration;
import tritechplugins.display.swing.SonarDisplayDecorations;

/**
 * PAMGuard normal mode reanalysis of file data using the JNA interfac to svs5. 
 * This does seem a bit slow to initialise if given a big file list, so may not
 * want to use long term
 * @author dg50
 *
 */
public class TritechJNAPlayback extends Svs5JNADaqSystem {

	private JNAPlaybackDecorations swingDecorations;

	public TritechJNAPlayback(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		prepareProcess();
	}

	@Override
	public boolean prepareProcess() {
		if (gSerialiser == null) {
			return false;
		}
		geminiCallback.setPlaybackMode(true);
		// make a list of files from the set folder, and pass to svs4
		int ans = setOnline(false, 0);
		return ans == 0;
//		return false;
	}

	@Override
	public boolean start() {
		TritechDaqParams params = tritechAcquisition.getDaqParams();
		OfflineFileList fileList = new OfflineFileList(params.getOfflineFileFolder(), new TritechFileFilter(), params.isOfflineSubFolders());
		String[] allFiles = fileList.asStringList();
		int ans = gSerialiser.setInputFileList(allFiles, allFiles.length);
//		System.out.println("Set list of files returned " + ans);
//		setOnline(false, 0);
		return ans == 0;
	}


	@Override
	public boolean stop() {
		try {
			return svs5Commands.setBoolCommand(GeminiStructure.SVS5_CONFIG_PLAY_STOP, true, 0) == 0;
		} catch (Svs5Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isRealTime() {
		return false;
	}

	@Override
	public void unprepareProcess() {
		stop();
	}

	@Override
	public SonarDisplayDecorations getSwingDecorations() {
		if (swingDecorations == null) {
			swingDecorations = new JNAPlaybackDecorations();
		}
		return swingDecorations;
	}

	private class JNAPlaybackDecorations extends SonarDisplayDecorations {

		@Override
		public SonarDisplayDecoration getNorthWestInset() {
			return new SonarsStatusPanel(tritechAcquisition, TritechJNAPlayback.this);
		}

	}

	@Override
	protected void newSonar(SonarStatusData sonarData) {
		// TODO Auto-generated method stub
		
	}
}
