package tritechplugins.acquire;

import javax.swing.Timer;

import geminisdk.Svs5Exception;
import geminisdk.structures.ChirpMode;
import geminisdk.structures.ConfigOnline;
import geminisdk.structures.GeminiRange;
import geminisdk.structures.RangeFrequencyConfig;

/**
 * Implementation of Tritech daq which uses only SVS5 functions. 
 * @author dg50
 *
 */
public class TritechJNADaqS extends TritechJNADaq {

	
	public TritechJNADaqS(TritechAcquisition tritechAcquisition, TritechDaqProcess tritechProcess) {
		super(tritechAcquisition, tritechProcess);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareProcess() {
		
		unprepareProcess();

		return prepareAcquisition();
	}
	
	@Override
	protected boolean prepareAllDevices(int[] sonars) {
		// TODO Auto-generated method stub
		return super.prepareAllDevices(sonars);
	}

	public boolean prepareDevice(int deviceId) {
		int err = 0;
//		if (1>0) return false;
		try {
			
			SonarDaqParams sonarParams = tritechAcquisition.getDaqParams().getSonarParams(deviceId);

			GeminiRange range = new GeminiRange(sonarParams.getRange());
			err = svs5Commands.setConfiguration(range, deviceId);
			//		err += svs5Commands.setConfiguration(range, 1);
//			System.out.println("setRange returned " + err);
			err = setRange(sonarParams.getRange(), deviceId);
			
			setGain(sonarParams.getGain(), deviceId);
			
			err = svs5Commands.setPingMode(true, (short) 0, deviceId);
//			svs5Commands.gemxSetPingMode(deviceId, 0);
//			svs5Commands.gemxAutoPingConfig(deviceId, sonarParams.getRange(), 
//					sonarParams.getGain(), (float) sonarParams.getFixedSoundSpeed());
//			System.out.println("setConfiguration pingMode returned " + err);
			
			err = svs5Commands.setSoSConfig(sonarParams.isUseFixedSoundSpeed(), sonarParams.getFixedSoundSpeed(), deviceId);
			
			ChirpMode chirpMode = new ChirpMode(sonarParams.getChirpMode());
			err = svs5Commands.setConfiguration(chirpMode, deviceId);
//			System.out.println("setConfiguration chirpMode returned " + err);

			err = svs5Commands.setHighResolution(sonarParams.isHighResolution(), deviceId);

			RangeFrequencyConfig rfConfig = new RangeFrequencyConfig();
			rfConfig.m_frequency = sonarParams.getRangeConfig();
			err = svs5Commands.setConfiguration(rfConfig);
//			System.out.println("setConfiguration returned " + err);
			//		
			//	
			////		SimulateADC simADC = new SimulateADC(true);
			////		err = svs5Commands.setConfiguration(simADC);
			////		System.out.println("Simulate returned " + err);
			//
			//		PingMode pingMode = new PingMode();
			//		pingMode.m_bFreeRun = false;
			//		pingMode.m_msInterval = 250;
			//		err += svs5Commands.setConfiguration(pingMode, 0);
			//		err = svs5Commands.setConfiguration(pingMode, 1);
			//		System.out.println("setConfiguration pingMode returned " + err);


			//		err = setFileLocation("C:\\GeminiData");
			//		String fileLoc = getFileLocation();
			//		System.out.printf("Gemini file location is \"%s\"\n", fileLoc);

			ConfigOnline cOnline = new ConfigOnline(sonarParams.isSetOnline());
			err = svs5Commands.setConfiguration(cOnline, deviceId);
			//		cOnline.value = false;
			//		err += svs5Commands.setConfiguration(cOnline, 0);
//			System.out.println("setOnline returned " + err);



		} catch (Svs5Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Error e) {
			System.out.println("Error calling SvS5 startup functions:" + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void unprepareProcess() {
		stop();
	}

}
