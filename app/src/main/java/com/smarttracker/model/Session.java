package com.smarttracker.model;

import android.location.Location;

import org.simpleframework.xml.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Session {

    @Element(name = "id")
    private long mId;

    @Element(name = "name")
    private String mName;

    @Element(name = "time")
    private Date mStartingDate;

    @Element(name = "endtime")
    private Date mEndingDate;

    @Element(name = "trkseg")
    private TrackSegment mTrackSegment;

    public Session() {
        mId = -1;
        mStartingDate = new Date();
        mEndingDate = new Date();
        mTrackSegment = new TrackSegment();
    }

    public Session(long id, String name, Date startingDate, Date endingDate) {
        mId = id;
        mName = name;
        mStartingDate = startingDate;
        mEndingDate = endingDate;

        mTrackSegment = new TrackSegment();
    }

    public int getDuration() {
        long diff = mEndingDate.getTime() - mStartingDate.getTime();
        return (int) diff / 1000;
    }

    public int getLength() {
        ArrayList<TrackPoint> points = mTrackSegment.getTrackPoints();
        double length = 0;

        for (int i = 0; i < points.size() - 1; ++i) {
            TrackPoint p1 = points.get(i);
            TrackPoint p2 = points.get(i + 1);

            Location a = new Location("");
            Location b = new Location("");

            a.setLatitude(p1.getLatitude());
            a.setLongitude(p1.getLongitude());

            b.setLatitude(p2.getLatitude());
            b.setLongitude(p2.getLongitude());

            length += a.distanceTo(b);
        }

        return (int) length;
    }

    public int getTraveledDistance(long time) {
        TrackPoint point = getClosestTrackPoint(time);

        ArrayList<TrackPoint> points = mTrackSegment.getTrackPoints();
        double length = 0;

        for (int i = 0; i < points.size() - 1; ++i) {
            if(points.get(i) == point) {
                break;
            }

            TrackPoint p1 = points.get(i);
            TrackPoint p2 = points.get(i + 1);

            Location a = new Location("");
            Location b = new Location("");

            a.setLatitude(p1.getLatitude());
            a.setLongitude(p1.getLongitude());

            b.setLatitude(p2.getLatitude());
            b.setLongitude(p2.getLongitude());

            length += a.distanceTo(b);
        }

        return (int) length;
    }

    public TrackPoint getClosestTrackPoint(long time) {
        TrackPoint closestPoint = null;
        long minTimeDiff = Long.MAX_VALUE;

        ArrayList<TrackPoint> points = mTrackSegment.getTrackPoints();
        for (int i = 0; i < points.size(); ++i) {
            long timeDiff = time - points.get(i).getTime();
            timeDiff = Math.abs(timeDiff);
            if (timeDiff < minTimeDiff) {
                closestPoint = points.get(i);
                minTimeDiff = timeDiff;
            }
        }

        return closestPoint;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getNameWithDate() {
        SimpleDateFormat nameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        String startingDate = nameDateFormat.format(mStartingDate);
        return mName + "_" + startingDate;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getStartingDate() {
        return mStartingDate;
    }

    public Date getEndingDate() {
        return mEndingDate;
    }

    public void setEndingDate(Date endingDate) {
        mEndingDate = endingDate;
    }

    public ArrayList<TrackPoint> getPoints() {
        return mTrackSegment.getTrackPoints();
    }

    public void appendPoint(TrackPoint point) {
        mTrackSegment.appendPoint(point);
    }

    public void setPoints(ArrayList<TrackPoint> points) {
        mTrackSegment.appendPoints(points);
    }
}
