package tritechplugins.record;

import PamguardMVC.PamDataUnit;

public class GLFRecorderDataUnit extends PamDataUnit {

	private String triggerName;

	public GLFRecorderDataUnit(long startTime, long endTime, GLFTriggerData triggerData) {
		super(startTime);
		setEndTime(endTime);
		if (triggerData != null) {
			this.setTriggerName(triggerData.triggerDataName);
		}
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public void setEndTime(long timeMilliseconds) {
		setDurationInMilliseconds(timeMilliseconds - getTimeMilliseconds());
	}

}
