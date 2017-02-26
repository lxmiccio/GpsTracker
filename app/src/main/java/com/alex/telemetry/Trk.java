package com.alex.telemetry;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Alex on 26/02/2017.
 */

public class Trk {

    @Element(name = "name")
    private String mName;

    @Element(name = "trkseg")
    private Trkseg mTrkseg;

    public Trk() {
        mTrkseg = new Trkseg();
    }

    public Trk(String name, Trkseg trkseg) {
        mName = name;
        mTrkseg = trkseg;
    }

    public String getName() {
        return mName;
    }

    public Trkseg getTrkseg() {
        return mTrkseg;
    }

    @Override
    public String toString() {
        return "Trk {" + '\'' + mName + '\'' + ", " + mTrkseg + '}';
    }
}
