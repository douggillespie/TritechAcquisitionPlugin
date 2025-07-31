package tritechplugins.record;

public class BufferState {

	private long firstTime, lastTime;
	private int nImage;
	private int bufferSeconds;
	
	public BufferState(long firstTime, long lastTime, int nImage, int bufferSeconds) {
		super();
		this.firstTime = firstTime;
		this.lastTime = lastTime;
		this.nImage = nImage;
		this.bufferSeconds = bufferSeconds;
	}
	
	/**
	 * Get the number of seconds of data in the buffer;
	 * @return n seconds of data
	 */
	public double getBufferedSeconds() {
		return (double) (lastTime-firstTime) / 1000.;
	}
	
	/**
	 * Get the buffer level as a percentage. Max it at 100 in case the buffer has more than that. 
	 * @return
	 */
	public double getBufferPercent() {
		if (bufferSeconds == 0) {
			return 0;
		}
		double p = getBufferedSeconds() / (double) bufferSeconds * 100;
		p = Math.min(p,  100);
		return p;
	}
	

}
