package tritechplugins.detect.veto;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.veto.swing.VetoDialogPanel;

/**
 * Manager of spatial vetoes. Will have to 
 * somehow keep a list of which vetoes are in use for 
 * an application and be able to recreate them at startup.
 * Bad news, of course, is that PAMGuard may want to run > 
 * one detector on the sonar data, so it will have to be
 * associated with a detector instance. 
 * @author dg50
 *
 */
public class SpatialVetoManager implements PamSettings {
	
	private ArrayList<SpatialVetoProvider> vetoProviders;
	
	private ArrayList<SpatialVeto> currentVetos;
	
	private AllVetoParams allVetoParams = new AllVetoParams();

	private ThresholdDetector thresholdDetector;

	public SpatialVetoManager(ThresholdDetector thresholdDetector) {
		this.thresholdDetector = thresholdDetector;
		vetoProviders = new ArrayList<>();
		currentVetos = new ArrayList<>();
		vetoProviders.add(new RThiVetoProvider());
		
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * 
	 * @return List of available veto providers
	 */
	public ArrayList<SpatialVetoProvider> getVetoProviders() {
		return vetoProviders;
	}

	/**
	 * @return the currentVetos
	 */
	public ArrayList<SpatialVeto> getCurrentVetos() {
		return currentVetos;
	}
	
	public VetoDialogPanel getDialogPanel(Window owner) {
		VetoDialogPanel dialogPanel = new VetoDialogPanel(owner, this);
		return dialogPanel;
	}

	public SpatialVeto addVeto(SpatialVetoProvider provider) {
		SpatialVeto newVeto = provider.createVeto();
		currentVetos.add(newVeto);
		return newVeto;
	}
	
	public void addVeto(SpatialVeto veto) {
		currentVetos.add(veto);
	}

	public void removeVeto(SpatialVeto veto) {
		currentVetos.remove(veto);
		
	}

	@Override
	public String getUnitName() {
		return thresholdDetector.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Sonar Spatial Vetos";
	}

	@Override
	public Serializable getSettingsReference() {
		// fill the list with the latest settings for each veto
		allVetoParams.allParams.clear();
		for (SpatialVeto aVeto : currentVetos) {
			allVetoParams.allParams.add(aVeto.getParams());
		}
		return allVetoParams;
	}

	@Override
	public long getSettingsVersion() {
		return AllVetoParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		allVetoParams = (AllVetoParams) pamControlledUnitSettings.getSettings();
		restoreVetos(allVetoParams);
		return true;
	}

	private void restoreVetos(AllVetoParams allVetoParams) {
		for (SpatialVetoParams params : allVetoParams.allParams) {
			SpatialVetoProvider provider = findProvider(params.getProviderClassName());
			if (provider == null) {
				System.out.println("Unable to find spatial veto type " + params.getProviderClassName());
				continue;
			}
			SpatialVeto veto = addVeto(provider);
			veto.setParams(params);
		}
		
	}
	
	private SpatialVetoProvider findProvider(String providerName) {
		for (SpatialVetoProvider provider : vetoProviders) {
			if (provider.getClass().getName().equals(providerName)) {
				return provider;
			}
		}
		return null;
	}

}
