package tritechplugins.acquire;

import geminisdk.structures.GemStatusPacket;

public class SonarStatusData {
		
	protected int totalImages;

	private GemStatusPacket statusPacket;
	
	public SonarStatusData(GemStatusPacket statusPacket) {
		this.statusPacket = statusPacket;
	}

	/**
	 * @return the statusPacket
	 */
	public GemStatusPacket getStatusPacket() {
		return statusPacket;
	}

	/**
	 * @param statusPacket the statusPacket to set
	 */
	public void setStatusPacket(GemStatusPacket statusPacket) {
		this.statusPacket = statusPacket;
	}

	/**
	 * Get the device id. 
	 * @return Device id
	 */
	public int getDeviceId() {
		return statusPacket.m_deviceID;
	}

	/**
	 * @return the totalImages
	 */
	public int getTotalImages() {
		return totalImages;
	}

	/**
	 * @param totalImages the totalImages to set
	 */
	public void setTotalImages(int totalImages) {
		this.totalImages = totalImages;
	}

}
