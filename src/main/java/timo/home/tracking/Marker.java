package timo.home.tracking;

/*Contains the TrackMarker, and the digitised points for a given marker*/
public class Marker{
	public TrackPoint tp;// = new TrackPoint(20);
	public  DigitisedPoints dp;
	public String label;
	public boolean trackOn = false;
	
	public Marker(String label){
		this(label, null, null);
	}
	
	
	public Marker(String label,TrackPoint tp, DigitisedPoints dp){
		this.label = label;
		this.tp = tp;
		this.dp = dp;
	}
	
		
	public Marker(String label,TrackPoint tp, DigitisedPoints dp, boolean trackOn){
		this(label,tp,dp);
		this.trackOn = trackOn;
	}
}
