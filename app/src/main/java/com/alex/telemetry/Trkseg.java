package com.alex.telemetry;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 26/02/2017.
 */

public class Trkseg {

    @ElementList(inline = true, name = "trkseg")
    private ArrayList<Trkpt> mTrkpts;

    public Trkseg() {
        mTrkpts = new ArrayList<>();
    }

    public Trkseg(ArrayList<Trkpt> trkpts) {
        mTrkpts = trkpts;
    }

    public ArrayList<Trkpt> getTrkpts() {
        return mTrkpts;
    }

    @Override
    public String toString() {
        return "Trkseg {" + mTrkpts + '}';
    }
}
