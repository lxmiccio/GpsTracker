package com.example.alex.gpstracker;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class Track {

    @Element(name = "name")
    private String mName;
    
    @Element(name = "trkseg")
    private TrackSegment mTrackSegment;

    public Track() {
        mTrackSegment = new TrackSegment();
    }

    public Track(String name) {
        mName = name;
        mTrackSegment = new TrackSegment();
    }

    public Track(String name, TrackSegment trackSegment) {
        mName = name;
        mTrackSegment = trackSegment;
    }

    public void appendPoint(TrackPoint point)
    {
        mTrackSegment.appendPoint(point);
    }

//    public void appendPoint(double latitude, double longitude)
//    {
//        mTrackSegment.appendPoint(latitude, longitude);
//    }

    public String getName() {
        return mName;
    }

    public TrackSegment getTrackSegment() {
        return mTrackSegment;
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mName + '\'' + ", " + mTrackSegment + '}';
    }
}
