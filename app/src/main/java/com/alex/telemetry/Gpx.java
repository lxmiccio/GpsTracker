package com.alex.telemetry;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by Alex on 26/02/2017.
 */

@Root
public class Gpx {

    //@Attribute(name = "xmlns:xsi")
    private String xsi;

    //@Attribute(name = "xmlns")
    private String xmlns;

    //@Attribute(name = "version")
    private String version;

    //@Attribute(name = "creator")
    private String creator;

    @Element(name = "trk")
    private Trk mTrk;

    public Gpx() {
        xsi = "http://www.w3.org/2001/XMLSchema-instance";
        xmlns = "http://www.topografix.com/GPX/1/1";
        version = "1.1";
        creator = "FVDL";
        mTrk = new Trk();
    }

    public Gpx(Trk trk) {
        xsi = "http://www.w3.org/2001/XMLSchema-instance";
        xmlns = "http://www.topografix.com/GPX/1/1";
        version = "1.1";
        creator = "FVDL";
        mTrk = trk;
    }

    public Trk getTrk() {
        return mTrk;
    }

    @Override
    public String toString() {
        return "Gpx {" + mTrk + '}';
    }
}