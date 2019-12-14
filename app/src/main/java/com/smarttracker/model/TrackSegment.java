package com.smarttracker.model;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;

public class TrackSegment {

    @ElementList(entry = "trkpt", inline = true)
    private ArrayList<TrackPoint> mPoints;

    public TrackSegment() {
        mPoints = new ArrayList<>();
    }

    public ArrayList<TrackPoint> getTrackPoints() {
        return mPoints;
    }

    public void appendPoint(TrackPoint point) {
        mPoints.add(point);
    }

    public void appendPoints(ArrayList<TrackPoint> points) {
        mPoints.addAll(points);
    }

    @Override
    public String toString() {
        return "TrackSegment {" + mPoints + '}';
    }
}