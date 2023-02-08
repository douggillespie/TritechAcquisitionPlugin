package tritechplugins.detect.veto;

import java.awt.Window;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.threshold.ThresholdDetector;
import tritechplugins.detect.veto.circle.CircleVetoProvider;
import tritechplugins.detect.veto.rthi.RThiVetoProvider;
import tritechplugins.detect.veto.swing.VetoDialogPanel;
import tritechplugins.detect.veto.swing.VetoOverlayDraw;
import tritechplugins.detect.veto.xy.XYVetoProvider;

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
	
	private SpatialVetoDataBlock vetoDataBlock;

	public SpatialVetoManager(ThresholdDetector thresholdDetector) {
		this.thresholdDetector = thresholdDetector;
		vetoProviders = new ArrayList<>();
		currentVetos = new ArrayList<>();
		vetoProviders.add(new RThiVetoProvider());
		vetoProviders.add(new XYVetoProvider());
		vetoProviders.add(new CircleVetoProvider());
		
		vetoDataBlock = new SpatialVetoDataBlock(thresholdDetector.getThresholdProcess());
		thresholdDetector.getThresholdProcess().addOutputDataBlock(vetoDataBlock);
		vetoDataBlock.setOverlayDraw(new VetoOverlayDraw());
		
		PamSettingManager.getInstance().registerSettings(this);

		checkExternalVetos();
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
		makeVetoDataUnits();
	}
	
	private SpatialVetoProvider findProvider(String providerName) {
		for (SpatialVetoProvider provider : vetoProviders) {
			if (provider.getClass().getName().equals(providerName)) {
				return provider;
			}
		}
		return null;
	}
	
	public void makeVetoDataUnits() {
		vetoDataBlock.removeEverything();
		if (currentVetos == null) {
			return;
		}
		for (SpatialVeto veto : currentVetos) {
			vetoDataBlock.addPamData(new SpatialVetoDataUnit(PamCalendar.getTimeInMillis(), veto));
		}
	}
	
	/**
	 * Run the vetoes on the detected regions, making a new 
	 * list. 
	 * @param detectedRegions
	 * @return
	 */
	public List<DetectedRegion> runVetos(List<DetectedRegion> detectedRegions) {
		/*
		 *  incoming data are in an array list, so it's inefficient to remove using 
		 *  an iterator, so make a new list to return. 
		 */
		if (detectedRegions == null) {
			return null;
		}
		if (currentVetos == null || currentVetos.size() == 0) {
			return detectedRegions;
		}
		ArrayList<DetectedRegion> passed = new ArrayList(detectedRegions.size());
		
		for (DetectedRegion aRegion : detectedRegions) {
			double x = aRegion.getPeakX();
			double y = aRegion.getPeakY();
			boolean want = true;
			for (SpatialVeto aVeto : currentVetos) {
				if (aVeto.doSonar(aRegion.getSonarId()) == false) {
					continue;
				}
				if (aVeto.isInVeto(x, y)) {
					want = false;
					break; // no need to look at other vetos if it failes on one. 
				}
			}
			if (want) {
				passed.add(aRegion);
//				System.out.printf("Keep region at %3.1f,%3.1f\n", x,y);
			}
//			else {
//				System.out.printf("Veto region at %3.1f,%3.1f\n", x,y);
//			}
				
		}
		
		passed.trimToSize();
		return passed;
	}

	private boolean initialisationComplete = false;
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			checkExternalVetos();
			break;
		case PamController.ADD_CONTROLLEDUNIT:
			if (initialisationComplete) {
				checkExternalVetos();
			}
		}
	}

	private void checkExternalVetos() {
		/*
		 * Look for controlled units which may be a veto provider 
		 * and add them to the list of possibles. 
		 */
		int nUnits = PamController.getInstance().getNumControlledUnits();
		for (int i = 0; i < nUnits; i++) {
			PamControlledUnit pamUnit = PamController.getInstance().getControlledUnit(i);
			if (pamUnit instanceof SpatialVetoProvider) {
				SpatialVetoProvider svp = (SpatialVetoProvider) pamUnit;
				if (findProvider(svp.getName()) == null) {
					vetoProviders.add(svp);
				}
			}
		}
	}

}
