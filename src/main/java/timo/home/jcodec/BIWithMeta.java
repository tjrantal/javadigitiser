/**Helper to include video timing info with BufferedImage*/
package timo.home.jcodec;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BIWithMeta extends BufferedImage{
	private double timeStamp;
	private double duration;
	public BIWithMeta(BufferedImage bi,double timeStamp,double duration){
		super(bi.getWidth(),bi.getHeight(),bi.getType());
		setData(bi.getData());
		this.timeStamp = timeStamp;
		this.duration = duration;
	}
	
	public BIWithMeta(BIWithMeta bi){
		super(bi.getWidth(),bi.getHeight(),bi.getType());
		setData(bi.getData());
		this.timeStamp = bi.getTimeStamp();
		this.duration = bi.getDuration();
	}

	public double getTimeStamp(){
		return timeStamp;
	}
	
	public double getDuration(){
		return duration;
	}
	
	public BIWithMeta binarise(int threshold, int erodeReps){
		double[][] bw = getBW();
		
		//Erode three pixels from the mask
		for (int i = 0; i<erodeReps;++i){
			bw = erode(bw,threshold);
		}
		
		int BLACK = 0xff<<24 | 0 << 16 | 0<<8 | 0;
		int WHITE = 0xff<<24 | 255 << 16 | 255<<8 | 255;
		
		for (int x = 0; x< bw.length; ++x){
			for (int y = 0; y< bw[x].length; ++y){
				if (bw[x][y] < threshold){
					setRGB(x,y,BLACK);
				}else{
					setRGB(x,y,WHITE);
				}
			}
		}
		return this;
	}
	
	/*
		Erode algorithm modified from http://ostermiller.org/dilate_and_erode.html
		Note that this erodes sub-threshold area (because I have black markers I was looking for...
	*/
	private double[][] erode(double[][] data,double threshold) {
		ArrayList<int[]> toErode = new ArrayList<int[]>();
		for (int i = 1; i < data.length - 1; i++) {
			for (int j = 1; j < data[i].length - 1; j++) {
				if (data[i][j] < threshold) {
					// Erode the pixel if any of the neighborhood pixels is background
					if (data[i - 1][j-1] >= threshold || data[i - 1][j] >= threshold || data[i - 1][j+1] >= threshold || 
					data[i][j - 1] >= threshold || data[i][j + 1] >= threshold ||
						data[i + 1][j-1] >= threshold || data[i+1][j] >= threshold || data[i+1][j+1] >= threshold)
					{
						toErode.add(new int[]{i,j});
					}
				}
			}
		}
		for (int i = 0;i<toErode.size();++i){
			data[toErode.get(i)[0]][toErode.get(i)[1]] = Math.min(255d,threshold+1);
		}
		return data;
	}
	
	private double[][] getBW(){
		double[][] ret = new double[this.getWidth()][this.getHeight()];
		for (int x = 0; x< ret.length; ++x){
			for (int y = 0; y< ret[x].length; ++y){
				double[] tempCol = getColour(new int[]{x,y});
				ret[x][y] = (tempCol[0]+tempCol[1]+tempCol[2])/3d;
			}
		}
		return ret;
	}
	
	private double[] getColour(int[] coordinate){
		int pixelcolour = this.getRGB(coordinate[0],coordinate[1]);
		int tempR = 0xff & (pixelcolour >> 16);
		int tempG = 0xff & (pixelcolour >> 8);
		int tempB = 0xff & pixelcolour;
		return new double[]{tempR,tempG,tempB};
	}
}
