package com.smarttracker.model;

import android.location.Location;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class TrackPoint {
    @Attribute(name = "alt")
    private double mAltitude;

    @Attribute(name = "bear")
    private float mBearing;

    @Attribute(name = "lat")
    private double mLatitude;

    @Attribute(name = "lon")
    private double mLongitude;

    @Attribute(name = "speed")
    private float mSpeed;

    @Attribute(name = "time")
    private long mTime;

    @Element(name = "name", required = false)
    private String mName;

    public TrackPoint(double altitude, float bearing, double latitude, double longitude, float speed, long time) {
        mAltitude = altitude;
        mBearing = bearing;
        mLatitude = latitude;
        mLongitude = longitude;
        mSpeed = speed;
        mTime = time;
    }

    public float distanceTo(TrackPoint point) {
        Location a = new Location("");
        Location b = new Location("");

        a.setLatitude(mLatitude);
        a.setLongitude(mLongitude);

        b.setLatitude(point.getLatitude());
        b.setLongitude(point.getLongitude());

        return a.distanceTo(b);
    }

    public double getAltitude() {
        return mAltitude;
    }

    public float getBearing() {
        return mBearing;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public long getTime() {
        return mTime;
    }

    @Override
    public String toString() {
        return "TrackPoint {" + mAltitude + "," + mBearing + ", " + mLatitude + ", " + mLongitude + ", " + mSpeed + '}';
    }
}
