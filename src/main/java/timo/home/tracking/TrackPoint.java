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
	private static final int SEARCH_DEFAULT_RADIUS = 20;
	private BIWithMeta currentFrame;
	private int colourTolerance = 10;
	private double[] digitisedCoordinates; // = new double[2];
	private int searchRadius = SEARCH_DEFAULT_RADIUS;
	private ArrayList<PointHelper> neighbourhood;
	private int width;
	private int height;
	private byte[][] fillMask;
	private ArrayList<int[]> colourCoordinates = null;
	int[] colourIn = null;
	
	/**Constructor*/
	public TrackPoint(int searchRadius){
		neighbourhood = getNeighbourhood(searchRadius);
	}
	
	/**Constructor*/
	public TrackPoint(){
		this(SEARCH_DEFAULT_RADIUS);
	}
	
	public byte[][] getMask(){
		return fillMask;
	}
	
	public ArrayList<int[]> getColourCoordinates(){
		return colourCoordinates;
	}
	
	/**Update search radius*/
	public void setSearchRadius(int searchRadius){
		if (this.searchRadius != searchRadius){
			this.searchRadius = searchRadius;
			neighbourhood = getNeighbourhood(searchRadius);			
		}
	}
	
	public int getSearchRadius(){
		return searchRadius;
	}
	
	public void setColourToLookFor(BIWithMeta currentFrame, double[] digitisedCoordinates){
		this.currentFrame = currentFrame;
		int[] coordinates = new int[]{(int) digitisedCoordinates[0],(int)  digitisedCoordinates[1]};
		colourIn = getColour(coordinates);
		//System.out.println(String.format("Set colour to look for r %d g %d b %d",colourIn[0],colourIn[1],colourIn[2]));
	}
	
	/*Call this to refine digitisation, and to visualise the point*/
	public double[] searchMarker(BIWithMeta currentFrame, double[] digitisedCoordinates, int tolerance){
		this.currentFrame = currentFrame;
		width = currentFrame.getWidth();
		height = currentFrame.getHeight();
		int[] coordinates = new int[]{(int) digitisedCoordinates[0],(int)  digitisedCoordinates[1]};
		//Refine digitisation here by looking through the neighbourhood for a matching point
		//System.out.println(String.format("Looking for x %d y %d r %d g %d b %d with %d height %d",coordinates[0],coordinates[1],colourIn[0],colourIn[1],colourIn[2],width,height));
		for (int nh = 0; nh<neighbourhood.size();++nh){
			int[] temp = new int[]{ coordinates[0]+neighbourhood.get(nh).x,coordinates[1]+neighbourhood.get(nh).y};
			int[] pixelColour  = getColour(temp);
			//System.out.println(String.format("Check nh %d x %d y %d r %d g %d b %d",nh,temp[0],temp[1],pixelColour[0],pixelColour[1],pixelColour[2]));
			if ((temp[0] >-1 & temp[0] < width & temp[1] > -1 & temp[1] < height) 
				&& checkColourMatch(pixelColour,colourIn,tolerance) == true){
				//Match found, return refined point coordinates
				//System.out.println(String.format("Found match nh %d x %d y %d",nh,temp[0],temp[1]));
				return floodFill(temp,colourIn, tolerance);						
			}
		}
		return null;	//If no match was found, return null	
	}
	
	public void setFrame(BIWithMeta currentFrame){
		this.currentFrame = currentFrame;
	}
	

	
	public void setTolerance(int colourTolerance){
		this.colourTolerance = colourTolerance;
	}
	
	public int getTolerance(){
		return colourTolerance;
	}
	
	public boolean checkColourMatch(int[] colour,int[] matchColour,int tolerance){
		/*
		System.out.println(String.format("In r %d g %d b %d check r %d g %d b %d",
				colour[0],colour[1],colour[2],
				matchColour[0],matchColour[1],matchColour[2]
			));
		*/
		if (	colour[0] >= matchColour[0]-tolerance & colour[0] <= matchColour[0]+tolerance &
				colour[1] >= matchColour[1]-tolerance & colour[1] <= matchColour[1]+tolerance &
				colour[2] >= matchColour[2]-tolerance & colour[2] <= matchColour[2]+tolerance )
		{
			return true;
		}else{
			return false;
		}
	}
	
	/*Clone the original frame for colouring*/
	private BufferedImage clone(BufferedImage bi) {
	 ColorModel cm = bi.getColorModel();
	 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	 WritableRaster raster = bi.copyData(null);
	 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	//public BufferedImage getColoured(){
	//	return colouredFrame;
	//}
	

	/**Flood fill marker
	*/
	
	private double[] floodFill(int[] coordinate, int[] matchColour, int tol){
		colourCoordinates = null;
		//colouredFrame = clone(currentFrame);
		fillMask = new byte[width][height];
		
		int[][] nHood = new int[][]{{0,1},{0,-1},{1,0},{-1,0}};	//4-connected neighbourhood
		Vector<int[]> seedList = new Vector<int[]>();
		seedList.add(coordinate);
		while (seedList.size() > 0){
			int[] check = seedList.remove(0);	//Get the first seed, and remove from the list
			int[] colour = getColour(check);
			//Check whether colours are within tolerance
			if (checkColourMatch(colour,matchColour,tol) &
				fillMask[check[0]][check[1]] == 0	//Hasn't been filled yet
				){
				fillMask[check[0]][check[1]] = 1;
				/*
				colour[0]+= 50;
				colour[1]+= 50;
				colour[2]+= 50;
				*/
				colour[0]= 255;
				colour[1]= 0;
				colour[2]= 0;
				
				for (int c = 0;c<colour.length;++c){
					colour[c] = colour[c]<256 ? colour[c]:255;
				}
				//colourPixel(check,colour);
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
		//Calculate the centre of the digitised points	
		double[] fillCoordinates = new double[2];
		colourCoordinates = new ArrayList<int[]>();
		int cnt = 0;
		for (int c = 0; c<fillMask.length; ++c){
			for (int r = 0; r<fillMask[c].length; ++r){
				if (fillMask[c][r] == 1){
					fillCoordinates[0] += c;
					fillCoordinates[1] += r;
					++cnt;
					colourCoordinates.add(new int[]{c,r});
				}
			}
		}
		for (int i = 0;i<fillCoordinates.length;++i){
			fillCoordinates[i]/=(double) cnt;
		}
		//System.out.println(String.format("Got coord colourCoordinates.size() %d",colourCoordinates.size()));
		return fillCoordinates;
	}
	
	private int[] getColour(int[] coordinate){
		int pixelcolour = currentFrame.getRGB(coordinate[0],coordinate[1]);
		int tempR = 0xff & (pixelcolour >> 16);
		int tempG = 0xff & (pixelcolour >> 8);
		int tempB = 0xff & pixelcolour;
		return new int[]{tempR,tempG,tempB};
	}
	
	/*
	private void colourPixel(int[] coordinate, int[] colour){
		int pixelcolour = 0xff<<24 | colour[0] << 16 | colour[1]<<8 | colour[2];
		colouredFrame.setRGB(coordinate[0],coordinate[1],pixelcolour);
	}
	*/


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
