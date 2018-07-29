package com.gpstracker;

import java.util.ArrayList;
import java.util.Date;

public class Track {

    private long mId;
    private Date mStartingDate;
    private Date mEndingDate;

    private ArrayList<TrackPoint> mPoints;

    public Track(Date startingDate, Date endingDate) {
        mId = -1;
        mStartingDate = startingDate;
        mEndingDate = endingDate;
        mPoints = new ArrayList<>();
    }

    public Track(long id, Date startingDate, Date endingDate) {
        mId = id;
        mStartingDate = startingDate;
        mEndingDate = endingDate;
        mPoints = new ArrayList<>();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getStartingDate() {
        return mStartingDate;
    }

    public Date getEndingDate() {
        return mEndingDate;
    }

    public void setPoints(ArrayList<TrackPoint> points) {
        mPoints = points;
    }

    public ArrayList<TrackPoint> getPoints() {
        return mPoints;
    }

    @Override
    public String toString() {
        return "Track {" + '\'' + mStartingDate + '\'' + '\'' + mEndingDate + '\'' + '}';
    }
}