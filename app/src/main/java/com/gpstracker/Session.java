package com.gpstracker;

import android.location.Location;
import android.util.Log;

import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.Date;

public class Session {

    private long mId;
    private Date mStartingDate;
    private Date mEndingDate;

    @Element(name = "trkseg")
    private TrackSegment mTrackSegment;

    public Session() {
        mId = -1;
        mStartingDate = new Date();
        mEndingDate = new Date();
        mTrackSegment = new TrackSegment();
    }

    public Session(long id, Date startingDate, Date endingDate) {
        mId = id;
        mStartingDate = startingDate;
        mEndingDate = endingDate;
        mTrackSegment = new TrackSegment();
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

        Log.d("Track", "Track length is " + length);

        return (int) length;
    }

    public int getDuration() {
        long diff = mEndingDate.getTime() - mStartingDate.getTime();
        return (int) diff / 1000;
    }

    public int getTraveledDistance(long time) {
        TrackPoint point = getClosestTrackPoint(time);

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

            if(points.get(i) == point) {
                break;
            }
        }

        Log.d("Track", "Traveled length is " + length);

        return (int) length;
    }

    public TrackPoint getClosestTrackPoint(long time) {
        TrackPoint closestPoint = null;
        long minTimeDiff = Long.MAX_VALUE;

        ArrayList<TrackPoint> points = mTrackSegment.getTrackPoints();
        for (int i = 0; i < points.size() - 1; ++i) {
            long timeDiff = time - points.get(i).getTime();
            timeDiff = Math.abs(timeDiff);
            if (timeDiff < minTimeDiff) {
                closestPoint = points.get(i);
                minTimeDiff = timeDiff;
                Log.d("Track", "Min time difference is " + minTimeDiff);
            }
        }

        return closestPoint;
    }

    public TrackPoint getClosestTrackPoint(TrackPoint point) {
        TrackPoint closestPoint = null;
        int minDistance = Integer.MAX_VALUE;

        ArrayList<TrackPoint> points = mTrackSegment.getTrackPoints();
        for (int i = 0; i < points.size() - 1; ++i) {
            Location a = new Location("");
            Location b = new Location("");

            a.setLatitude(points.get(i).getLatitude());
            a.setLongitude(points.get(i).getLongitude());

            b.setLatitude(point.getLatitude());
            b.setLongitude(point.getLongitude());

            int distance = (int) a.distanceTo(b);
            if (distance < minDistance) {
                closestPoint = points.get(i);
                minDistance = distance;
            }
        }

        return closestPoint;
    }

    public long getId() {
        return mId;
    }

    public Date getStartingDate() {
        return mStartingDate;
    }

    public Date getEndingDate() {
        return mEndingDate;
    }

    public TrackSegment getTrackSegment() {
        return mTrackSegment;
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
