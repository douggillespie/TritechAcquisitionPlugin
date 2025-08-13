package tritechplugins.acquire;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class SonarStatusDataBlock extends PamDataBlock<SonarStatusDataUnit> {

	private TritechDaqProcess tritechDaqProcess;

	public SonarStatusDataBlock(TritechDaqProcess tritechDaqProcess) {
		super(SonarStatusDataUnit.class, "Sonar Status", tritechDaqProcess, 0);
		this.tritechDaqProcess = tritechDaqProcess;
	}


}
