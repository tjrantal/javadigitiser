package timo.home.tracking;

import java.util.Vector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import timo.home.jcodec.BIWithMeta;


public class TrackPoint{
	private BIWithMeta currentFrame;
	private BufferedImage colouredFrame;
	private int colourTolerance;
	private double[] digitisedCoordinates; // = new double[2];
	private int searchRadius = 20;
	private ArrayList<PointHelper> neighbourhood;
	private int width;
	private int height;
	private byte[][] fillMask;
	
	public TrackPoint(BIWithMeta currentFrame,double[] digitisedCoordinates,int colourTolerance){
		this.currentFrame = currentFrame;
		this.digitisedCoordinates = digitisedCoordinates;
		this.colourTolerance = colourTolerance;
		width = currentFrame.getWidth();
		height = currentFrame.getHeight();
		neighbourhood = getNeighbourhood(searchRadius);
		
		//Look for the point within the neighbourhood at this point
		
		//TESTING with input point
		int[] colourIn = getColour((int) digitisedCoordinates[0],(int)  digitisedCoordinates[1]);
		floodFill((int) digitisedCoordinates[0],(int)  digitisedCoordinates[1],colourIn[0], colourIn[1], colourIn[2], colourTolerance);
		
	}
	
	/*Clone the original frame for colouring*/
	private BufferedImage clone(BufferedImage bi) {
	 ColorModel cm = bi.getColorModel();
	 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	 WritableRaster raster = bi.copyData(null);
	 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	public BufferedImage getColoured(){
		return colouredFrame;
	}
	

	/**Flood fill marker
	*/
	
	private boolean floodFill(int x, int y, int r, int g, int b, int tol){
		colouredFrame = clone(currentFrame);
		fillMask = new byte[width][height];
		
		int[][] nHood = new int[][]{{0,1},{0,-1},{1,0},{-1,0}};	//4-connected neighbourhood
		Vector<int[]> seedList = new Vector<int[]>();
		seedList.add(new int[]{x,y});
		while (seedList.size() > 0){
			int[] check = seedList.remove(0);	//Get the first seed, and remove from the list
			int[] colour = getColour(check[0], check[1]);
			//Check whether colours are within tolerance
			if (colour[0] >= r-tol & colour[0] <= r+tol &
				colour[1] >= g-tol & colour[1] <= g+tol &
				colour[2] >= b-tol & colour[2] <= b+tol &
				fillMask[check[0]][check[1]] == 0	//Hasn't been filled yet
				){
				fillMask[check[0]][check[1]] = 1;
				colour[0]+= 50;
				colour[1]+= 50;
				colour[2]+= 50;
				for (int c = 0;c<colour.length;++c){
					colour[c] = colour[c]<256 ? colour[c]:255;
				}
				colourPixel(check[0],check[1],colour);
				//Add neighbouring pixels if appropriate
				for (int nh = 0; nh<nHood.length;++nh){
					int tempX = check[0]+nHood[nh][0];
					int tempY = check[1]+nHood[nh][1];
					if ((tempX >-1 & tempX < width & tempY > -1 & tempY < height) && fillMask[tempX][tempY] == 0){
						seedList.add(new int[]{tempX,tempY});
					}
				}
				
			}
		
		}
		return true;
	}
	
	private int[] getColour(int x, int y){
		int pixelcolour = currentFrame.getRGB(x,y);
		int tempR = 0xff & (pixelcolour >> 16);
		int tempG = 0xff & (pixelcolour >> 8);
		int tempB = 0xff & pixelcolour;
		return new int[]{tempR,tempG,tempB};
	}
	
	private void colourPixel(int x, int y, int[] colour){
		int pixelcolour = 0xff<<24 | colour[0] << 16 | colour[1]<<8 | colour[2];
		colouredFrame.setRGB(x,y,pixelcolour);
	}


	/**Get pixel neighbourhood coordinates in a concentric circle
		Will be sorted so that the search proceeds outwards
	*/
	private ArrayList<PointHelper> getNeighbourhood(int searchRadius){
		ArrayList<PointHelper> temp = new ArrayList<PointHelper>();
		int cnt = 0;
		temp.add(new PointHelper(0,0,cnt++));
		for (int i = 1; i<=searchRadius; ++i){
			for (int j = 0; j<2*4*i; ++j){
				//Calculate X and Y
				double cosine = Math.cos(2d*Math.PI*((double) j)/((double) 2*4*i));
				double sine = Math.sin(2d*Math.PI*((double) j)/((double) 2*4*i));
				int tempX = (int) Math.round(((double) i)*cosine);
				int tempY = (int) Math.round(((double) i)*sine);				
				temp.add(new PointHelper(tempX,tempY,cnt++));
			}
		}
		ArrayList<PointHelper> sorted = new ArrayList(new HashSet(temp)); //Use HashSet to get the unique set
		Collections.sort(sorted);	//Sort the points based on index (i.e. in growing circles)
		return sorted;	
	}
	
	
	/**Helper class to get a unique list of coordinates
		Implements equals and hashcode so that HashSet returns the unique points
		Implements Comparable to sort based on index -> will be used to search in growing concentric circles
	*/
	public class PointHelper implements Comparable<PointHelper>{
		public int x;
		public int y;
		public int index;
		public PointHelper(int x, int y, int index){
			this.x = x;
			this.y = y;
			this.index = index;
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof PointHelper){
				if(((PointHelper) obj).x == this.x & ((PointHelper) obj).y == this.y){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		
		}	
		
		@Override
      public int hashCode() {
        return 1;
   	}
   	
   	@Override
   	public int compareTo(PointHelper a){
   		return this.index-a.index;
   	}
	}
}
