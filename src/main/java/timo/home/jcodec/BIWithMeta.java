/**Helper to include video timing info with BufferedImage*/
package timo.home.jcodec;

import java.awt.image.BufferedImage;

public class BIWithMeta extends BufferedImage{
	private double timeStamp;
	private double duration;
	public BIWithMeta(BufferedImage bi,double timeStamp,double duration){
		super(bi.getWidth(),bi.getHeight(),bi.getType());
		setData(bi.getData());
		this.timeStamp = timeStamp;
		this.duration = duration;
	}

	public double getTimeStamp(){
		return timeStamp;
	}
	
		public double getDuration(){
		return duration;
	}
}
