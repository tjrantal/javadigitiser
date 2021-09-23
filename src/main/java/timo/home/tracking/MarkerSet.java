package timo.home.tracking;

import java.util.Vector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

/*Contains the Markers of the current marker set*/
public class MarkerSet{
	public ArrayList<Marker> set = null;
	public MarkerSet(ArrayList<String> labels){
		set = new ArrayList<Marker>(labels.size());
		for (int i = 0;i<labels.size();++i){
			set.add(new Marker(labels.get(i)));
		}
	}
}
