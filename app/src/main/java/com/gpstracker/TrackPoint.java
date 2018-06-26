package com.gpstracker;

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

    public TrackPoint() {
        mAltitude = 0;
        mBearing = 0;
        mLatitude = 0;
        mLongitude = 0;
        mSpeed = 0;
        mTime = 0;
    }

    public TrackPoint(double altitude, float bearing, double latitude, double longitude, float speed, long time) {
        mAltitude = altitude;
        mBearing = bearing;
        mLatitude = latitude;
        mLongitude = longitude;
        mSpeed = speed;
        mTime = time;
    }

    public TrackPoint(double altitude, float bearing, double latitude, double longitude, float speed, long time, String name) {
        mAltitude = altitude;
        mBearing = bearing;
        mLatitude = latitude;
        mLongitude = longitude;
        mSpeed = speed;
        mTime = time;
        mName = name;
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

    public long getTime() {
        return mTime;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "TrackPoint {" + mAltitude + "," + mBearing + ", " + mLatitude + ", " + mLongitude + ", " + mSpeed + ", " + mTime + '}';
    }
}
