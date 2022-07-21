package tritechplugins.detect.track;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import tritechgemini.detect.DetectedRegion;
import tritechplugins.detect.threshold.RegionDataUnit;

public class TrackBinarySource extends BinaryDataSource {

	private TrackLinkProcess trackLinkProcess;
	private TrackLinkDataBlock trackLinkDataBlock;

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public TrackBinarySource(TrackLinkProcess trackLinkProcess, TrackLinkDataBlock trackLinkDataBlock) {
		super(trackLinkDataBlock);
		this.trackLinkProcess = trackLinkProcess;
		this.trackLinkDataBlock = trackLinkDataBlock;
	}

	@Override
	public String getStreamName() {
		return "Sonar Tracks";
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return 1;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		
		TrackLinkDataUnit trackUnit = (TrackLinkDataUnit) pamDataUnit;
		TrackChain chain = trackUnit.getTrackChain();
		
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		try {
			/*
			 * Write the main info from the track, which won't be
			 * in the standard header. 
			 */
			int nPoint = trackUnit.getSubDetectionsCount();
			dos.writeInt(nPoint);
			int[] sonars = chain.getSonarIds();
			dos.writeByte(sonars.length);
			for (int i = 0; i < sonars.length; i++) {
				dos.writeShort(sonars[i]);
			}
			dos.writeFloat((float) chain.getEnd2EndMetres());
			dos.writeFloat((float) chain.getWobblyLength());
			dos.writeFloat((float) chain.getMeanOccupancy());
			/*
			 * Then write data for every track point. 
			 */
			for (int i = 0; i < nPoint; i++) {
				RegionDataUnit regionDataUnit = trackUnit.getSubDetection(i);
				DetectedRegion region = regionDataUnit.getRegion();
				dos.writeLong(regionDataUnit.getTimeMilliseconds());
				dos.writeShort(region.getSonarId());
				dos.writeFloat((float) region.getMinBearing());
				dos.writeFloat((float) region.getMaxBearing());
				dos.writeFloat((float) region.getPeakBearing());
				dos.writeFloat((float) region.getMinRange());
				dos.writeFloat((float) region.getMaxRange());
				dos.writeFloat((float) region.getPeakRange());
				dos.writeFloat((float) region.getObjectSize());
				dos.writeFloat((float) region.getOccupancy());
				dos.writeShort(region.getAverageValue());
				dos.writeInt(region.getTotalValue());
				dos.writeShort(region.getMaxValue());
			}
			
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		BinaryObjectData packedData = new BinaryObjectData(0, bos.toByteArray());
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		
		return packedData;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

}
