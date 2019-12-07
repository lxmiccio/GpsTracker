package com.gpstracker;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;

public class TrackSegment {

    @ElementList(inline = true, name = "trkpt")
    private ArrayList<TrackPoint> mPoints;

    public TrackSegment() {
        mPoints = new ArrayList<>();
    }

    public void appendPoint(TrackPoint point) {
        mPoints.add(point);
    }

    public void appendPoints(ArrayList<TrackPoint> points) {
        mPoints.addAll(points);
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return mPoints;
    }

    @Override
    public String toString() {
        return "TrackSegment {" + mPoints + '}';
    }
}