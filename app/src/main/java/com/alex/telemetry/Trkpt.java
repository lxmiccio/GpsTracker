package com.alex.telemetry;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Alex on 26/02/2017.
 */

public class Trkpt {

    @Attribute(name = "lat")
    private double mLatitude;

    @Attribute(name = "lon")
    private double mLongitude;

    @Element(name = "name", required = false)
    private String mName;

    public Trkpt() {
        mLatitude = 0;
        mLongitude = 0;
    }
    public Trkpt(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public Trkpt(double latitude, double longitude, String name) {
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "Trkpt {" + mLatitude + ", " + mLongitude + '}';
    }
}
