package tritechplugins.detect.track;

import java.sql.Types;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import tritechplugins.detect.threshold.ThresholdDetector;

public class TrackLogging extends SuperDetLogging {

	PamTableItem nPoints, endTime, sonarIds, durationSecs, straightLength, wobblyLength, occupancy;
	private ThresholdDetector thresholdDetector;
	
	public TrackLogging(ThresholdDetector thresholdDetector, SuperDetDataBlock pamDataBlock, boolean createDataUnits) {
		super(pamDataBlock, createDataUnits);
		this.thresholdDetector = thresholdDetector;
		setTableDefinition(makeBasicTable());
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		TrackLinkDataUnit trackLinkDataUnit = new TrackLinkDataUnit(timeMilliseconds);
		double occ = occupancy.getFloatValue();
		trackLinkDataUnit.setMeanOccupancy(occ);
		return trackLinkDataUnit;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		TrackLinkDataUnit trackDataUnit = (TrackLinkDataUnit) pamDataUnit;
		nPoints.setValue(trackDataUnit.getSubDetectionsCount());
		TrackChain chain = trackDataUnit.getTrackChain();
		String sonarIdstring = chain.getsonarIdString();
		if (sonarIdstring == null ) {
			sonarIds.setValue(null);
		}
		else {
			sonarIds.setValue(sonarIdstring);
		}
		endTime.setValue(PamCalendar.formatDBDateTime(chain.getLastTime(), true));
		durationSecs.setValue((float) (trackDataUnit.getDurationInMilliseconds()/1000.));
		straightLength.setValue((float) chain.getEnd2EndMetres());
		wobblyLength.setValue((float) chain.getWobblyLength());
		occupancy.setValue((float) chain.getMeanOccupancy());
		chain.getEndsVector();
	}
	
	public PamTableDefinition makeBasicTable() {
		PamTableDefinition tableDef = new PamTableDefinition(thresholdDetector.getUnitName() + " Tracks");
		tableDef.addTableItem(nPoints = new PamTableItem("N Points", Types.INTEGER));
		tableDef.addTableItem(endTime = new PamTableItem("End Time", Types.TIMESTAMP));
		tableDef.addTableItem(sonarIds = new PamTableItem("SonarIds", Types.CHAR, 50));
		tableDef.addTableItem(durationSecs = new PamTableItem("Duration", Types.REAL));
		tableDef.addTableItem(straightLength = new PamTableItem("Straight Length", Types.REAL));
		tableDef.addTableItem(wobblyLength = new PamTableItem("Wobbly Length", Types.REAL));
		tableDef.addTableItem(occupancy = new PamTableItem("Mean Occupancy", Types.REAL));
		
		return tableDef;
	}

}
