package timo.home.tracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

import timo.home.jcodec.BIWithMeta;


public class TrackPoint{
	private BIWithMeta currentFrame;
	private BIWithMeta colouredFrame;
	private int colourTolerance;
	private double[] digitisedCoordinates; // = new double[2];
	private int searchRadius = 20;
	private ArrayList<PointHelper> neighbourhood;
	private int width;
	private int height;
	
	public TrackPoint(BIWithMeta currentFrame,double[] digitisedCoordinates,int colourTolerance){
		this.currentFrame = currentFrame;
		this.digitisedCoordinates = digitisedCoordinates;
		this.colourTolerance = colourTolerance;
		width = currentFrame.getWidth();
		height = currentFrame.getHeight();
		neighbourhood = getNeighbourhood(searchRadius);
	}
	
	public BIWithMeta getColoured(){
		return colouredFrame;
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
