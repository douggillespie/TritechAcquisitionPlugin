package tritechplugins.record.logging;

import java.sql.Types;

import PamView.PamTable;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import tritechplugins.record.GLFRecorderCtrl;
import tritechplugins.record.GLFRecorderDataUnit;
import tritechplugins.record.GLFRecorderDataBlock;
import tritechplugins.record.GLFRecorderProcess;

public class GLFRecorderLogging extends SQLLogging {

	private GLFRecorderCtrl recorderControl;
	
	private PamTableItem recordingEnd, triggerName;
//	private PamTableItem fileName;

	public GLFRecorderLogging(GLFRecorderCtrl recorderControl, GLFRecorderDataBlock glfRecordDataBlock) {
		super(glfRecordDataBlock);
		this.recorderControl = recorderControl;
		recordingEnd = new PamTableItem("End Time", Types.TIMESTAMP);
//		fileName = new PamTableItem("File Name", Types.VARCHAR);
		triggerName = new PamTableItem("Trigger", Types.VARCHAR);
		
		PamTableDefinition tableDef = new PamTableDefinition("GLF Recordings");
		tableDef.addTableItem(recordingEnd);
		tableDef.addTableItem(triggerName);
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		GLFRecorderDataUnit rdu = (GLFRecorderDataUnit) pamDataUnit;
		recordingEnd.setValue(sqlTypes.getTimeStamp(rdu.getEndTimeInMilliseconds()));
		String trg = rdu.getTriggerName();
		triggerName.setValue(trg);
	}

}
