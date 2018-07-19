package com.gpstracker;

import org.simpleframework.xml.Element;

import java.util.Date;

public class Track {

    @Element(name = "name")
    private String mName;
    
    @Element(name = "trkseg")
    private TrackSegment mTrackSegment;

    private Date mStartingDate;
    private Date mEndingDate;

    public Track() {
        mTrackSegment = new TrackSegment();
    }

    public Track(Date startingDate, Date endingDate) {
        mTrackSegment = new TrackSegment();
        mStartingDate = startingDate;
        mEndingDate = endingDate;
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

    public Date getStartingDate() {
        return mStartingDate;
    }

    public Date getEndingDate() {
        return mEndingDate;
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mName + '\'' + ", " + mTrackSegment + '}';
    }
}
