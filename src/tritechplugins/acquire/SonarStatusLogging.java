package tritechplugins.acquire;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import tritechgemini.imagedata.GLFStatusData;

/**
 * Log a subset of status data to database. 
 * @author dg50
 *
 */
public class SonarStatusLogging extends SQLLogging {
	
	private PamTableItem source, deviceId, acquire, tempShutdown, oowShutdown, oow, totalRecords;
	/*
	 * 
	public int m_packetCount;
	public int m_recvErrorCount;
	public int m_resentPacketCount;
	public int m_droppedPacketCount;
	public int m_unknownPacketCount;
	 */
	private PamTableItem packetCount, recvErrorCount, resentPacketCount, droppedPacketCount, unkPacketCount;
	private TritechDaqProcess tritechDaqProcess;

	public SonarStatusLogging(TritechDaqProcess tritechDaqProcess, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.tritechDaqProcess = tritechDaqProcess;
		
		PamTableDefinition tableDef = new PamTableDefinition("Sonar Status");
		tableDef.addTableItem(source = new PamTableItem("Source", Types.VARCHAR));
		tableDef.addTableItem(deviceId = new PamTableItem("DeviceId", Types.INTEGER));
		tableDef.addTableItem(acquire = new PamTableItem("Acquire", Types.BOOLEAN));
		tableDef.addTableItem(tempShutdown = new PamTableItem("TempShutdown", Types.BOOLEAN));
		tableDef.addTableItem(oowShutdown = new PamTableItem("oowShutdown", Types.BOOLEAN));
		tableDef.addTableItem(oow = new PamTableItem("OutOfWater", Types.BOOLEAN));
		tableDef.addTableItem(packetCount = new PamTableItem("PacketCount", Types.INTEGER));
		tableDef.addTableItem(recvErrorCount = new PamTableItem("RcvErrorCount", Types.INTEGER));
		tableDef.addTableItem(resentPacketCount = new PamTableItem("ResentPacketCount", Types.INTEGER));
		tableDef.addTableItem(droppedPacketCount = new PamTableItem("DroppedPacketCount", Types.INTEGER));
		tableDef.addTableItem(unkPacketCount = new PamTableItem("UnknownPacketCount", Types.INTEGER));
		tableDef.addTableItem(totalRecords = new PamTableItem("TotalRecords", Types.INTEGER));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		SonarStatusDataUnit statusDataUnit = (SonarStatusDataUnit) pamDataUnit;
		GLFStatusData statusData = statusDataUnit.getStatusData();
		short shutStat = statusData.m_shutdownStatus;
		source.setValue(statusData.source);
		deviceId.setValue((int) statusData.m_deviceID);
		acquire.setValue(statusDataUnit.isPamStarted() ? 1 : 0);
		tempShutdown.setValue((shutStat & GLFStatusData.OVER_TEMPERATURE) != 0 ? 1 : 0);
		oowShutdown.setValue((shutStat & GLFStatusData.OUT_OF_WATERSHUTDOWN) != 0 ? 1 : 0);
		oow.setValue((shutStat & GLFStatusData.OUT_OF_WATER) != 0 ? 1 : 0);
		packetCount.setValue(statusData.m_packetCount);
		recvErrorCount.setValue(statusData.m_recvErrorCount);
		resentPacketCount.setValue(statusData.m_resentPacketCount);
		droppedPacketCount.setValue(statusData.m_droppedPacketCount);
		unkPacketCount.setValue(statusData.m_unknownPacketCount);
		SonarStatusData status = tritechDaqProcess.getTritechDaqSystem().getSonarStatusData(statusData.m_deviceID);
		if (status == null) {
			totalRecords.setValue(null);
		}
		else {
			totalRecords.setValue(status.getTotalImages());
		}
	}

}
