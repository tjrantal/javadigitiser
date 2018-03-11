
/*
	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

	N.B.  the above text was copied from http://www.gnu.org/licenses/gpl.html
	unmodified. I have not attached a copy of the GNU license to the source...

    Copyright (C) 2011-2018 Timo Rantalainen, tjrantal@gmail.com
*/

package timo.home.tracking;
public class Point implements Comparable<Point>{
	public int frameNo;
	public double timeStamp;
	public double x;
	public double y;
	public double[] scaledPoints = null;
	public Point(double x,double y, int frameNo, double tStamp){
		this.x = x;
		this.y = y;
		this.frameNo = frameNo;
		this.timeStamp = tStamp;
	}
	
	public Point(double[] digitizedPoint, int frameNo, double tStamp){
		this.x = digitizedPoint[0];
		this.y = digitizedPoint[1];
		this.frameNo = frameNo;
		this.timeStamp = tStamp;
	}
	
	public Point(double x,double y, double[] scaledPoints, int frameNo, double tStamp){
		this.x = x;
		this.y = y;
		this.scaledPoints = (double[]) scaledPoints.clone();
		this.frameNo = frameNo;
		this.timeStamp = tStamp;
	}

	public Point(double[] digitizedPoint, double[] scaledPoints, int frameNo, double tStamp){
		this.x = digitizedPoint[0];
		this.y = digitizedPoint[1];
		this.scaledPoints = (double[]) scaledPoints.clone();
		this.frameNo = frameNo;
		this.timeStamp = tStamp;
	}
	
	public Point(double[] digitizedPoint, double[][] scaledPoints, int frameNo, double tStamp){
		this.x = digitizedPoint[0];
		this.y = digitizedPoint[1];
		double[] temp = new double[scaledPoints.length];
		for (int i =0;i<scaledPoints.length;++i){
			temp[i] = scaledPoints[i][0];
		}
		this.scaledPoints = (double[]) temp.clone();
		this.frameNo = frameNo;
		this.timeStamp = tStamp;
	}
	
	public int compareTo(Point o){
		int returnValue = 0;
		if (o == null || this == null) {throw new NullPointerException();}
		if (this.frameNo == o.frameNo) {return 0;}
		return this.frameNo < o.frameNo ? -1 : 1;		
	}
}
