package tritechplugins.acquire;

import tritechgemini.imagedata.GLFStatusData;

public class SonarStatusData {
		
	protected int totalImages;
	
	protected int interStatusImages;
	
	protected int zeroPacketWarnings;
	
	protected long lastReboot = System.currentTimeMillis();
		
	protected long lastImageTime = System.currentTimeMillis();

	private GLFStatusData statusPacket;
		
	public SonarStatusData(GLFStatusData statusPacket) {
		this.statusPacket = statusPacket;
	}

	/**
	 * @return the statusPacket
	 */
	public GLFStatusData getStatusPacket() {
		return statusPacket;
	}

	/**
	 * @param statusPacket the statusPacket to set
	 */
	public void setStatusPacket(GLFStatusData statusPacket) {
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
