package tritechplugins.record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import PamguardMVC.PamDataBlock;

public class GLFRecorderParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public static int START_IDLE = 0;
	
	public static int START_RECORD = 1;
	
	public int bufferSeconds = 10;
	
	public int initialState = START_IDLE;
	
	public String outputFolder = null;
	
	public int maxSizeMegabytes = 300;
	
	public String imageDataSource;
	
	private HashMap<String, GLFTriggerData> triggerDataHash = new HashMap<String, GLFTriggerData>();
	
	
	public void setTrigerData(PamDataBlock dataBlock, GLFTriggerData triggerData) {
		setTriggerData(dataBlock.getLongDataName(), triggerData);
	}
	
	public GLFTriggerData getTriggerData(PamDataBlock dataBlock, boolean autoCreate) {
		return getTriggerData(dataBlock.getLongDataName(), autoCreate);
	}
	
	/**
	 * Set a set of trigger data for a named data block
	 * @param blockName
	 * @param triggerData
	 */
	public void setTriggerData(String blockName, GLFTriggerData triggerData) {
		triggerDataHash.put(blockName, triggerData);
	}
	
	/**
	 * Get a set of trigger data for a named data block
	 * @param blockName
	 * @param autoCreate
	 * @return
	 */
	public GLFTriggerData getTriggerData(String blockName, boolean autoCreate) {
		GLFTriggerData data = triggerDataHash.get(blockName);
		if (data == null && autoCreate) {
			data = new GLFTriggerData(blockName);
			triggerDataHash.put(blockName, data);
		}
		return data;
	}
	
	/**
	 * Get the hashmap key names. These should be longdatanames of data blocks. 
	 * @return
	 */
	public Set<String> getTriggerHashKeys() {
		return triggerDataHash.keySet();
	}

	/**
	 * Remove a set
	 * @param setName
	 */
	public void removeTriggerData(String setName) {
		triggerDataHash.remove(setName);
	}

//	/**
//	 * Only use this in the dialogs. Not in general programming
//	 * @return the triggerDataHash
//	 */
//	public HashMap<String, GLFTriggerData> getTriggerDataHash() {
//		return triggerDataHash;
//	}

//	/**
//	 * Only use this in the dialogs. Not in general programming
//	 * @param triggerDataHash the triggerDataHash to set
//	 */
//	public void setTriggerDataHash(HashMap<String, GLFTriggerData> triggerDataHash) {
//		this.triggerDataHash = triggerDataHash;
//	}
	

}
