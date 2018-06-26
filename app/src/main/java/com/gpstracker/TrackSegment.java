package com.gpstracker;

import java.util.ArrayList;
import org.simpleframework.xml.ElementList;

public class TrackSegment {

    @ElementList(inline = true, name = "trkseg")
    private ArrayList<TrackPoint> mPoints;

    public TrackSegment() {
        mPoints = new ArrayList<>();
    }

    public TrackSegment(ArrayList<TrackPoint> trkpts) {
        mPoints = trkpts;
    }

    public void appendPoint(TrackPoint point)
    {
        mPoints.add(point);
    }

//    public void appendPoint(double latitude, double longitude)
//    {
//        TrackPoint point = new TrackPoint(latitude, longitude);
//        mPoints.add(point);
//    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return mPoints;
    }

    @Override
    public String toString() {
        return "TrackSegment {" + mPoints + '}';
    }
}