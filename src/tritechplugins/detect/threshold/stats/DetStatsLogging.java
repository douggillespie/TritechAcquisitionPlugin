package tritechplugins.detect.threshold.stats;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class DetStatsLogging extends SQLLogging {
	
	private PamTableItem regionCount, usedRegions, trackCount, nFrame, endTime;

	public DetStatsLogging(PamDataBlock pamDataBlock, String name) {
		super(pamDataBlock);
		PamTableDefinition tableDef = new PamTableDefinition(name);
		tableDef.addTableItem(endTime = new PamTableItem("EndDate", Types.TIMESTAMP));
		tableDef.addTableItem(nFrame = new PamTableItem("Frames", Types.INTEGER));
		tableDef.addTableItem(regionCount = new PamTableItem("Regions", Types.INTEGER));
		tableDef.addTableItem(usedRegions = new PamTableItem("Used Regions", Types.INTEGER));
		tableDef.addTableItem(trackCount = new PamTableItem("Tracks", Types.INTEGER));
		setTableDefinition(tableDef);
	}
	
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DetStatsDataUnit dsdu = (DetStatsDataUnit) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(dsdu.getEndTimeInMilliseconds()));
		nFrame.setValue(dsdu.getnFrame());
		regionCount.setValue(dsdu.getRegionCount());
		usedRegions.setValue(dsdu.getUsedRegionCount());
		trackCount.setValue(dsdu.getTrackCount());
	}

}
