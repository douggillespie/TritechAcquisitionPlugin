package tritechplugins.detect.threshold;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import tritechgemini.detect.DetectedRegion;

public class RegionLogging extends SQLLogging {
	
	private PamTableItem sonarId, minBearing, maxBearing, minRange, maxRange, objectSize;
	private PamTableItem meanValue, totalValue, maxValue;
	private ThresholdDetector thresholdDetector;

	public RegionLogging(ThresholdDetector thresholdDetector, RegionDataBlock regionDataBlock) {
		super(regionDataBlock);
		this.thresholdDetector = thresholdDetector;
		PamTableDefinition tableDef = makeBaseTable();
		setTableDefinition(tableDef);
	}

	private PamTableDefinition makeBaseTable() {
		PamTableDefinition tableDef = new PamSubtableDefinition(thresholdDetector.getUnitName() + " targets");
		tableDef.addTableItem(sonarId = new PamTableItem("SonarID", Types.INTEGER));
		tableDef.addTableItem(minBearing = new PamTableItem("MinBearing", Types.REAL));
		tableDef.addTableItem(maxBearing = new PamTableItem("MaxBearing", Types.REAL));
		tableDef.addTableItem(minRange = new PamTableItem("MinRange", Types.REAL));
		tableDef.addTableItem(maxRange = new PamTableItem("MaxRange", Types.REAL));
		tableDef.addTableItem(objectSize = new PamTableItem("ObjectSize", Types.REAL));
		tableDef.addTableItem(meanValue = new PamTableItem("MeanValue", Types.INTEGER));
		tableDef.addTableItem(totalValue = new PamTableItem("TotalValue", Types.INTEGER));
		tableDef.addTableItem(maxValue = new PamTableItem("MaxValue", Types.INTEGER));
		return tableDef;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		RegionDataUnit regionDataUnit = (RegionDataUnit) pamDataUnit;
		DetectedRegion region = regionDataUnit.getRegion();
		sonarId.setValue(regionDataUnit.getSonarId());
		double b1 = region.getMinBearing();
		double b2 = region.getMaxBearing();
		minBearing.setValue((float) Math.min(b1, b2));
		maxBearing.setValue((float) Math.max(b1, b2));
		minRange.setValue((float) region.getMinRange());
		maxRange.setValue((float) region.getMaxRange());
		objectSize.setValue((float) region.getObjectSize());
		meanValue.setValue(region.getAverageValue());
		totalValue.setValue(region.getTotalValue());
		maxValue.setValue(region.getMaxValue());
	}

	@Override
	protected RegionDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		int sonarId = this.sonarId.getIntegerValue();
		double minB = minBearing.getFloatValue();
		double maxB = maxBearing.getFloatValue();
		double minR = minRange.getFloatValue();
		double maxR = maxRange.getFloatValue();
		double size = objectSize.getFloatValue();
		int meanV = meanValue.getIntegerValue();
		int totV = totalValue.getIntegerValue();
		int maxV = maxValue.getIntegerValue();
		
		DetectedRegion region = new DetectedRegion(timeMilliseconds, sonarId, minB, maxB, minR, maxR, size, meanV, totV, maxV);
		RegionDataUnit rdu = new RegionDataUnit(timeMilliseconds, sonarId, region);
		return rdu;
	}

}
