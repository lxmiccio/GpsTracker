package com.gpstracker;

public class TrackPoint {

    private double mAltitude;
    private float mBearing;
    private double mLatitude;
    private double mLongitude;
    private float mSpeed;
    private long mTime;

    public TrackPoint(double altitude, float bearing, double latitude, double longitude, float speed, long time) {
        mAltitude = altitude;
        mBearing = bearing;
        mLatitude = latitude;
        mLongitude = longitude;
        mSpeed = speed;
        mTime = time;
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

    @Override
    public String toString() {
        return "TrackPoint {" + mAltitude + "," + mBearing + ", " + mLatitude + ", " + mLongitude + ", " + mSpeed + '}';
    }
}
